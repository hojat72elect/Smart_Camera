import React, {useRef, useState} from 'react';
import {ScrollView, Text, TouchableOpacity, View} from 'react-native';

const CATEGORIES = ["Panorama", "Photo", "Video", "Beautify"];

export function AutoScrollingTabs() {
    const scrollViewRef = useRef<ScrollView>(null);
    const itemRefs = useRef<{ [key: string]: View | null }>({});
    const [activeTab, setActiveTab] = useState(CATEGORIES[0]);

    const handleTabPress = (tabName: string) => {
        // Execute the onClick functionality
        setActiveTab(tabName);
        console.log(`Executing onClick action for: ${tabName}`);

        // Animate scroll to the clicked item
        const selectedItem = itemRefs.current[tabName];
        if (selectedItem && scrollViewRef.current) {
            // measureLayout calculates coordinates relative to the ScrollView node
            selectedItem.measureLayout(
                scrollViewRef.current,
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

    return (
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
    );
}