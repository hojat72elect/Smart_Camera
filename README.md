# Smart Camera App

A feature-rich React Native camera app built with Expo SDK.

## Features

1. **Camera Live Preview** - Real-time camera feed display
2. **Image Capture** - Take photos with high quality
3. **Camera Switching** - Toggle between front and rear cameras
4. **Save to Device** - Automatically save captured photos to device gallery
5. **Flash Control** - Toggle flash on/off for image capture

## Installation

1. Install dependencies:
```bash
bun install
```

2. Start the development server:
```bash
bun run start
```

3. Run the app (right now we only support Android):

# For Android
bun run android

## Permissions

This app requires the following permissions:
- Camera access
- Photo library access (for saving images)

These permissions are automatically requested when you first use the camera.

## TechStack

- Built with React Native and Expo SDK
- Uses `expo-camera` for camera functionality
- Uses `expo-media-library` for saving photos
- TypeScript for type safety
- Responsive design for different screen sizes

## License

MIT License
