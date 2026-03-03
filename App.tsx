import React, { useState, useCallback } from 'react';
import { View, Text, StyleSheet, Dimensions, ScrollView } from 'react-native';
import Video from 'react-native-video';
import { SafeAreaView } from 'react-native-safe-area-context';
import VisibilityView from './src/components/VisibilityView';

const { width } = Dimensions.get('window');

type VideoItem = { kind: 'video'; id: string; title: string; urls: string[] };
type TextItem = { kind: 'text'; id: string; heading: string; body: string };
type BannerItem = { kind: 'block'; id: string; label: string; color: string };
type FeedItem = VideoItem | TextItem | BannerItem;

type FlatRow =
  | { kind: 'video'; id: string; url: string }
  | { kind: 'text'; id: string; heading: string; body: string }
  | { kind: 'block'; id: string; label: string; color: string };

const FEED: FeedItem[] = [
  {
    kind: 'text',
    id: 't1',
    heading: 'Welcome',
    body: 'Scroll to see videos',
  },
  {
    kind: 'video',
    id: 'v1',
    title: 'Nature',
    urls: [
      'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
      'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4',
      'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4',
    ],
  },
  {
    kind: 'block',
    id: 'b1',
    label: 'Trending',
    color: '#FF6B35',
  },
  {
    kind: 'video',
    id: 'v2',
    title: 'Short Films',
    urls: [
      'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4',
      'https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/720/Big_Buck_Bunny_720_10s_5MB.mp4',
    ],
  },
];

const flattenFeed = (feed: FeedItem[]): FlatRow[] => {
  const rows: FlatRow[] = [];

  feed.forEach(item => {
    if (item.kind === 'video') {
      item.urls.forEach((url, i) =>
        rows.push({
          kind: 'video',
          id: `${item.id}-${i}`,
          url,
        }),
      );
    } else {
      rows.push(item);
    }
  });

  return rows;
};

const DATA = flattenFeed(FEED);

const VideoRow = ({ url, scrollY }: { url: string; scrollY: number }) => {
  const [playing, setPlaying] = useState(false);

  return (
    <View style={styles.videoContainer}>
      <VisibilityView
        threshold={0.5}
        onFocus={() => setPlaying(true)}
        onBlur={() => setPlaying(false)}
      >
        <Video
          source={{ uri: url }}
          paused={!playing}
          repeat
          resizeMode="cover"
          style={styles.video}
        />
      </VisibilityView>

      <View style={styles.badge}>
        <Text style={styles.badgeText}>
          {playing ? '▶ Playing' : '⏸ Paused'}
        </Text>
      </View>
    </View>
  );
};

export default function App() {
  const [scrollY, setScrollY] = useState(0);

  const renderRow = useCallback(
    (item: FlatRow) => {
      if (item.kind === 'text') {
        return (
          <View key={item.id} style={styles.textCard}>
            <Text style={styles.heading}>{item.heading}</Text>
            <Text style={styles.body}>{item.body}</Text>
          </View>
        );
      }

      if (item.kind === 'block') {
        return (
          <View
            key={item.id}
            style={[styles.banner, { backgroundColor: item.color }]}
          >
            <Text style={styles.bannerText}>{item.label}</Text>
          </View>
        );
      }

      return <VideoRow key={item.id} url={item.url} scrollY={scrollY} />;
    },
    [scrollY],
  );

  return (
    <SafeAreaView style={{ flex: 1 }}>
      <ScrollView
        onScroll={e => setScrollY(e.nativeEvent.contentOffset.y)}
        scrollEventThrottle={32}
      >
        {DATA.map(renderRow)}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  videoContainer: {
    height: 260,
    backgroundColor: 'black',
    marginVertical: 6,
  },

  video: {
    width,
    height: 260,
  },

  badge: {
    position: 'absolute',
    top: 10,
    right: 10,
    backgroundColor: 'rgba(0,0,0,0.6)',
    padding: 6,
    borderRadius: 8,
  },

  badgeText: {
    color: '#fff',
    fontSize: 11,
  },

  textCard: {
    margin: 16,
    padding: 16,
    backgroundColor: '#fff',
    borderRadius: 10,
  },

  heading: {
    fontSize: 18,
    fontWeight: 'bold',
  },

  body: {
    fontSize: 14,
    marginTop: 6,
  },

  banner: {
    margin: 16,
    padding: 30,
    borderRadius: 10,
  },

  bannerText: {
    color: '#fff',
    fontSize: 18,
    fontWeight: 'bold',
  },
});
