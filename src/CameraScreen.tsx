import React, {useEffect, useRef, useState} from 'react';
import {ActivityIndicator, Alert, Text, TouchableOpacity, View,} from 'react-native';
import {CameraType, CameraView, FlashMode, useCameraPermissions} from 'expo-camera';
import * as MediaLibrary from 'expo-media-library';
import {Ionicons} from '@expo/vector-icons';

export function CameraScreen() {
    const [permission, requestPermission] = useCameraPermissions();
    const [facing, setFacing] = useState<CameraType>('back');
    const [flash, setFlash] = useState<FlashMode>('off');
    const [timer, setTimer] = useState<number>(0);
    const [isTimerActive, setIsTimerActive] = useState<boolean>(false);
    const [countdown, setCountdown] = useState<number>(0);
    const [isCapturing, setIsCapturing] = useState<boolean>(false);
    const cameraRef = useRef<CameraView>(null);

    useEffect(() => {
        if (isTimerActive && countdown > 0) {
            const timerId = setTimeout(() => {
                setCountdown(countdown - 1);
            }, 1000);
            return () => clearTimeout(timerId);
        } else if (isTimerActive && countdown === 0) {
            takePicture();
            setIsTimerActive(false);
        }
    }, [isTimerActive, countdown]);

    if (!permission) {
        return <View style={{
            flex: 1,
            backgroundColor: '#000'
        }}/>;
    }

    if (!permission.granted) {
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

    const toggleFlash = () => {
        setFlash(current => (current === 'off' ? 'on' : 'off'));
    };

    const setTimerLength = (seconds: number) => {
        setTimer(seconds);
        if (seconds === 0) {
            setIsTimerActive(false);
            setCountdown(0);
        }
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
                    await MediaLibrary.saveToLibraryAsync(photo!.uri);
                    Alert.alert('Success', 'Photo saved to your gallery!');
                }
            } catch (error) {
                console.error('Error taking picture:', error);
                Alert.alert('Error', 'Failed to take picture');
            } finally {
                setIsCapturing(false);
                setCountdown(0);
            }
        }
    };

    const handleCapture = () => {
        if (timer > 0) {
            setCountdown(timer);
            setIsTimerActive(true);
        } else {
            takePicture();
        }
    };

    return (
        <View style={{
            flex: 1,
            backgroundColor: '#000'
        }}>
            <CameraView
                style={{flex: 1}}
                facing={facing}
                flash={flash}
                ref={cameraRef}
            >
                {/* Timer Countdown Overlay */}
                {countdown > 0 && (
                    <View style={{
                        position: 'absolute',
                        top: '50%',
                        left: '50%',
                        transform: [{translateX: -50}, {translateY: -50}],
                        backgroundColor: 'rgba(0, 0, 0, 0.7)',
                        borderRadius: 50,
                        width: 100,
                        height: 100,
                        justifyContent: 'center',
                        alignItems: 'center'
                    }}>
                        <Text style={{
                            fontSize: 48,
                            fontWeight: 'bold',
                            color: 'white'
                        }}>{countdown}</Text>
                    </View>
                )}

                {/* Top Controls */}
                <View style={{
                    flexDirection: 'row',
                    justifyContent: 'space-between',
                    paddingHorizontal: 30,
                    paddingTop: 50,
                    paddingBottom: 20
                }}>
                    <TouchableOpacity style={{
                        backgroundColor: 'rgba(0, 0, 0, 0.5)',
                        padding: 12,
                        borderRadius: 25,
                        alignItems: 'center',
                        justifyContent: 'center'
                    }} onPress={toggleFlash}>
                        <Ionicons
                            name={flash === 'on' ? 'flash' : 'flash-off'}
                            size={24}
                            color="white"
                        />
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

                {/* Bottom Controls */}
                <View style={{
                    position: 'absolute',
                    bottom: 0,
                    left: 0,
                    right: 0,
                    paddingBottom: 40,
                    alignItems: 'center'
                }}>
                    {/* Timer Options */}
                    <View style={{
                        flexDirection: 'row',
                        marginBottom: 30,
                        backgroundColor: 'rgba(0, 0, 0, 0.5)',
                        borderRadius: 25,
                        padding: 8
                    }}>
                        <TouchableOpacity
                            style={[
                                {
                                    paddingHorizontal: 15,
                                    paddingVertical: 8,
                                    marginHorizontal: 5,
                                    borderRadius: 20
                                },
                                timer === 0 && {backgroundColor: '#007AFF'}
                            ]}
                            onPress={() => setTimerLength(0)}
                        >
                            <Text style={{
                                color: 'white',
                                fontSize: 14,
                                fontWeight: '500'
                            }}>No Timer</Text>
                        </TouchableOpacity>
                        <TouchableOpacity
                            style={[
                                {
                                    paddingHorizontal: 15,
                                    paddingVertical: 8,
                                    marginHorizontal: 5,
                                    borderRadius: 20
                                },
                                timer === 3 && {backgroundColor: '#007AFF',}
                            ]}
                            onPress={() => setTimerLength(3)}
                        >
                            <Text style={{
                                color: 'white',
                                fontSize: 14,
                                fontWeight: '500'
                            }}>3s</Text>
                        </TouchableOpacity>
                        <TouchableOpacity
                            style={[
                                {
                                    paddingHorizontal: 15,
                                    paddingVertical: 8,
                                    marginHorizontal: 5,
                                    borderRadius: 20
                                },
                                timer === 5 && {backgroundColor: '#007AFF',}
                            ]}
                            onPress={() => setTimerLength(5)}
                        >
                            <Text style={{
                                color: 'white',
                                fontSize: 14,
                                fontWeight: '500'
                            }}>5s</Text>
                        </TouchableOpacity>
                        <TouchableOpacity
                            style={[
                                {
                                    paddingHorizontal: 15,
                                    paddingVertical: 8,
                                    marginHorizontal: 5,
                                    borderRadius: 20
                                },
                                timer === 10 && {backgroundColor: '#007AFF',}
                            ]}
                            onPress={() => setTimerLength(10)}
                        >
                            <Text style={{
                                color: 'white',
                                fontSize: 14,
                                fontWeight: '500'
                            }}>10s</Text>
                        </TouchableOpacity>
                    </View>

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
                </View>
            </CameraView>
        </View>
    );
}
