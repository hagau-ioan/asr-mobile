//
//  FirebaseMessagingBridge.h
//  iosApp
//
//  Bridge header for Firebase Messaging functionality
//

#ifndef FirebaseMessagingBridge_h
#define FirebaseMessagingBridge_h

#import <Foundation/Foundation.h>

// Bridge object accessible from Swift
@class FirebaseMessagingBridge;

@interface FirebaseMessagingBridge : NSObject

+ (instancetype)shared;

// Callback types
typedef void (^TokenCallback)(NSString * _Nullable token, NSString * _Nullable error);
typedef void (^BooleanCallback)(BOOL success);
typedef void (^NotificationCallback)(NSString * _Nullable title, NSString * _Nullable body, NSDictionary<NSString *, NSString *> * _Nullable data);

// Methods called from Kotlin
- (void)getTokenWithCompletion:(TokenCallback)completion;
- (void)deleteTokenWithCompletion:(BooleanCallback)completion;
- (void)subscribeToTopic:(NSString *)topic completion:(BooleanCallback)completion;
- (void)unsubscribeFromTopic:(NSString *)topic completion:(BooleanCallback)completion;
- (void)setOnTokenRefreshCallback:(void (^)(NSString *token))callback;
- (void)setOnNotificationReceivedCallback:(NotificationCallback)callback;

// Methods called from Swift to report results to Kotlin
- (void)reportGetTokenResult:(NSString * _Nullable)token error:(NSString * _Nullable)error;
- (void)reportDeleteTokenResult:(BOOL)success;
- (void)reportSubscribeToTopicResult:(BOOL)success;
- (void)reportUnsubscribeFromTopicResult:(BOOL)success;
- (void)notifyTokenRefresh:(NSString *)token;
- (void)notifyNotificationReceived:(NSString * _Nullable)title body:(NSString * _Nullable)body data:(NSDictionary<NSString *, NSString *> * _Nullable)data;

@end

#endif /* FirebaseMessagingBridge_h */
