# Toll Free Traveler - OpenStreetMap Navigation App

A privacy-focused navigation app that helps you avoid tolls and paid highway sections while ensuring you're not stuck on mud roads. Perfect for scenic travel through small towns and untolled highway sections!

**NO Google dependencies** - Uses OpenStreetMap and OpenRouteService for complete privacy and GrapheneOS compatibility.

## Features

- **Toll-Free Routing**: Automatically avoids toll roads and paid highway sections
- **Smart Road Selection**: Prefers secondary roads but ensures paved surfaces (no mud roads!)
- **Scenic Route Mode**: Takes you through small towns and interesting routes
- **Flexible Options**: Toggle between toll-free mode and fastest route as needed
- **Road Quality Filter**: Ensures minimum road quality standards
- **Privacy-First**: No Google services, no tracking
- **GrapheneOS Ready**: Works perfectly without Google Play Services

## Tech Stack

- **Language**: Kotlin
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Maps**: OSMDroid (OpenStreetMap renderer)
- **Routing**: OpenRouteService API (free tier available)
- **HTTP Client**: OkHttp
- **JSON Parsing**: Gson

## Project Structure

```
tollfree-traveler/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/tollfreetraveler/
│   │   │   ├── MainActivity.kt              # Main UI with OSM map
│   │   │   ├── data/
│   │   │   │   └── RouteModels.kt           # Data classes for routes
│   │   │   └── routing/
│   │   │       └── TollFreeRouteCalculator.kt  # ORS API integration
│   │   ├── res/
│   │   │   ├── layout/activity_main.xml     # Main screen with MapView
│   │   │   └── values/                      # Colors, strings, themes
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## Setup Instructions

### 1. Get FREE OpenRouteService API Key

1. Go to [openrouteservice.org](https://openrouteservice.org/)
2. Sign up for a free account
3. Get your API key from the dashboard
4. The free tier includes 2,000 requests/day - plenty for personal use!

### 2. Open in Android Studio

1. Install Android Studio from [developer.android.com](https://developer.android.com/studio)
2. Click "Open an Existing Project"
3. Select the `tollfree-traveler` folder
4. Wait for Gradle sync to complete

### 3. Build & Run

```bash
cd tollfree-traveler
./gradlew assembleDebug
```

Or simply click the green **Play** button in Android Studio to run on your device/emulator.

### 4. Using the App

1. **Tap once** on the map to set your starting point
2. **Tap again** to set your destination
3. The app will calculate a **toll-free route** avoiding paid roads
4. Toggle the button to switch between toll-free and fastest modes
5. Tap again to start a new route

## How It Works

### OpenStreetMap (OSM)
- Community-maintained map data
- Completely free and open source
- Detailed road information including surface types and toll tags

### OpenRouteService (ORS)
- Free routing API using OSM data
- Supports `avoid_features=tollways` parameter
- Provides turn-by-turn directions
- Returns GeoJSON route geometry

### Road Quality Assurance
The app filters routes to ensure:
- ✅ Paved surfaces minimum
- ✅ No mud/unpaved roads (unless no alternative)
- ✅ Secondary roads preferred for scenic travel
- ✅ Small town routes over boring highways

## GrapheneOS Compatibility

This app is **perfect for GrapheneOS**:
- ❌ NO Google Play Services required
- ❌ NO Google Maps API
- ❌ NO tracking or telemetry
- ✅ Works with sandboxed apps
- ✅ Respects Android permissions
- ✅ All network calls are transparent

## Customization

### Change API Key
Edit `TollFreeRouteCalculator.kt`:
```kotlin
private val apiKey = "YOUR_API_KEY_HERE"
```

### Adjust Routing Preferences
Modify `defaultRoutingOptions`:
```kotlin
val avoidTolls = true              // Avoid toll roads
val preferSecondaryRoads = true    // Prefer scenic routes
val minimumSurfaceQuality = PAVED  // No dirt roads!
```

## Future Enhancements

- [ ] Offline maps support (OSMDroid supports this!)
- [ ] Waypoint planning for road trips
- [ ] Points of interest along scenic routes
- [ ] Export routes to GPX
- [ ] Voice navigation
- [ ] Real-time traffic avoidance (optional)
- [ ] Self-hosted OSRM/GraphHopper support

## Troubleshooting

**No route appearing?**
- Check internet connection
- Verify API key is valid
- Check LogCat for error messages

**App crashes on startup?**
- Ensure you have Android 8.0 or higher
- Grant location permissions when prompted

**Routes going through tolls?**
- The API might not have perfect toll data for all regions
- Report issues to OpenStreetMap contributors

## Privacy Policy

This app:
- Does NOT collect any personal data
- Does NOT track your location history
- Does NOT send analytics
- Only makes routing API calls when you request a route
- Stores nothing persistently except app preferences

## License

MIT License - Feel free to use, modify, and distribute!

## Contributing

Contributions welcome! Areas for improvement:
- Better road surface detection from OSM tags
- Integration with alternative routing engines
- UI/UX improvements
- Testing on different devices/ROMs

---

**Remember**: The journey IS the destination! 🗺️🚗

Enjoy toll-free scenic adventures through small towns and countryside!
