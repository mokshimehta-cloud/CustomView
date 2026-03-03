import UIKit
import React

class VisibilityView: UIView {

  @objc var threshold: NSNumber = 0.5
  @objc var onVisibilityChange: RCTDirectEventBlock?

  private var isCurrentlyVisible = false
  private var displayLink: CADisplayLink?

  override func didMoveToWindow() {
    super.didMoveToWindow()
    startTracking()
  }

  override func removeFromSuperview() {
    super.removeFromSuperview()
    stopTracking()
  }

  private func startTracking() {
    stopTracking()

    displayLink = CADisplayLink(target: self, selector: #selector(checkVisibility))
    displayLink?.add(to: .main, forMode: .common)
  }

  private func stopTracking() {
    displayLink?.invalidate()
    displayLink = nil
  }

  @objc private func checkVisibility() {
    guard let window = window else { return }

    let viewFrame = convert(bounds, to: window)
    let intersection = viewFrame.intersection(window.bounds)

    if intersection.isNull || bounds.height == 0 {
      updateVisibility(false)
      return
    }

    let visibleHeight = intersection.height
    let ratio = visibleHeight / bounds.height

    updateVisibility(ratio >= CGFloat(truncating: threshold))
  }

  private func updateVisibility(_ visible: Bool) {
    if visible == isCurrentlyVisible { return }

    isCurrentlyVisible = visible
    onVisibilityChange?(["focused": visible])
  }
}