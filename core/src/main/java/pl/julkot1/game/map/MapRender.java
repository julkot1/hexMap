package pl.julkot1.game.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.math.collision.BoundingBox;
import pl.julkot1.game.gui.Gui;
import com.badlogic.gdx.Input;
import pl.julkot1.game.InputManager;

public class MapRender {
    private static final float RENDER_RADIUS = 44f;

    private static PerspectiveCamera camera;
    private static ModelBatch modelBatch;
    private static Environment environment;
    private static DirectionalShadowLight sunLight;
    private static Vector3 mapCenter;
    private static float cameraAngle = 0f;
    private static float cameraDistance = 8f;
    private static Vector3 cameraOffset = new Vector3(0, 0, 0);
    private static float timeOfDay = 0f;

    private static int hoveredX = -1, hoveredY = -1;
    private static Tile hoveredTile = null;

    private static float hexRadius = 1f;
    private static float hexHeight = 0.5f;
    private static int mapWidth = 0;
    private static int mapHeight = 0;

    public static void init(int mapWidth_, int mapHeight_, float hexRadius_, float hexHeight_, Vector3 center) {
        camera = new PerspectiveCamera(67, 1280, 720);
        camera.position.set(10, 15, 20);
        camera.lookAt(10, 0, 5);
        camera.near = 5f;
        camera.far = 100f;
        camera.update();

        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));

        sunLight = new DirectionalShadowLight(2048, 2048, 1f, 1f, 100f, 100f);
        environment.add(sunLight.set(1f, 1f, 1f, -1f, -0.8f, -0.2f));
        environment.shadowMap = sunLight;

        if (center == null) {
            mapCenter = new Vector3(
                mapWidth_ * hexRadius_ * 0.75f,
                0,
                mapHeight_ * hexRadius_ * (float)Math.sqrt(3) / 2f
            );
        } else {
            mapCenter = new Vector3(center);
        }
        cameraAngle = 0f;
        cameraDistance = 8f;
        cameraOffset = new Vector3(0, 0, 0);
        timeOfDay = 0f;

        hexRadius = hexRadius_;
        hexHeight = hexHeight_;
        mapWidth = mapWidth_;
        mapHeight = mapHeight_;
    }

    public static Vector3 getMapCenter() {
        return mapCenter;
    }

    public static void setCameraOffset(Vector3 offset) {
        cameraOffset.set(offset);
        updateCamera();
    }

    public static void adjustCameraAngle(float delta) {
        cameraAngle += delta;
        updateCamera();
    }

    public static void handleCameraInput(float delta, Vector3 cameraOffset) {
        InputManager input = InputManager.get();
        float panSpeed = 10f * delta;
        float zoomSpeed = 10f * delta;

        Vector3 forward = new Vector3(
            (float)Math.sin(Math.toRadians(cameraAngle)),
            0,
            (float)Math.cos(Math.toRadians(cameraAngle))
        ).nor();
        Vector3 right = new Vector3(forward.z, 0, -forward.x).nor();

        if (input.isKeyPressed(Input.Keys.UP)) {
            cameraOffset.sub(forward.x * panSpeed, 0, forward.z * panSpeed);
        }
        if (input.isKeyPressed(Input.Keys.DOWN)) {
            cameraOffset.add(forward.x * panSpeed, 0, forward.z * panSpeed);
        }
        if (input.isKeyPressed(Input.Keys.LEFT)) {
            cameraOffset.sub(right.x * panSpeed, 0, right.z * panSpeed);
        }
        if (input.isKeyPressed(Input.Keys.RIGHT)) {
            cameraOffset.add(right.x * panSpeed, 0, right.z * panSpeed);
        }

        // Wrap cameraOffset.x and cameraOffset.z to simulate a planet
        float mapWidthWorld = mapWidth * hexRadius * 1.5f;
        float mapHeightWorld = mapHeight * hexRadius * (float)Math.sqrt(3);

        // Wrap horizontally
        cameraOffset.x = wrap(cameraOffset.x, -mapWidthWorld / 2f, mapWidthWorld / 2f);

        // Wrap vertically
        cameraOffset.z = wrap(cameraOffset.z, -mapHeightWorld / 2f, mapHeightWorld / 2f);

        if (input.isKeyPressed(Input.Keys.PLUS) || input.isKeyPressed(Input.Keys.EQUALS)) {
            cameraDistance -= zoomSpeed;
            if (cameraDistance < 1f) cameraDistance = 1f;
        }
        if (input.isKeyPressed(Input.Keys.MINUS)) {
            cameraDistance += zoomSpeed;
            if (cameraDistance > 50f) cameraDistance = 50f;
        }
        updateCamera();
    }

    private static float wrap(float value, float min, float max) {
        float range = max - min;
        while (value < min) value += range;
        while (value > max) value -= range;
        return value;
    }

    private static void updateCamera() {
        float rad = (float)Math.toRadians(cameraAngle);
        float x = mapCenter.x + cameraDistance * (float)Math.sin(rad) + cameraOffset.x;
        float z = mapCenter.z + cameraDistance * (float)Math.cos(rad) + cameraOffset.z;
        float y = 10f + cameraDistance * 0.2f + cameraOffset.y;
        camera.position.set(x, y, z);
        camera.lookAt(mapCenter.x + cameraOffset.x, 0 + cameraOffset.y, mapCenter.z + cameraOffset.z);
        camera.up.set(Vector3.Y);
        camera.update();
    }

    public static PerspectiveCamera getCamera() {
        return camera;
    }

    public static Environment getEnvironment() {
        return environment;
    }

    public static void setTimeOfDay(float tod) {
        timeOfDay = tod;
        // Optionally update sunLight direction/color here for day/night cycle
    }

    public static void render(HexMap hexMap) {
        Vector3 camPos = camera.position;
        float hexW = hexRadius * 2f;
        float hexH = (float) (Math.sqrt(3) * hexRadius);

        // Sphere parameters
        float sphereRadius = Math.max(mapWidth, mapHeight) * hexRadius / (float)Math.PI; // Adjust for visual scale

        modelBatch.begin(camera);
                    if (cubeInstance != null) {
                        modelBatch.render(cubeInstance, environment);
                    }
                }
            }
        }
        modelBatch.end();
    }

    public static void updateHoveredTile(Gui gui, HexMap hexMap, float hexRadius) {
        hoveredX = -1;
        hoveredY = -1;
        if (gui.getStage().hit(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), true) != null) {
            return;
        }
        Ray ray = getCamera().getPickRay(Gdx.input.getX(), Gdx.input.getY());
        final float[] minDist = {Float.MAX_VALUE};
        hexMap.iterate((x, y, tile) -> {
            float px = x * hexRadius * 1.5f;
            float py = y * hexRadius * (float)Math.sqrt(3) + (x % 2) * hexRadius * (float)Math.sqrt(3) / 2f;
            float height = tile.getHeight();

            BoundingBox bbox = new BoundingBox(
                new Vector3(px - hexRadius, 0, py - hexRadius),
                new Vector3(px + hexRadius, height, py + hexRadius)
            );

            if (com.badlogic.gdx.math.Intersector.intersectRayBoundsFast(ray, bbox)) {
                float dist = ray.origin.dst(px, 0, py);
                if (dist < minDist[0]) {
                    if(hoveredTile != null) {
                        hoveredTile.getModelInstance().materials.get(0).set(ColorAttribute.createDiffuse(hoveredTile.getColor()));
                    }
                    minDist[0] = dist;
                    hoveredX = x;
                    hoveredY = y;
                    Tile t = hexMap.getTile(x, y);
                    ColorAttribute orig = (ColorAttribute) t.getModelInstance().materials.get(0).get(ColorAttribute.Diffuse);
                    Color hoverColor = new Color(orig.color).lerp(com.badlogic.gdx.graphics.Color.RED, 0.5f);
                    t.getModelInstance().materials.get(0).set(ColorAttribute.createDiffuse(hoverColor));
                    hoveredTile = t;
                }
            }
        });
    }

    public static void resize(int width, int height) {
        if (camera != null) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }
    }

    public static void dispose() {
        if (modelBatch != null) modelBatch.dispose();
    }
}
