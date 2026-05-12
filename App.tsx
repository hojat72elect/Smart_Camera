import React from 'react';
import {StatusBar} from 'expo-status-bar';
import {View} from 'react-native';
import CameraScreen from './src/components/CameraScreen';

export default function App() {
    return (
        <View style={{
            flex: 1,
            backgroundColor: '#000'
        }}>
            <CameraScreen/>
            <StatusBar style="auto"/>
        </View>
    );
}
