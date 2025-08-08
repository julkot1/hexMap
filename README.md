# Hex 3D Map Generator

A procedural 3D hexagonal world generator and viewer built with [libGDX](https://libgdx.com/).  
Features continent and island generation, biomes, day/night cycle, interactive GUI, and tile/object interaction.

## Features

- **Procedural World Generation:**  
  Generates a world map with 4–10 continents, islands, and realistic temperature, moisture, and plant variety using noise functions.
- **3D Hex Map Rendering:**  
  Renders a large hex grid in 3D with per-tile height, color, and type.
- **Biomes:**  
  Biomes are determined by terrain, temperature, moisture, and plant variety (e.g., ocean, desert, tundra, forest, jungle, steppe, etc.).
- **Camera Controls:**  
  - Mouse drag: rotate/orbit camera
  - Scroll: zoom in/out
  - Arrow keys: pan camera (relative to facing)
- **Tile Interaction:**  
  - Click a tile to spawn/despawn a cube on top.
  - Hover highlights tile.
  - Tile info window shows biome, temperature, moisture, plant variety, and more.
- **GUI:**  
  - FPS and camera position display
  - Minimap with camera view and click-to-move
  - Regenerate map button
  - Info panel and tile info window
- **Optimized Rendering:**  
  Only visible tiles are rendered for high performance on large maps.

## Controls

- **Camera:**
  - Mouse drag: Orbit camera around map center
  - Mouse scroll or +/- keys: Zoom in/out
  - Arrow keys: Pan camera (relative to facing)
- **Tile:**
  - Left click: Select tile (spawns/despawns cube, shows info)
  - Hover: Highlights tile
- **GUI:**
  - Click minimap: Move camera to location
  - Regenerate: Generate new world

## Build & Run

### Requirements

- Java 21
- [Gradle](https://gradle.org/) (wrapper included)
- Desktop: LWJGL3 (default)

### Build

```sh
./gradlew build
```

### Run

```sh
./gradlew lwjgl3:run
```

### Jar

```sh
./gradlew lwjgl3:jar
# Run with:
java -jar lwjgl3/build/libs/hex-lwjgl3.jar
```

## Project Structure

- `core/` — Main game logic (map generation, rendering, GUI, etc.)
- `lwjgl3/` — Desktop launcher
- `map/` — Map, tile, and rendering classes
- `gui/` — GUI panels, minimap, and tile info window

## Credits

- [libGDX](https://libgdx.com/)
- [Joise](https://github.com/KdotJPG/Joise) for procedural noise

---

*Created by julkot1*
