/**
 * Each one of the media items we show to the user in the GalleryScreen.
 */
export type GalleryItem = {
    id: string;
    uri: string;
    width?: number;
    height?: number;
    creationTime?: number;
};