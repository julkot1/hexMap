package pl.julkot1.game.map;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import pl.julkot1.game.Screen3D;

public class MapRender {
    private static final float renderRadius = 44f;

    public static void render(
            ModelBatch modelBatch,
            PerspectiveCamera camera,
            Environment environment,
            HexMap hexMap
    ) {
        Vector3 camPos = camera.position;
        float hexWidth = Screen3D.HEX_RADIUS * 2f;
        float hexHeight = (float)Math.sqrt(3) * Screen3D.HEX_RADIUS;
        int camTileX = Math.round(camPos.x / (Screen3D.HEX_RADIUS * 1.5f));
        int camTileY = Math.round(camPos.z / hexHeight);
        int tileRenderRadius = Math.max(8, (int)(renderRadius / (Screen3D.HEX_RADIUS * 1.5f)) + 2);

        // Compute visible bounds for culling
        int minX = Math.max(0, camTileX - tileRenderRadius);
        int maxX = Math.min(Screen3D.MAP_WIDTH, camTileX + tileRenderRadius);
        int minY = Math.max(0, camTileY - tileRenderRadius);
        int maxY = Math.min(Screen3D.MAP_HEIGHT, camTileY + tileRenderRadius);

        modelBatch.begin(camera);
        for (int x = minX; x < maxX; x++) {
            // Precompute px for this column
            float px = x * Screen3D.HEX_RADIUS * 1.5f;
            float yOffset = (x % 2) * hexHeight / 2f;
            for (int y = minY; y < maxY; y++) {
                float py = y * hexHeight + yOffset;
                // Fast AABB check before expensive distance check
                if (Math.abs(px - camPos.x) > renderRadius + hexWidth || Math.abs(py - camPos.z) > renderRadius + hexHeight)
                    continue;
                float dist = camPos.dst(px, 0, py);
                if (dist < renderRadius) {
                    Tile tile = hexMap.getTile(x, y);
                    ModelInstance instance = tile.getModelInstance();
                    if (instance != null) {

                            modelBatch.render(instance, environment);
                    }
                    ModelInstance cubeInstance = tile.getCubeInstance();
                    if (cubeInstance != null) {
                        modelBatch.render(cubeInstance, environment);
                    }
                }
            }
        }
        modelBatch.end();
    }
}
