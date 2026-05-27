import 'react-native-gesture-handler';
import React from 'react';
import {StatusBar} from 'expo-status-bar';
import {View} from 'react-native';
import {NavigationContainer} from '@react-navigation/native';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {CameraScreen} from './src/CameraScreen';
import {GalleryScreen} from './src/GalleryScreen';

export type RootStackParamList = {
    Camera: undefined;
    Gallery: undefined;
};

const Stack = createNativeStackNavigator<RootStackParamList>();

export default function App() {
    return (
        <View style={{
            flex: 1,
            backgroundColor: '#000'
        }}>
            <NavigationContainer>
                <Stack.Navigator initialRouteName="Camera">
                    <Stack.Screen
                        name="Camera"
                        component={CameraScreen}
                        options={{headerShown: false}}
                    />
                    <Stack.Screen
                        name="Gallery"
                        component={GalleryScreen}
                        options={{title: 'Gallery'}}
                    />
                </Stack.Navigator>
            </NavigationContainer>
            <StatusBar style="auto"/>
        </View>
    );
}
