import React, {useRef, useState} from 'react';
import {
    ActivityIndicator,
    NativeModules,
    Platform,
    ScrollView,
    Text,
    ToastAndroid,
    TouchableOpacity,
    View,
} from 'react-native';
import {CameraType, CameraView, useCameraPermissions} from 'expo-camera';
import * as MediaLibrary from 'expo-media-library';
import {Ionicons} from '@expo/vector-icons';
import type {NativeStackScreenProps} from '@react-navigation/native-stack';
import type {RootStackParamList} from '../App';

/**
 * The main screen showing a preview of the Camera.
 */
export function CameraScreen({navigation}: NativeStackScreenProps<RootStackParamList, 'Camera'>) {

    const CATEGORIES = ["Photo", "Panorama", "Video", "Beautify"];

    const [permission, requestPermission] = useCameraPermissions();
    const [facing, setFacing] = useState<CameraType>('back');
    const [isCapturing, setIsCapturing] = useState<boolean>(false);
    const cameraRef = useRef<CameraView>(null);
    const scrollViewRef = useRef<ScrollView>(null);
    const itemRefs = useRef<{ [key: string]: View | null }>({});
    const [activeTab, setActiveTab] = useState(CATEGORIES[0]); // Default mode of the camera is "Photo"

    const handleTabPress = (tabName: string) => {
        setActiveTab(tabName);

        // Animate scroll to the clicked item
        const selectedItem = itemRefs.current[tabName];
        if (selectedItem && scrollViewRef.current) {
            // measureLayout calculates coordinates relative to the ScrollView node
            selectedItem.measureLayout(
                scrollViewRef.current as any,
                (x, y) => {
                    scrollViewRef.current?.scrollTo({
                        x: x - 16, // Offsets slightly to keep previous tab peeking
                        y: 0,
                        animated: true,
                    });
                },
                () => console.error('Measurement failed') // Fallback if layout isn't ready
            );
        }
    };

    if (!permission) {
        // There was an error while requesting camera permission.
        return <View style={{
            flex: 1,
            backgroundColor: '#000'
        }}/>;
    }

    if (!permission.granted) {
        // Permission is not granted
        return (
            <View style={{
                flex: 1,
                justifyContent: 'center',
                alignItems: 'center',
                padding: 20
            }}>
                <Text style={{
                    fontSize: 16,
                    textAlign: 'center',
                    marginBottom: 20,
                    color: '#333'
                }}>We need your permission to show the camera</Text>
                <TouchableOpacity style={{
                    backgroundColor: '#007AFF',
                    paddingHorizontal: 20,
                    paddingVertical: 10,
                    borderRadius: 8
                }} onPress={requestPermission}>
                    <Text style={{
                        color: 'white',
                        fontSize: 16
                    }}>Grant permission</Text>
                </TouchableOpacity>
            </View>
        );
    }

    const toggleCameraFacing = () => {
        setFacing(current => (current === 'back' ? 'front' : 'back'));
    };

    const ensureSmartCameraAlbum = async (asset: MediaLibrary.Asset) => {
        const albumName = 'SmartCamera';
        const existingAlbum = await MediaLibrary.getAlbumAsync(albumName);

        if (!existingAlbum) {
            await MediaLibrary.createAlbumAsync(albumName, asset, false);
            return;
        }

        await MediaLibrary.addAssetsToAlbumAsync([asset], existingAlbum, false);
    };

    const takePicture = async () => {
        if (cameraRef.current && !isCapturing) {
            setIsCapturing(true);
            try {
                const photo = await cameraRef.current.takePictureAsync({
                    quality: 0.8,
                    base64: false,
                });

                if (photo!.uri) {
                    // First make sure we're in Android and the customized native function exists
                    if (Platform.OS === 'android' && NativeModules.SmartCameraMedia?.saveJpegToDcimSmartCamera) {
                        await NativeModules.SmartCameraMedia.saveJpegToDcimSmartCamera(photo!.uri);
                    } else {
                        const mediaPerm = await MediaLibrary.requestPermissionsAsync();
                        if (!mediaPerm.granted) {
                            ToastAndroid.show("Storage permission is required to save photos", ToastAndroid.SHORT);
                            return;
                        }

                        const asset = await MediaLibrary.createAssetAsync(photo!.uri);
                        await ensureSmartCameraAlbum(asset);
                    }
                    ToastAndroid.show("photo was saved successfully", ToastAndroid.SHORT);
                }
            } catch (error) {
                console.error('Error taking picture:', error);
                ToastAndroid.show("Failed to save the picture!!!", ToastAndroid.SHORT);
            } finally {
                setIsCapturing(false);
            }
        }
    };

    const handleCapture = () => {
        switch (activeTab) {
            case "Photo": {
                takePicture();
                break;
            }
            case "Panorama": {
                ToastAndroid.show("User wants to take a panorama", ToastAndroid.SHORT);
                break;
            }
            case "Video": {
                ToastAndroid.show("User wants to take a video", ToastAndroid.SHORT);
                break;
            }
            case "Beautify": {
                ToastAndroid.show("User wants to take a beautified picture", ToastAndroid.SHORT);
                break;
            }
        }
    };

    const goToGallery = () => {
        navigation.navigate('Gallery');
    };

    return (
        <View style={{
            flex: 1,
            backgroundColor: '#000'
        }}>
            <CameraView
                style={{flex: 1}}
                facing={facing}
                ref={cameraRef}
            >


                <View style={{
                    position: 'absolute',
                    flexDirection: "column",
                    bottom: 0,
                    left: 0,
                    right: 0,
                    paddingBottom: 40,
                }}>
                    {/* Bottom Controls */}
                    <View style={{
                        flexDirection: 'row',
                        justifyContent: 'space-evenly',
                        alignItems: 'center'
                    }}>

                        {/*The gallery button*/}
                        <TouchableOpacity
                            style={{
                                width: 50,
                                height: 50,
                                backgroundColor: '#ccc',
                                borderRadius: 40,
                            }}
                            onPress={goToGallery}
                            disabled={isCapturing}
                        >
                            {/*todo : inside the gallery button, we show a thumbnail of the last photo captured*/}

                        </TouchableOpacity>

                        {/* Capture Button */}
                        <TouchableOpacity
                            style={[{
                                width: 80,
                                height: 80,
                                borderRadius: 40,
                                backgroundColor: 'white',
                                justifyContent: 'center',
                                alignItems: 'center',
                                borderWidth: 4,
                                borderColor: '#ddd'
                            }, isCapturing && {
                                backgroundColor: '#ccc',
                                borderColor: '#999'
                            }]}
                            onPress={handleCapture}
                            disabled={isCapturing}
                        >
                            {isCapturing ? (
                                <ActivityIndicator size="large" color="white"/>
                            ) : (
                                <View style={{
                                    width: 60,
                                    height: 60,
                                    borderRadius: 30,
                                    backgroundColor: '#ff3b30'
                                }}/>
                            )}
                        </TouchableOpacity>


                        <TouchableOpacity style={{
                            backgroundColor: 'rgba(0, 0, 0, 0.5)',
                            padding: 12,
                            borderRadius: 25,
                            alignItems: 'center',
                            justifyContent: 'center'
                        }} onPress={toggleCameraFacing}>
                            <Ionicons name="camera-reverse" size={24} color="white"/>
                        </TouchableOpacity>
                    </View>
                </View>
            </CameraView>
            <View style={{
                paddingVertical: 20,
                backgroundColor: "transparent"
            }}>
                <ScrollView
                    ref={scrollViewRef}
                    horizontal={true}
                    showsHorizontalScrollIndicator={false}
                    contentContainerStyle={{
                        paddingHorizontal: 10,
                        gap: 10,
                    }}
                >
                    {CATEGORIES.map((category) => {
                        const isActive = activeTab === category;
                        return (
                            <View
                                key={category}
                                // Store the native node reference for layout measurement
                                ref={(element) => {
                                    itemRefs.current[category] = element;
                                }}
                                collapsable={false} // Crucial: prevents Android from optimizing away the view hierarchy
                            >
                                <TouchableOpacity
                                    onPress={() => handleTabPress(category)}
                                    style={{
                                        paddingHorizontal: 16,
                                        paddingVertical: 0,
                                        borderRadius: 20,
                                    }}
                                >
                                    <Text style={[{
                                        fontSize: 14,
                                        fontWeight: '600',
                                        color: '#595F5FFF',
                                    }, isActive && {color: '#fff'}]}>
                                        {category}
                                    </Text>
                                </TouchableOpacity>
                            </View>
                        );
                    })}
                </ScrollView>
            </View>
        </View>
    );
}
