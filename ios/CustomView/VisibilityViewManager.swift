import Foundation

@objc(VisibilityViewManager)
class VisibilityViewManager: RCTViewManager {
  
  override func view() -> UIView? {
    return VisibilityView(frame: .zero)
  }
  
  override static func requiresMainQueueSetup() -> Bool {
    return true
  }
}