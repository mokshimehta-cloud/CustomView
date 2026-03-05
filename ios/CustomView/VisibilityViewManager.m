#import <React/RCTViewManager.h>

@interface RCT_EXTERN_MODULE(VisibilityViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(threshold, NSNumber)
RCT_EXPORT_VIEW_PROPERTY(onVisibilityChange, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onLog, RCTDirectEventBlock)

@end