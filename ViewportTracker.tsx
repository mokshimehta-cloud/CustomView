import React, { useRef, useEffect, useState } from 'react';
import { View, Dimensions, findNodeHandle, UIManager } from 'react-native';

const { height: SCREEN_HEIGHT } = Dimensions.get('window');

type Props = {
  scrollY: number;
  onFocus?: () => void;
  onBlur?: () => void;
  threshold?: number;
  children: React.ReactNode;
};

export default function ViewportTracker({
  scrollY,
  onFocus,
  onBlur,
  threshold = 0.5,
  children,
}: Props) {
  const viewRef = useRef<View>(null);
  const [focused, setFocused] = useState(false);

  useEffect(() => {
    if (!viewRef.current) return;

    const handle = findNodeHandle(viewRef.current);

    if (!handle) return;

    UIManager.measure(handle, (_x, _y, _w, h, _px, pageY) => {
      const visibleTop = Math.max(pageY, 0);
      const visibleBottom = Math.min(pageY + h, SCREEN_HEIGHT);

      const visibleHeight = Math.max(visibleBottom - visibleTop, 0);
      const ratio = visibleHeight / h;

      const isVisible = ratio >= threshold;

      if (isVisible && !focused) {
        setFocused(true);
        onFocus?.();
      }

      if (!isVisible && focused) {
        setFocused(false);
        onBlur?.();
      }
    });
  }, [scrollY]);

  return <View ref={viewRef}>{children}</View>;
}
