import Foundation
import FirebaseStorage
import ComposeApp

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
    }
    
    @objc private func handleDownloadFileRequest(_ notification: Notification) {
        guard let userInfo = notification.object as? [String: Any],
              let path = userInfo["path"] as? String else {
            // Report error back to Kotlin
            ComposeApp.FirebaseStorageBridgeKt.reportDownloadResult(
                content: nil,
                error: "Invalid download request data"
            )
            return
        }
        
        let storageRef = storage.reference().child(path)
        
        // Maximum download size: 10 MB
        let maxSize: Int64 = 10 * 1024 * 1024
        
        storageRef.getData(maxSize: maxSize) { [weak self] data, error in
            if let error = error {
                ComposeApp.FirebaseStorageBridgeKt.reportDownloadResult(
                    content: nil,
                    error: error.localizedDescription
                )
                return
            }
            
            guard let data = data,
                  let content = String(data: data, encoding: .utf8) else {
                ComposeApp.FirebaseStorageBridgeKt.reportDownloadResult(
                    content: nil,
                    error: "Failed to decode file content"
                )
                return
            }
            
            ComposeApp.FirebaseStorageBridgeKt.reportDownloadResult(
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
            ComposeApp.FirebaseStorageBridgeKt.reportUploadResult(
                success: false,
                error: "Invalid upload request data"
            )
            return
        }
        
        guard let localFileURL = URL(string: localPath) ?? URL(fileURLWithPath: localPath) as URL? else {
            ComposeApp.FirebaseStorageBridgeKt.reportUploadResult(
                success: false,
                error: "Invalid local file path"
            )
            return
        }
        
        let storageRef = storage.reference().child(remotePath)
        
        // Create upload task
        let uploadTask = storageRef.putFile(from: localFileURL, metadata: nil) { metadata, error in
            if let error = error {
                ComposeApp.FirebaseStorageBridgeKt.reportUploadResult(
                    success: false,
                    error: error.localizedDescription
                )
                return
            }
            
            // Upload successful
            ComposeApp.FirebaseStorageBridgeKt.reportUploadResult(
                success: true,
                error: nil
            )
        }
        
        // Monitor upload progress
        uploadTask.observe(.progress) { snapshot in
            guard let progress = snapshot.progress else { return }
            let percentComplete = Float(progress.completedUnitCount) / Float(progress.totalUnitCount)
            ComposeApp.FirebaseStorageBridgeKt.reportUploadProgress(progress: percentComplete)
        }
    }
}
