import React, {useRef, useState} from 'react';
import {ActivityIndicator, Text, ToastAndroid, TouchableOpacity, View,} from 'react-native';
import {NativeModules, Platform} from 'react-native';
import {CameraType, CameraView, useCameraPermissions} from 'expo-camera';
import * as MediaLibrary from 'expo-media-library';
import {Ionicons} from '@expo/vector-icons';
import {AutoScrollingTabs} from "./AutoScrollingTabs";

/**
 * The main screen showing a preview of the Camera.
 */
export function CameraScreen() {

    const [permission, requestPermission] = useCameraPermissions();
    const [facing, setFacing] = useState<CameraType>('back');
    const [isCapturing, setIsCapturing] = useState<boolean>(false);
    const cameraRef = useRef<CameraView>(null);

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
        takePicture();
    };

    const goToGallery = () => {
        console.log("User wants to go to the gallery");
        ToastAndroid.show("\"Gallery\" is not implemented yet!!!", ToastAndroid.SHORT);
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
            <AutoScrollingTabs/>
        </View>
    );
}
