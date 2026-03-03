import React from 'react';
import {
  requireNativeComponent,
  ViewProps,
  NativeSyntheticEvent,
} from 'react-native';

type VisibilityEvent = {
  focused: boolean;
};

type NativeProps = ViewProps & {
  threshold?: number;
  onVisibilityChange?: (event: NativeSyntheticEvent<VisibilityEvent>) => void;
};

const NativeVisibilityView =
  requireNativeComponent<NativeProps>('VisibilityView');

type Props = ViewProps & {
  threshold?: number;
  onFocus?: () => void;
  onBlur?: () => void;
};

export default function VisibilityView({
  threshold = 0.5,
  onFocus,
  onBlur,
  ...rest
}: Props) {
  return (
    <NativeVisibilityView
      {...rest}
      threshold={threshold}
      onVisibilityChange={event => {
        const { focused } = event.nativeEvent;

        if (focused) {
          onFocus?.();
        } else {
          onBlur?.();
        }
      }}
    />
  );
}
