import SwiftUI
import ComposeApp
import UIKit
import FirebaseCore
import FirebaseMessaging
import UserNotifications

// App Delegate adapter for Firebase compatibility
// 
// IMPORTANT: For Firebase Cloud Messaging (FCM) to work on iOS, you need:
// 1. APNs Key or Certificate configured in Firebase Console
// 2. Apple Developer Program (paid - $99/year) to create APNs Key from Apple Developer Portal
//    - Personal Team (free) can test the app and generate FCM tokens, but cannot create APNs Key
//    - Without APNs Key in Firebase, push notifications will NOT be delivered
// 3. See FIREBASE_SPM_SETUP_INSTRUCTIONS.md for detailed setup steps
//
class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        // Firebase is already configured in iOSApp.init()
        
        // Set up notification delegates
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        
        // Request notification permissions
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if granted {
                DispatchQueue.main.async {
                    application.registerForRemoteNotifications()
                }
            }
        }
        
        return true
    }
    
    // Handle APNs token registration failure
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("Failed to register for remote notifications: \(error.localizedDescription)")
    }
    
    // Handle APNs token registration
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
        print("APNs token registered successfully")
    }
    
    // Handle notification when app is in foreground
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let userInfo = notification.request.content.userInfo

        // Extract notification data
        let title = notification.request.content.title
        let body = notification.request.content.body
        let data: [String: String] = userInfo.reduce(into: [:]) { result, pair in
            if let key = pair.key as? String, key != "aps" {
                // Convert value to String (handles Any type from FCM data payload)
                let valueString: String?
                if let stringValue = pair.value as? String {
                    valueString = stringValue
                } else {
                    valueString = String(describing: pair.value)
                }
                if let value = valueString {
                    result[key] = value
                }
            }
        }

        // Notify Kotlin bridge
        SwiftFirebaseMessagingBridge.shared.notifyNotificationReceived(title: title, body: body, data: data)
        
        // Show notification even when app is in foreground
        completionHandler([.banner, .sound, .badge])
    }
    
    // Handle notification tap
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo

        // Extract notification data
        let title = response.notification.request.content.title
        let body = response.notification.request.content.body
        let data: [String: String] = userInfo.reduce(into: [:]) { result, pair in
            if let key = pair.key as? String, key != "aps" {
                // Convert value to String (handles Any type from FCM data payload)
                let valueString: String?
                if let stringValue = pair.value as? String {
                    valueString = stringValue
                } else {
                    valueString = String(describing: pair.value)
                }
                if let value = valueString {
                    result[key] = value
                }
            }
        }

        // Notify Kotlin bridge
        SwiftFirebaseMessagingBridge.shared.notifyNotificationReceived(title: title, body: body, data: data)
        
        completionHandler()
    }
    
    // Handle FCM token refresh
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        if let token = fcmToken {
            SwiftFirebaseMessagingBridge.shared.notifyTokenRefresh(token)
        }
    }
}

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    init() {
        // Initialize Firebase
        FirebaseApp.configure()
        
        // Initialize Firebase Auth Bridge for Kotlin/Native interop
        // This sets up the notification observers for communication between Swift and Kotlin
        _ = SwiftFirebaseAuthBridge.shared
        
        // Initialize Firebase Storage Bridge for Kotlin/Native interop
        // This sets up the notification observers for communication between Swift and Kotlin
        _ = FirebaseStorageBridge.shared
        
        // Initialize Firebase Messaging Bridge for Kotlin/Native interop
        // This sets up the notification observers for communication between Swift and Kotlin
        _ = SwiftFirebaseMessagingBridge.shared
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

struct ContentView: View {
    @State private var showCamera = false
    
    var body: some View {
        ZStack {
            ComposeView(showCamera: $showCamera)
                .ignoresSafeArea(.all)
            
            if showCamera {
                ImagePicker(onImageCaptured: { image in
                    if let image = image {
                        // Save image using ImageCapture
                        let imageCapture = ImageCapture()
                        _ = imageCapture.saveImageData(image: image)
                        CameraBridge.shared.reportCaptureResult(success: true)
                    } else {
                        CameraBridge.shared.cancelCapture()
                    }
                    showCamera = false
                })
            }
        }
        .onReceive(NotificationCenter.default.publisher(for: NSNotification.Name("LaunchCamera"))) { _ in
            showCamera = true
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    @Binding var showCamera: Bool
    
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ImagePicker: UIViewControllerRepresentable {
    let onImageCaptured: (UIImage?) -> Void
    
    func makeUIViewController(context: Context) -> UIImagePickerController {
        let picker = UIImagePickerController()
        picker.delegate = context.coordinator
        picker.sourceType = .camera
        return picker
    }
    
    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(onImageCaptured: onImageCaptured)
    }
    
    class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let onImageCaptured: (UIImage?) -> Void
        
        init(onImageCaptured: @escaping (UIImage?) -> Void) {
            self.onImageCaptured = onImageCaptured
        }
        
        func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
            let image = info[.originalImage] as? UIImage
            onImageCaptured(image)
        }
        
        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
            onImageCaptured(nil)
        }
    }
}
