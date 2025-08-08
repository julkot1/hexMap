package pl.julkot1.game.map;

import com.badlogic.gdx.graphics.Color;
import java.util.HashMap;
import java.util.Map;

// Joise imports
import com.sudoplay.joise.module.ModuleFractal;
import com.sudoplay.joise.module.ModuleAutoCorrect;
import com.sudoplay.joise.module.ModuleBasisFunction.BasisType;
import com.sudoplay.joise.module.ModuleBasisFunction.InterpolationType;

public class MapGenerator {
    public static int SEED = 1332232137;

    // --- Constants for thresholds ---
    public static final float WATER_LEVEL = 0.45f;
    public static final float SAND_LEVEL = 0.5f;

    // --- Color constants ---
    public static final Map<Tile.BiomeType, Color> BIOME_COLORS = new HashMap<>();
    static {
        BIOME_COLORS.put(Tile.BiomeType.OCEAN, new Color(0.18f, 0.45f, 0.7f, 1f));
        BIOME_COLORS.put(Tile.BiomeType.BEACH, new Color(0.85f, 0.8f, 0.5f, 1f));
        BIOME_COLORS.put(Tile.BiomeType.DESERT, new Color(0.93f, 0.85f, 0.45f, 1f));
        BIOME_COLORS.put(Tile.BiomeType.SAVANNA, new Color(0.7f, 0.7f, 0.2f, 1f));
        BIOME_COLORS.put(Tile.BiomeType.STEPPE, new Color(0.6f, 0.8f, 0.4f, 1f));
        BIOME_COLORS.put(Tile.BiomeType.GRASSLAND, new Color(0.2f, 0.7f, 0.2f, 1f));
        BIOME_COLORS.put(Tile.BiomeType.TUNDRA, new Color(0.8f, 0.85f, 0.7f, 1f));
        BIOME_COLORS.put(Tile.BiomeType.TAIGA, new Color(0.2f, 0.5f, 0.3f, 1f));
        BIOME_COLORS.put(Tile.BiomeType.JUNGLE, new Color(0.0f, 0.45f, 0.13f, 1f));
        BIOME_COLORS.put(Tile.BiomeType.FOREST, new Color(0.1f, 0.5f, 0.1f, 1f));
        BIOME_COLORS.put(Tile.BiomeType.FLOWER_FIELD, new Color(0.7f, 0.9f, 0.5f, 1f));
        BIOME_COLORS.put(Tile.BiomeType.SHRUBLAND, new Color(0.5f, 0.7f, 0.3f, 1f));
        BIOME_COLORS.put(Tile.BiomeType.FERN_FOREST, new Color(0.1f, 0.7f, 0.3f, 1f));
        BIOME_COLORS.put(Tile.BiomeType.PINE_FOREST, new Color(0.15f, 0.35f, 0.18f, 1f));
        BIOME_COLORS.put(Tile.BiomeType.RAINFOREST, new Color(0.0f, 0.35f, 0.18f, 1f));
        BIOME_COLORS.put(Tile.BiomeType.DENSE_FOREST, new Color(0.05f, 0.35f, 0.12f, 1f));
    }


    public static final int BASE_OCTAVES = 4;
    public static final float BASE_FREQ = 0.1f;
    public static final int HUMIDITY_OCTAVES = 4;
    public static final float HUMIDITY_FREQ = 0.018f;
    public static final int TEMP_OCTAVES = 4;
    public static final float TEMP_FREQ = 0.012f;

    public static final int CONTINENT_OCTAVES = 7;
    public static final float CONTINENT_FREQ = 0.023f;
    public static final float CONTINENT_THRESHOLD = 0.7f;

