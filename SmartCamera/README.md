# Smart Camera App

A feature-rich React Native camera app built with Expo SDK.

## Features

1. **Camera Live Preview** - Real-time camera feed display
2. **Image Capture** - Take photos with high quality
3. **Camera Switching** - Toggle between front and rear cameras
4. **Save to Device** - Automatically save captured photos to device gallery
5. **Flash Control** - Toggle flash on/off for image capture
6. **Timer Function** - Set timer delays (3s, 5s, 10s) for photo capture

## Installation

1. Install dependencies:
```bash
npm install
```

2. Start the development server:
```bash
npm start
```

3. Run on your preferred platform:
```bash
# For iOS
npm run ios

# For Android
npm run android

# For Web (limited camera support)
npm run web
```

## Permissions

This app requires the following permissions:
- Camera access
- Photo library access (for saving images)

These permissions are automatically requested when you first use the camera.

## Usage

1. Grant camera permissions when prompted
2. Use the top controls to:
   - Toggle flash on/off
   - Switch between front and rear cameras
3. Use the timer buttons to set a delay before capture
4. Tap the capture button to take a photo
5. Photos are automatically saved to your device's photo gallery

## Technical Details

- Built with React Native and Expo SDK
- Uses `expo-camera` for camera functionality
- Uses `expo-media-library` for saving photos
- TypeScript for type safety
- Responsive design for different screen sizes

## Development

This project was created using Expo's blank template with TypeScript support.

## License

MIT License
