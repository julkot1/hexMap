package pl.julkot1.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.VertexAttributes;

import java.util.HashMap;
import java.util.Map;

public class ModelManager {
    private static ModelManager instance;
    private final Map<String, Model> modelCache = new HashMap<>();

    private ModelManager() {}

    public static ModelManager get() {
        if (instance == null) instance = new ModelManager();
        return instance;
    }

    public Model getHexModel(float radius, float height) {
        String key = "hex_" + radius + "_" + height;
        if (!modelCache.containsKey(key)) {
            ModelBuilder builder = new ModelBuilder();
            Model model = builder.createCylinder(
                radius * 2, height, radius * 2, 6,
                new Material(ColorAttribute.createDiffuse(Color.WHITE)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
            );
            modelCache.put(key, model);
        }
        return modelCache.get(key);
    }

    public Model getCubeModel(float size, Color color) {
        String key = "cube_" + size + "_" + color.toString();
        if (!modelCache.containsKey(key)) {
            ModelBuilder builder = new ModelBuilder();
            Model model = builder.createBox(
                size, size, size,
                new Material(
                    ColorAttribute.createDiffuse(color),
                    FloatAttribute.createShininess(16f)
                ),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
            );
            modelCache.put(key, model);
        }
        return modelCache.get(key);
    }

    public void dispose() {
        for (Model m : modelCache.values()) {
            m.dispose();
        }
        modelCache.clear();
    }
}