    public static void generateNoiseMap(HexMap map) {

        // --- Continent mask noise ---
        ModuleFractal continentMask = new ModuleFractal();
        continentMask.setSeed(SEED + 5000);
        continentMask.setType(ModuleFractal.FractalType.HYBRIDMULTI);
        continentMask.setAllSourceBasisTypes(BasisType.GRADIENT);
        continentMask.setAllSourceInterpolationTypes(InterpolationType.LINEAR);
        continentMask.setNumOctaves(CONTINENT_OCTAVES);
        continentMask.setFrequency(CONTINENT_FREQ);

        ModuleAutoCorrect continentNorm = new ModuleAutoCorrect();
        continentNorm.setSource(continentMask);
        continentNorm.setSamples(10000);
        continentNorm.calculate();

        // --- Island mask noise for scattered islands ---
        ModuleFractal islandMask = new ModuleFractal();
        islandMask.setSeed(SEED + 9000);
        islandMask.setType(ModuleFractal.FractalType.FBM);
        islandMask.setAllSourceBasisTypes(BasisType.SIMPLEX);
        islandMask.setAllSourceInterpolationTypes(InterpolationType.QUINTIC);
        islandMask.setNumOctaves(3);
        islandMask.setFrequency(0.09f);

        ModuleAutoCorrect islandNorm = new ModuleAutoCorrect();
        islandNorm.setSource(islandMask);
        islandNorm.setSamples(10000);
        islandNorm.calculate();

        // --- Noise modules ---
        ModuleFractal base = new ModuleFractal();
        base.setSeed(SEED);
        base.setType(ModuleFractal.FractalType.FBM);
        base.setAllSourceBasisTypes(BasisType.SIMPLEX);
        base.setAllSourceInterpolationTypes(InterpolationType.QUINTIC);
        base.setNumOctaves(BASE_OCTAVES);
        base.setFrequency(BASE_FREQ);

        ModuleFractal humidity = new ModuleFractal();
        humidity.setSeed(SEED + 1000);
        humidity.setType(ModuleFractal.FractalType.FBM);
        humidity.setAllSourceBasisTypes(BasisType.SIMPLEX);
        humidity.setAllSourceInterpolationTypes(InterpolationType.QUINTIC);
        humidity.setNumOctaves(HUMIDITY_OCTAVES);
        humidity.setFrequency(HUMIDITY_FREQ);

        ModuleFractal temperature = new ModuleFractal();
        temperature.setSeed(SEED + 2000);
        temperature.setType(ModuleFractal.FractalType.FBM);
        temperature.setAllSourceBasisTypes(BasisType.SIMPLEX);
        temperature.setAllSourceInterpolationTypes(InterpolationType.QUINTIC);
        temperature.setNumOctaves(TEMP_OCTAVES);
        temperature.setFrequency(TEMP_FREQ);


        // --- Normalize all noises to [0,1] ---
        ModuleAutoCorrect baseNorm = new ModuleAutoCorrect();
        baseNorm.setSource(base);
        baseNorm.setSamples(10000);
        baseNorm.calculate();

        ModuleAutoCorrect humidityNorm = new ModuleAutoCorrect();
        humidityNorm.setSource(humidity);
        humidityNorm.setSamples(10000);
        humidityNorm.calculate();

        ModuleAutoCorrect tempNorm = new ModuleAutoCorrect();
        tempNorm.setSource(temperature);
        tempNorm.setSamples(10000);
        tempNorm.calculate();


        BiomeResolver biomeResolver = new SimpleBiomeResolver();

        for (int row = 0; row < map.getRows(); row++) {
            for (int col = 0; col < map.getCols(); col++) {

                double continent = continentNorm.get(col, row, 0);
                double island = islandNorm.get(col, row, 0);

                double n = baseNorm.get(col, row, 0);
                double h = humidityNorm.get(col, row, 0);
                double t = tempNorm.get(col, row, 0);

                Tile tile = map.getTile(row, col);

                double terrainValue;
                if (continent > CONTINENT_THRESHOLD) {
                    terrainValue = Math.max(n, continent * 0.6);
                } else if (island > 0.82) {
                    terrainValue = Math.max(n, island * 0.7);
                } else {
                    terrainValue = Math.min(n, continent * 0.2);
                }
                tile.setTerrainLevel(resolveHeight(terrainValue));
                tile.setHeight();
                tile.setTerrainType(resolveTerrainType(terrainValue));
                tile.setTemperatureType(resolveTemperatureType(t));
                tile.setMoistureType(resolveMoistureType(h));

                BiomeResult biomeResult = biomeResolver.resolve(
                    tile.getTerrainType(), tile.getTemperatureType(), tile.getMoistureType(), terrainValue, h, t
                );
                tile.setBiomeType(biomeResult.biomeType);
                tile.setColor(biomeResult.color);

                tile.setTemperature((float)t);
                tile.setMoisture((float)h);
            }
        }
    }


