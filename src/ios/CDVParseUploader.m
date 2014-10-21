#import "CDVParseUploader.h"
#import <Parse/Parse.h>

@implementation CDVParseUploader

- (NSData *)compressImage:(UIImage *)image{
    float actualHeight = image.size.height;
    float actualWidth = image.size.width;
    float maxHeight = 600.0;
    float maxWidth = 800.0;
    float imgRatio = actualWidth/actualHeight;
    float maxRatio = maxWidth/maxHeight;
    float compressionQuality = 0.5;//50 percent compression
    //NSLog(@"Actual w:%@, h:%@", actualWidth, actualHeight);
    
    if (actualHeight > maxHeight || actualWidth > maxWidth){
        if(imgRatio < maxRatio){
            //adjust width according to maxHeight
            imgRatio = maxHeight / actualHeight;
            actualWidth = imgRatio * actualWidth;
            actualHeight = maxHeight;
        }
        else if(imgRatio > maxRatio){
            //adjust height according to maxWidth
            imgRatio = maxWidth / actualWidth;
            actualHeight = imgRatio * actualHeight;
            actualWidth = maxWidth;
        }
        else{
            actualHeight = maxHeight;
            actualWidth = maxWidth;
        }
    }
    CGRect rect = CGRectMake(0.0, 0.0, actualWidth, actualHeight);
    UIGraphicsBeginImageContext(rect.size);
    [image drawInRect:rect];
    UIImage *img = UIGraphicsGetImageFromCurrentImageContext();
    NSData *imageData = UIImageJPEGRepresentation(img, compressionQuality);
    UIGraphicsEndImageContext();
    
    return imageData;
}

- (void)upload:(CDVInvokedUrlCommand*)command
{
    NSURL *fileURI = [NSURL URLWithString:[command.arguments objectAtIndex:0]];
	NSString *filePath = [fileURI path];
	if (filePath != nil && [filePath length] > 0){
        //since compressImage returns a jpeg, we'll force the extension to be .jpg
        NSString *fileName = [[[filePath lastPathComponent] stringByDeletingPathExtension] stringByAppendingPathExtension:@"jpg"];
        NSLog(@"Opening file at %@", filePath);
		UIImage *image;
		image = [UIImage imageWithContentsOfFile:filePath];
        NSData *imageData = [self compressImage:image];
		PFFile *imageFile = [PFFile fileWithName:fileName data:imageData];
		[imageFile saveInBackgroundWithBlock:^(BOOL succeeded, NSError *error){
			CDVPluginResult *pluginResult = nil;
			if (succeeded){
                NSMutableDictionary *returnInfo = [NSMutableDictionary dictionaryWithCapacity:2];
				[returnInfo setObject:imageFile.name forKey:@"name"];
                [returnInfo setObject:imageFile.url forKey:@"url"];
				pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnInfo];
            }
			else {
				NSString *errorString = [NSString stringWithFormat:@"Error: %@ %@", error, [error userInfo]];
				NSLog(errorString);
				pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:errorString];
			}
			[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
		}];
	}
	else {
		CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Arg was null"];
		[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
	}
}

@end
