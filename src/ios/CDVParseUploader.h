#import <Cordova/CDV.h>

@interface CDVParseUploader : CDVPlugin

- (NSData *)compressImage:(UIImage *)image;
- (void)upload:(CDVInvokedUrlCommand*)command;

@end
