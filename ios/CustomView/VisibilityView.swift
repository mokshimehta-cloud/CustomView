import UIKit

class VisibilityView: UIView {

    @objc var threshold: NSNumber = 0.5
    @objc var onVisibilityChange: RCTDirectEventBlock?
    @objc var onLog: RCTDirectEventBlock?

    private var isCurrentlyVisible = false
    private var displayLink: CADisplayLink?
    private var isScrolling = false
    private var scrollStopTimer: Timer?
    private let scrollStopDelay: TimeInterval = 0.15
    
    private var lastContentOffset: CGPoint = .zero
    
    override init(frame: CGRect) {
        super.init(frame: frame)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    private func console(_ message: String) {
        onLog?(["message": message])
    }

    override func didMoveToWindow() {
        super.didMoveToWindow()
        if window != nil {
            console("✅ ATTACHED to window")
            startTracking()
        } else {
            console("❌ REMOVED from window")
            stopTracking()
        }
    }

    override func removeFromSuperview() {
        super.removeFromSuperview()
        stopTracking()
    }

    private func startTracking() {
        stopTracking()
        displayLink = CADisplayLink(target: self, selector: #selector(onFrame))
        displayLink?.preferredFramesPerSecond = 30
        displayLink?.add(to: .main, forMode: .common)
    }

    private func stopTracking() {
        displayLink?.invalidate()
        displayLink = nil
        scrollStopTimer?.invalidate()
        scrollStopTimer = nil
    }

    private func findParentScrollView() -> UIScrollView? {
        var current: UIView? = self.superview
        while current != nil {
            if let scrollView = current as? UIScrollView {
                return scrollView
            }
            current = current?.superview
        }
        return nil
    }
    
    @objc private func onFrame() {
        guard let scrollView = findParentScrollView() else {
            if !isScrolling {
                checkVisibility()
            }
            return
        }
        
        let currentOffset = scrollView.contentOffset
        
        // Detect scroll by comparing content offset
        if currentOffset != lastContentOffset {
            onScrollDetected()
            lastContentOffset = currentOffset
        }
        
        // Only check visibility when not scrolling
        if !isScrolling {
            checkVisibility()
        }
    }
    
    private func onScrollDetected() {
        if !isScrolling {
            console("🔄 Scroll STARTED - pausing videos")
            isScrolling = true
            updateVisibility(false) // Immediately blur when scrolling starts
        }
        
        // Reset scroll stop timer
        scrollStopTimer?.invalidate()
        scrollStopTimer = Timer.scheduledTimer(withTimeInterval: scrollStopDelay, repeats: false) { [weak self] _ in
            self?.onScrollStopped()
        }
    }
    
    private func onScrollStopped() {
        console("🛑 Scroll STOPPED - checking visibility")
        isScrolling = false
        checkVisibility()
    }

    private func checkVisibility() {
        // Don't update focus while scrolling
        if isScrolling {
            return
        }
        
        guard let window = window else {
            updateVisibility(false)
            return
        }

        var visibleRect = window.bounds

        if let scrollView = findParentScrollView() {
            let scrollViewFrame = scrollView.convert(scrollView.bounds, to: window)
            visibleRect = scrollViewFrame.intersection(window.bounds)
        }

        let viewFrame = convert(bounds, to: window)
        let intersection = viewFrame.intersection(visibleRect)

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
        console("🎯 FOCUS: \(visible ? "PLAYING ✅" : "PAUSED ❌")")
        onVisibilityChange?(["focused": visible])
    }
    
    deinit {
        stopTracking()
    }
}