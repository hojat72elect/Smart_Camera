import React, {useCallback, useEffect, useMemo, useState} from 'react';
import {ActivityIndicator, FlatList, Image, Modal, Pressable, Text, useWindowDimensions, View,} from 'react-native';
import {getAlbumAsync, getAssetsAsync, requestPermissionsAsync} from 'expo-media-library';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import type {RootStackParamList} from '../App';
import {GalleryItem} from "./GalleryItem";

export function GalleryScreen({navigation}: NativeStackScreenProps<RootStackParamList, 'Gallery'>) {

    const {width: screenWidth, height: screenHeight} = useWindowDimensions();
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [allGalleryItems, setAllGalleryItems] = useState<GalleryItem[]>([]);
    const [isViewerOpen, setIsViewerOpen] = useState(false);
    const [viewerIndex, setViewerIndex] = useState(0);

    const load = useCallback(async () => {
        setIsLoading(true);
        setError(null);
        try {
            const perm = await requestPermissionsAsync();
            if (!perm.granted) {
                setAllGalleryItems([]);
                setError('Media library permission is required to show your photos.');
                return;
            }

            const album = await getAlbumAsync('SmartCamera');
            if (!album) {
                setAllGalleryItems([]);
                return;
            }

            const page = await getAssetsAsync({
                album,
                mediaType: ['photo'],
                sortBy: [['creationTime', false]],
                first: 200,
            });

            setAllGalleryItems(
                page.assets.map(a => ({
                    id: a.id,
                    uri: a.uri,
                    width: a.width,
                    height: a.height,
                    creationTime: a.creationTime,
                })),
            );
        } catch (e) {
            console.error(e);
            setError('Failed to load photos.');
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        return navigation.addListener('focus', () => {
            load();
        });
    }, [navigation, load]);

    const numColumns = 3;
    const keyExtractor = useCallback((it: GalleryItem) => it.id, []);

    const empty = useMemo(() => {
        if (isLoading) return null;
        if (error) return null;
        return (
            <View style={{padding: 20}}>
                <Text style={{color: '#333', textAlign: 'center'}}>
                    No photos yet. Capture a photo and it will appear here.
                </Text>
            </View>
        );
    }, [isLoading, error]);

    const openViewer = useCallback((index: number) => {
        setViewerIndex(index);
        setIsViewerOpen(true);
    }, []);

    return (
        <View style={{flex: 1, backgroundColor: 'white'}}>
            {isLoading ? (
                <View style={{flex: 1, justifyContent: 'center', alignItems: 'center'}}>
                    <ActivityIndicator size="large"/>
                </View>
            ) : error ? (
                <View style={{flex: 1, justifyContent: 'center', alignItems: 'center', padding: 20}}>
                    <Text style={{color: '#333', textAlign: 'center', marginBottom: 12}}>{error}</Text>
                    <Pressable
                        onPress={load}
                        style={{
                            backgroundColor: '#007AFF',
                            paddingHorizontal: 16,
                            paddingVertical: 10,
                            borderRadius: 8,
                        }}
                    >
                        <Text style={{color: 'white'}}>Retry</Text>
                    </Pressable>
                </View>
            ) : (
                <FlatList
                    data={allGalleryItems}
                    keyExtractor={keyExtractor}
                    numColumns={numColumns}
                    contentContainerStyle={{padding: 2}}
                    ListEmptyComponent={empty}
                    renderItem={({item, index}) => (
                        <Pressable
                            onPress={() => openViewer(index)}
                            style={{
                                flex: 1 / numColumns,
                                aspectRatio: 1,
                                padding: 2,
                            }}
                        >
                            <Image
                                source={{uri: item.uri}}
                                style={{flex: 1, borderRadius: 6, backgroundColor: '#eee'}}
                                resizeMode="cover"
                            />
                        </Pressable>
                    )}
                />
            )}

            <Modal visible={isViewerOpen} onRequestClose={() => setIsViewerOpen(false)} animationType="slide">
                <View style={{flex: 1, backgroundColor: 'black'}}>
                    <View
                        style={{
                            paddingTop: 16,
                            paddingHorizontal: 12,
                            paddingBottom: 12,
                            flexDirection: 'row',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                        }}
                    >
                        <Pressable onPress={() => setIsViewerOpen(false)} style={{padding: 8}}>
                            <Text style={{color: 'white'}}>Close</Text>
                        </Pressable>
                        <Text style={{color: 'white'}}>
                            {allGalleryItems.length ? `${viewerIndex + 1} / ${allGalleryItems.length}` : ''}
                        </Text>
                        <View style={{width: 52}}/>
                    </View>

                    <FlatList
                        data={allGalleryItems}
                        horizontal={true}
                        pagingEnabled={true}
                        initialScrollIndex={viewerIndex}
                        onScrollToIndexFailed={() => {
                            // If the list isn't measured yet, we'll still render; user can swipe manually.
                        }}
                        onMomentumScrollEnd={ev => {
                            const w = ev.nativeEvent.layoutMeasurement.width;
                            const x = ev.nativeEvent.contentOffset.x;
                            const i = w ? Math.round(x / w) : 0;
                            setViewerIndex(i);
                        }}
                        keyExtractor={keyExtractor}
                        renderItem={({item}) => (
                            <View style={{
                                width: screenWidth,
                                height: screenHeight,
                                justifyContent: 'center',
                                alignItems: 'center'
                            }}>
                                <Image
                                    source={{uri: item.uri}}
                                    style={{width: '100%', height: '100%'}}
                                    resizeMode="contain"
                                />
                            </View>
                        )}
                    />
                </View>
            </Modal>
        </View>
    );
}

