//
//  FirebaseAuthBridge.h
//  iosApp
//
//  Objective-C header for FirebaseAuthBridge Swift class
//  This allows Kotlin/Native to access Firebase Auth via cinterop
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// Bridge class to expose Firebase Auth functionality to Kotlin/Native
@interface FirebaseAuthBridge : NSObject

/// Sign in with email and password
/// @param email User email
/// @param password User password
/// @param completion Callback with result (success, uid, email, displayName, errorMessage)
- (void)signInWithEmail:(NSString *)email
                password:(NSString *)password
              completion:(void (^)(BOOL success, NSString * _Nullable uid, NSString * _Nullable email, NSString * _Nullable displayName, NSString * _Nullable errorMessage))completion;

/// Sign out current user
- (void)signOut;

/// Get current user
/// @return Tuple (uid, email, displayName) - returned as NSArray with 3 elements
- (NSArray<NSString *> * _Nullable)getCurrentUser;

/// Check if user is signed in
/// @return true if user is signed in, false otherwise
- (BOOL)isUserSignedIn;

/// Get authentication token
/// @param forceRefresh Whether to force token refresh
/// @param completion Callback with token or error
- (void)getAuthTokenWithForceRefresh:(BOOL)forceRefresh
                           completion:(void (^)(NSString * _Nullable token, NSString * _Nullable error))completion;

/// Verify session by checking if current user token is valid
/// @param completion Callback with result (Bool)
- (void)verifySessionWithCompletion:(void (^)(BOOL isValid))completion;

@end

NS_ASSUME_NONNULL_END
