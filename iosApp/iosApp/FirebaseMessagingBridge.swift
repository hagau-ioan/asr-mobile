import Foundation
import FirebaseMessaging
import ComposeApp
import UserNotifications

// Kotlin bridge reference
private let kotlinBridge = ComposeApp.FirebaseMessagingBridge.shared

/// Swift bridge class to expose Firebase Messaging functionality to Kotlin/Native
/// Uses notification-based communication pattern similar to FirebaseAuthBridge
@objc public class SwiftFirebaseMessagingBridge: NSObject {
    
    public static let shared = SwiftFirebaseMessagingBridge()
    
    private let messaging = Messaging.messaging()
    
    override init() {
        super.init()
        setupNotificationObservers()
        setupFirebaseMessaging()
    }
    
    private func setupNotificationObservers() {
        // Observe get token requests from Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleGetTokenRequest(_:)),
            name: NSNotification.Name("FirebaseMessagingGetToken"),
            object: nil
        )
        
        // Observe delete token requests from Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleDeleteTokenRequest(_:)),
            name: NSNotification.Name("FirebaseMessagingDeleteToken"),
            object: nil
        )
        
        // Observe subscribe to topic requests from Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleSubscribeToTopicRequest(_:)),
            name: NSNotification.Name("FirebaseMessagingSubscribeToTopic"),
            object: nil
        )
        
        // Observe unsubscribe from topic requests from Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleUnsubscribeFromTopicRequest(_:)),
            name: NSNotification.Name("FirebaseMessagingUnsubscribeFromTopic"),
            object: nil
        )
    }
    
    private func setupFirebaseMessaging() {
        // Note: Notification permissions and MessagingDelegate are set up in AppDelegate
        // This bridge only handles communication between Kotlin and Swift
    }
    
    @objc private func handleGetTokenRequest(_ notification: Notification) {
        messaging.token { [weak self] token, error in
            if let error = error {
                kotlinBridge.reportGetTokenResult(token: nil, error: error.localizedDescription)
            } else if let token = token {
                kotlinBridge.reportGetTokenResult(token: token, error: nil)
            } else {
                kotlinBridge.reportGetTokenResult(token: nil, error: "Token is nil")
            }
        }
    }
    
    @objc private func handleDeleteTokenRequest(_ notification: Notification) {
        messaging.deleteToken { [weak self] error in
            if let error = error {
                kotlinBridge.reportDeleteTokenResult(success: false)
            } else {
                kotlinBridge.reportDeleteTokenResult(success: true)
            }
        }
    }
    
    @objc private func handleSubscribeToTopicRequest(_ notification: Notification) {
        guard let userInfo = notification.object as? [String: Any],
              let topic = userInfo["topic"] as? String else {
            kotlinBridge.reportSubscribeToTopicResult(success: false)
            return
        }
        
        messaging.subscribe(toTopic: topic) { error in
            if let error = error {
                kotlinBridge.reportSubscribeToTopicResult(success: false)
            } else {
                kotlinBridge.reportSubscribeToTopicResult(success: true)
            }
        }
    }
    
    @objc private func handleUnsubscribeFromTopicRequest(_ notification: Notification) {
        guard let userInfo = notification.object as? [String: Any],
              let topic = userInfo["topic"] as? String else {
            kotlinBridge.reportUnsubscribeFromTopicResult(success: false)
            return
        }
        
        messaging.unsubscribe(fromTopic: topic) { error in
            if let error = error {
                kotlinBridge.reportUnsubscribeFromTopicResult(success: false)
            } else {
                kotlinBridge.reportUnsubscribeFromTopicResult(success: true)
            }
        }
    }
    
    // Notify about token refresh (called from delegate)
    func notifyTokenRefresh(_ token: String) {
        kotlinBridge.notifyTokenRefresh(token: token)
    }
    
    // Notify about notification received (called from delegate)
    func notifyNotificationReceived(title: String?, body: String?, data: [String: String]?) {
        kotlinBridge.notifyNotificationReceived(title: title, body: body, data: data)
    }
}

// Note: MessagingDelegate is implemented in AppDelegate (iOSApp.swift)
// Token refresh is handled there and forwarded to this bridge via notifyTokenRefresh()