    public interface BiomeResolver {
        BiomeResult resolve(
            Tile.TerrainType terrainType,
            Tile.TemperatureType temperatureType,
            Tile.MoistureType moistureType,
            double n, double h, double t
        );
    }

    public record BiomeResult(Tile.BiomeType biomeType, Color color) {
    }

    // --- SIMPLER BIOME RESOLVER WITH MORE FOREST/JUNGLE/PLANTS ---
    public static class SimpleBiomeResolver implements BiomeResolver {
        @Override
        public BiomeResult resolve(
            Tile.TerrainType terrainType,
            Tile.TemperatureType temperatureType,
            Tile.MoistureType moistureType,
            double n, double h, double t
        ) {

            Tile.BiomeType biomeType = null;

            switch (terrainType) {
                case WATER:
                    biomeType = Tile.BiomeType.OCEAN;
                    break;
                case SAND:
                    biomeType = Tile.BiomeType.DESERT;
                case LAND:
                {
                    switch (temperatureType) {
                        case COLD -> {
                            switch (moistureType) {
                                case DRY -> biomeType = Tile.BiomeType.TUNDRA;
                                case NORMAL -> biomeType = Tile.BiomeType.PINE_FOREST;
                                case WET -> biomeType = Tile.BiomeType.FERN_FOREST;
                                default -> biomeType = Tile.BiomeType.TAIGA;
                            }
                        }
                        case TEMPERATE -> {
                            switch (moistureType) {
                                case DRY -> biomeType = Tile.BiomeType.SHRUBLAND;
                                case NORMAL -> biomeType = Tile.BiomeType.FOREST;
                                case WET -> biomeType = Tile.BiomeType.DENSE_FOREST;
                                default -> biomeType = Tile.BiomeType.FLOWER_FIELD;
                            }
                        }
                        case HOT -> {
                            switch (moistureType) {
                                case DRY -> biomeType = Tile.BiomeType.DESERT;
                                case NORMAL -> biomeType = Tile.BiomeType.SAVANNA;
                                case WET -> biomeType = Tile.BiomeType.JUNGLE;
                                default -> biomeType = Tile.BiomeType.RAINFOREST;
                            }

                        }
                    }
                }

            }

            Color color = BIOME_COLORS.getOrDefault(biomeType, Color.GRAY);
            return new BiomeResult(biomeType, color);
        }
    }

    // --- Helper methods for modularity ---

    private static Tile.TerrainType resolveTerrainType(double n) {
        if (n < WATER_LEVEL) return Tile.TerrainType.WATER;
        if (n < SAND_LEVEL) return Tile.TerrainType.SAND;
        return Tile.TerrainType.LAND;
    }
    private static Tile.TerrainLevel resolveHeight(double n) {
        if (n < WATER_LEVEL) return Tile.TerrainLevel.LEVEL_0;
        if (n < SAND_LEVEL) return Tile.TerrainLevel.LEVEL_1;
        if (n  < 0.6) return Tile.TerrainLevel.LEVEL_2;
        if (n < 0.75) return Tile.TerrainLevel.LEVEL_3;
        return Tile.TerrainLevel.LEVEL_4;
    }
    private static Tile.TemperatureType resolveTemperatureType(double t) {
        if (t < 0.28) return Tile.TemperatureType.COLD;
        if (t > 0.72) return Tile.TemperatureType.HOT;
        return Tile.TemperatureType.TEMPERATE;
    }

    private static Tile.MoistureType resolveMoistureType(double h) {
        if (h < 0.28) return Tile.MoistureType.DRY;
        if (h > 0.72) return Tile.MoistureType.WET;
        return Tile.MoistureType.NORMAL;
    }

}
