package pl.julkot1.game.map;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

public class Tile {
    // Terrain, temperature, moisture, plant variety, and biome enums
    public enum TerrainType {
        WATER, SAND, LAND, UNKNOWN
    }

    public enum TemperatureType {
        COLD, TEMPERATE, HOT, UNKNOWN
    }

    public enum MoistureType {
        DRY, NORMAL, WET, UNKNOWN
    }

    public enum PlantVarietyType {
        NONE, LOW, NORMAL, HIGH, UNKNOWN
    }

    public enum BiomeType {
        OCEAN, LAKE, BEACH, DESERT, SAVANNA, STEPPE, GRASSLAND, TUNDRA, TAIGA, JUNGLE, FOREST, FLOWER_FIELD, SHRUBLAND, FERN_FOREST, PINE_FOREST, RAINFOREST, DENSE_FOREST, UNKNOWN
    }
    public enum TerrainLevel {
        LEVEL_0, LEVEL_1, LEVEL_2, LEVEL_3, LEVEL_4, UNKNOWN
    }



    private Color color;
    private boolean clicked;
    private float light; // 0 = no extra light, 1 = full extra light

    private TerrainType terrainType;
    private TemperatureType temperatureType;
    private MoistureType moistureType;
    private BiomeType biomeType;
    private TerrainLevel terrainLevel;

    private float temperature;
    private float moisture;
    private float plantVariety;
    private float height;

    private Model model;
    private ModelInstance modelInstance;

    private Model cubeModel;
    private ModelInstance cubeInstance;

    public Tile(Color color) {
        this.color = color;
        this.clicked = false;
        this.light = 0f;
        this.terrainType = TerrainType.UNKNOWN;
        this.temperatureType = TemperatureType.UNKNOWN;
        this.moistureType = MoistureType.UNKNOWN;
        this.biomeType = BiomeType.UNKNOWN;
        this.temperature = 0f;
        this.moisture = 0f;
        this.plantVariety = 0f;
        this.height = 0f;
    }


    public void setTerrainLevel(TerrainLevel terrainLevel) {this.terrainLevel = terrainLevel;}
    public TerrainLevel getTerrainLevel() {return terrainLevel;}

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public boolean isClicked() { return clicked; }
    public void setClicked(boolean clicked) { this.clicked = clicked; }

    public float getLight() { return light; }
    public void setLight(float light) { this.light = light; }

    public TerrainType getTerrainType() { return terrainType; }
    public void setTerrainType(TerrainType terrainType) { this.terrainType = terrainType; }

    public TemperatureType getTemperatureType() { return temperatureType; }
    public void setTemperatureType(TemperatureType temperatureType) { this.temperatureType = temperatureType; }

    public MoistureType getMoistureType() { return moistureType; }
    public void setMoistureType(MoistureType moistureType) { this.moistureType = moistureType; }

    public BiomeType getBiomeType() { return biomeType; }
    public void setBiomeType(BiomeType biomeType) { this.biomeType = biomeType; }

    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }

    public float getMoisture() { return moisture; }
    public void setMoisture(float moisture) { this.moisture = moisture; }

    public float getPlantVariety() { return plantVariety; }
    public void setPlantVariety(float plantVariety) { this.plantVariety = plantVariety; }

    public float getHeight() { return height; }
    public void setHeight() {
        this.height = switch (terrainLevel) {
            case LEVEL_0 -> 0.5f;
            case LEVEL_1 -> 0.75f;
            case LEVEL_2 -> 1.0f;
            case LEVEL_3 -> 1.25f;
            case LEVEL_4 -> 1.5f;
            default -> 0.0f;
        };
    }

    public void setModel(Model model) {
        this.model = model;
        if (model != null) {
            this.modelInstance = new ModelInstance(model);
        } else {
            this.modelInstance = null;
        }
    }

    public Model getModel() {
        return model;
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    public void setModelInstance(ModelInstance instance) {
        this.modelInstance = instance;
    }

    public void setCubeModel(Model cubeModel) {
        this.cubeModel = cubeModel;
        if (cubeModel != null) {
            this.cubeInstance = new ModelInstance(cubeModel);
        } else {
            this.cubeInstance = null;
        }
    }

    public Model getCubeModel() {
        return cubeModel;
    }

    public ModelInstance getCubeInstance() {
        return cubeInstance;
    }

    public void setCubeInstance(ModelInstance instance) {
        this.cubeInstance = instance;
    }
}
