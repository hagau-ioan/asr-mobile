import Foundation
import FirebaseStorage
import ComposeApp

// Kotlin bridge reference
private let kotlinBridge = ComposeApp.FirebaseStorageBridge.shared

/// Bridge class to expose Firebase Storage functionality to Kotlin/Native
/// Uses notification-based communication pattern similar to FirebaseAuthBridge
@objc public class FirebaseStorageBridge: NSObject {
    
    public static let shared = FirebaseStorageBridge()
    
    private let storage = Storage.storage()
    
    override init() {
        super.init()
        setupNotificationObservers()
    }
    
    private func setupNotificationObservers() {
        // Observe download file requests from Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleDownloadFileRequest(_:)),
            name: NSNotification.Name("FirebaseStorageDownloadFile"),
            object: nil
        )

        // Observe upload file requests from Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleUploadFileRequest(_:)),
            name: NSNotification.Name("FirebaseStorageUploadFile"),
            object: nil
        )

        // Observe list files requests from Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleListFilesRequest(_:)),
            name: NSNotification.Name("FirebaseStorageListFiles"),
            object: nil
        )

        // Observe delete file requests from Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleDeleteFileRequest(_:)),
            name: NSNotification.Name("FirebaseStorageDeleteFile"),
            object: nil
        )

        // Observe download to temp requests from Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleDownloadToTempRequest(_:)),
            name: NSNotification.Name("FirebaseStorageDownloadToTemp"),
            object: nil
        )
    }
    
    // Track active downloads to prevent duplicates
    private var activeDownloadPaths = Set<String>()
    
    @objc private func handleDownloadFileRequest(_ notification: Notification) {
        guard let userInfo = notification.object as? [String: Any],
              let path = userInfo["path"] as? String else {
            // Report error back to Kotlin (no path available for invalid request)
            kotlinBridge.reportDownloadResult(
                path: "",
                content: nil,
                error: "Invalid download request data"
            )
            return
        }
        
        // Check if download is already in progress for this path
        if activeDownloadPaths.contains(path) {
            // Download already in progress, skip duplicate request
            // The existing download will complete and notify all callbacks via Kotlin bridge
            return
        }
        
        // Mark as in progress
        activeDownloadPaths.insert(path)
        
        let storageRef = storage.reference().child(path)
        
        // Maximum download size: 10 MB
        let maxSize: Int64 = 10 * 1024 * 1024
        
        storageRef.getData(maxSize: maxSize) { [weak self] data, error in
            guard let self = self else { return }
            
            // Remove from active downloads
            self.activeDownloadPaths.remove(path)
            
            if let error = error {
                kotlinBridge.reportDownloadResult(
                    path: path,
                    content: nil,
                    error: error.localizedDescription
                )
                return
            }
            
            guard let data = data,
                  let content = String(data: data, encoding: .utf8) else {
                kotlinBridge.reportDownloadResult(
                    path: path,
                    content: nil,
                    error: "Failed to decode file content"
                )
                return
            }
            
            kotlinBridge.reportDownloadResult(
                path: path,
                content: content,
                error: nil
            )
        }
    }
    
    @objc private func handleUploadFileRequest(_ notification: Notification) {
        guard let userInfo = notification.object as? [String: Any],
              let localPath = userInfo["localPath"] as? String,
              let remotePath = userInfo["remotePath"] as? String else {
            // Report error back to Kotlin
            kotlinBridge.reportUploadResult(
                success: false,
                error: "Invalid upload request data"
            )
            return
        }
        
        guard let localFileURL = URL(string: localPath) ?? URL(fileURLWithPath: localPath) as URL? else {
            kotlinBridge.reportUploadResult(
                success: false,
                error: "Invalid local file path"
            )
            return
        }
        
        let storageRef = storage.reference().child(remotePath)
        
        // Create upload task
        let uploadTask = storageRef.putFile(from: localFileURL, metadata: nil) { metadata, error in
            if let error = error {
                kotlinBridge.reportUploadResult(
                    success: false,
                    error: error.localizedDescription
                )
                return
            }
            
            // Upload successful
            kotlinBridge.reportUploadResult(
                success: true,
                error: nil
            )
        }
        
        // Monitor upload progress
        uploadTask.observe(.progress) { snapshot in
            guard let progress = snapshot.progress else { return }
            let percentComplete = Float(progress.completedUnitCount) / Float(progress.totalUnitCount)
            kotlinBridge.reportUploadProgress(progress: percentComplete)
        }
    }

    @objc private func handleListFilesRequest(_ notification: Notification) {
        guard let userInfo = notification.object as? [String: Any],
              let path = userInfo["path"] as? String else {
            kotlinBridge.reportListFilesResult(
                files: [],
                error: "Invalid list files request data"
            )
            return
        }

        let storageRef = storage.reference().child(path)

        storageRef.listAll { [weak self] result, error in
            guard self != nil else { return }

            if let error = error {
                kotlinBridge.reportListFilesResult(
                    files: [],
                    error: error.localizedDescription
                )
                return
            }

            guard let result = result else {
                kotlinBridge.reportListFilesResult(
                    files: [],
                    error: "No result returned"
                )
                return
            }

            // Fetch metadata for each item
            let group = DispatchGroup()
            var files: [ComposeApp.CloudStorageFile] = []

            for item in result.items {
                group.enter()
                item.getMetadata { metadata, error in
                    if let metadata = metadata {
                        let file = ComposeApp.CloudStorageFile(
                            path: item.fullPath,
                            name: item.name,
                            sizeBytes: metadata.size,
                            updatedTimeMillis: Int64((metadata.updated?.timeIntervalSince1970 ?? 0) * 1000)
                        )
                        files.append(file)
                    } else {
                        // Add file with default values if metadata fails
                        let file = ComposeApp.CloudStorageFile(
                            path: item.fullPath,
                            name: item.name,
                            sizeBytes: 0,
                            updatedTimeMillis: 0
                        )
                        files.append(file)
                    }
                    group.leave()
                }
            }

            group.notify(queue: .main) {
                // Sort by updated time descending
                let sortedFiles = files.sorted { $0.updatedTimeMillis > $1.updatedTimeMillis }
                kotlinBridge.reportListFilesResult(
                    files: sortedFiles,
                    error: nil
                )
            }
        }
    }

    @objc private func handleDeleteFileRequest(_ notification: Notification) {
        guard let userInfo = notification.object as? [String: Any],
              let remotePath = userInfo["remotePath"] as? String else {
            kotlinBridge.reportDeleteFileResult(
                success: false,
                error: "Invalid delete request data"
            )
            return
        }

        let storageRef = storage.reference().child(remotePath)

        storageRef.delete { error in
            if let error = error {
                kotlinBridge.reportDeleteFileResult(
                    success: false,
                    error: error.localizedDescription
                )
                return
            }

            kotlinBridge.reportDeleteFileResult(
                success: true,
                error: nil
            )
        }
    }

    @objc private func handleDownloadToTempRequest(_ notification: Notification) {
        guard let userInfo = notification.object as? [String: Any],
              let remotePath = userInfo["remotePath"] as? String else {
            kotlinBridge.reportDownloadToTempResult(
                localPath: nil,
                error: "Invalid download request data"
            )
            return
        }

        let storageRef = storage.reference().child(remotePath)

        // Create temp file URL
        let fileName = (remotePath as NSString).lastPathComponent
        let fileExtension = (fileName as NSString).pathExtension
        let tempDir = FileManager.default.temporaryDirectory
        let tempFileURL = tempDir.appendingPathComponent("firebase_\(UUID().uuidString).\(fileExtension)")

        storageRef.write(toFile: tempFileURL) { url, error in
            if let error = error {
                kotlinBridge.reportDownloadToTempResult(
                    localPath: nil,
                    error: error.localizedDescription
                )
                return
            }

            guard let url = url else {
                kotlinBridge.reportDownloadToTempResult(
                    localPath: nil,
                    error: "Download returned nil URL"
                )
                return
            }

            kotlinBridge.reportDownloadToTempResult(
                localPath: url.path,
                error: nil
            )
        }
    }
}
