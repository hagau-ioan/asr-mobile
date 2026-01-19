import Foundation
import FirebaseAuth
import ComposeApp

// Kotlin bridge reference
private let kotlinBridge = ComposeApp.FirebaseAuthBridge.shared

/// Swift bridge class to expose Firebase Auth functionality to Kotlin/Native
/// Uses notification-based communication pattern similar to CameraBridge
@objc public class SwiftFirebaseAuthBridge: NSObject {

    public static let shared = SwiftFirebaseAuthBridge()

    private let auth = Auth.auth()

    override init() {
        super.init()
        setupNotificationObservers()
    }

    private func setupNotificationObservers() {
        // Observe sign in requests from Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleSignInRequest(_:)),
            name: NSNotification.Name("FirebaseAuthSignIn"),
            object: nil
        )

        // Observe sign out requests from Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleSignOutRequest(_:)),
            name: NSNotification.Name("FirebaseAuthSignOut"),
            object: nil
        )

        // Observe get token requests from Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleGetTokenRequest(_:)),
            name: NSNotification.Name("FirebaseAuthGetToken"),
            object: nil
        )

        // Observe verify session requests from Kotlin
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleVerifySessionRequest(_:)),
            name: NSNotification.Name("FirebaseAuthVerifySession"),
            object: nil
        )

        // Update current user data when auth state changes
        auth.addStateDidChangeListener { [weak self] _, user in
            self?.updateCurrentUserData(uid: user?.uid, email: user?.email, displayName: user?.displayName)
        }
    }

    @objc private func handleSignInRequest(_ notification: Notification) {
        guard let userInfo = notification.object as? [String: Any],
              let email = userInfo["email"] as? String,
              let password = userInfo["password"] as? String else {
            return
        }

        auth.signIn(withEmail: email, password: password) { [weak self] result, error in
            if let error = error {
                let errorMessage = self?.getAuthErrorMessage(error) ?? "Authentication failed"
                kotlinBridge.reportSignInResult(
                    success: false,
                    uid: nil,
                    email: nil,
                    displayName: nil,
                    errorMessage: errorMessage
                )
                return
            }

            guard let user = result?.user else {
                kotlinBridge.reportSignInResult(
                    success: false,
                    uid: nil,
                    email: nil,
                    displayName: nil,
                    errorMessage: "User is null"
                )
                return
            }

            kotlinBridge.reportSignInResult(
                success: true,
                uid: user.uid,
                email: user.email,
                displayName: user.displayName,
                errorMessage: nil
            )
        }
    }

    @objc private func handleSignOutRequest(_ notification: Notification) {
        do {
            try auth.signOut()
            updateCurrentUserData(uid: nil, email: nil, displayName: nil)
        } catch {
            print("SwiftFirebaseAuthBridge: Sign out error: \(error.localizedDescription)")
        }
    }

    @objc private func handleGetTokenRequest(_ notification: Notification) {
        guard let user = auth.currentUser else {
            kotlinBridge.reportTokenResult(token: nil, error: "No user signed in")
            return
        }

        let forceRefresh = (notification.object as? [String: Any])?["forceRefresh"] as? Bool ?? false

        user.getIDTokenResult(forcingRefresh: forceRefresh) { result, error in
            if let error = error {
                kotlinBridge.reportTokenResult(token: nil, error: error.localizedDescription)
                return
            }
            kotlinBridge.reportTokenResult(token: result?.token, error: nil)
        }
    }

    @objc private func handleVerifySessionRequest(_ notification: Notification) {
        guard let user = auth.currentUser else {
            kotlinBridge.reportVerifySessionResult(isValid: false)
            return
        }

        // Force a token refresh to verify the session is still valid with the server
        // This will fail if the user has been deleted from Firebase Auth
        user.getIDTokenResult(forcingRefresh: true) { result, error in
            if result?.token != nil && error == nil {
                kotlinBridge.reportVerifySessionResult(isValid: true)
            } else {
                // If token refresh fails, the user may have been deleted
                // Sign out to clear local state
                do {
                    try self.auth.signOut()
                    self.updateCurrentUserData(uid: nil, email: nil, displayName: nil)
                } catch {
                    // Ignore sign out errors
                }
                kotlinBridge.reportVerifySessionResult(isValid: false)
            }
        }
    }

    private func updateCurrentUserData(uid: String?, email: String?, displayName: String?) {
        kotlinBridge.setCurrentUser(uid: uid, email: email, displayName: displayName)
    }

    /// Convert Firebase Auth error to user-friendly message
    private func getAuthErrorMessage(_ error: Error) -> String {
        if let authError = error as? AuthErrorCode {
            switch authError.code {
            case .invalidEmail:
                return "Invalid email address"
            case .wrongPassword:
                return "Wrong password"
            case .userNotFound:
                return "User not found"
            case .userDisabled:
                return "User account has been disabled"
            case .tooManyRequests:
                return "Too many requests. Please try again later"
            case .operationNotAllowed:
                return "Operation not allowed"
            case .networkError:
                return "Network error. Please check your connection"
            default:
                return "Authentication failed: \(error.localizedDescription)"
            }
        }
        return "Authentication failed: \(error.localizedDescription)"
    }
}
