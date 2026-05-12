import React from 'react';
import {StatusBar} from 'expo-status-bar';
import {StyleSheet, View} from 'react-native';
import CameraScreen from './src/components/CameraScreen';

export default function App() {
    return (
        <View style={styles.container}>
            <CameraScreen/>
            <StatusBar style="auto"/>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#000',
    },
});
