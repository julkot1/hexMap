package pl.julkot1.game.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import pl.julkot1.game.gui.Gui;

public class MapRender {
    private static final float renderRadius = 44f;

    private static PerspectiveCamera camera;
    private static Environment environment;
    private static ModelBatch modelBatch;
    private static Vector3 mapCenter;
    private static int mapWidth, mapHeight;
    private static float hexRadius, hexHeight;

    private static int lastMouseX;
    private static boolean dragging = false;
    private static int hoveredX = -1, hoveredY = -1;
    private static Tile hoveredTile = null;

    // Store camera angle and distance here
    private static float cameraAngle = 0f;
    private static float cameraDistance = 8f;

    public static float getCameraAngle() { return cameraAngle; }
    public static float getCameraDistance() { return cameraDistance; }
    public static void setCameraAngle(float angle) { cameraAngle = angle; }
    public static void setCameraDistance(float dist) { cameraDistance = dist; }

    public static void initCameraAndEnvironment(int width, int height, float hexRad, float hexH, Vector3 center) {
        mapWidth = width;
        mapHeight = height;
        hexRadius = hexRad;
        hexHeight = hexH;
        mapCenter = new Vector3(center);

        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10, 15, 20);
        camera.lookAt(10, 0, 5);
        camera.near = 5f;
        camera.far = 100f;
        camera.update();

        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(
                ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
        // Add a sun-like directional light
        environment.add(new DirectionalLight().set(1f, 1f, 0.95f, -0.7f, -1f, -0.5f));
    }

    public static PerspectiveCamera getCamera() {
        return camera;
    }

    public static Environment getEnvironment() {
        return environment;
    }

    public static ModelBatch getModelBatch() {
        return modelBatch;
    }

    public static void updateCamera(Vector3 cameraOffset) {
        float rad = (float)Math.toRadians(cameraAngle);
        float x = mapCenter.x + cameraDistance * (float)Math.sin(rad) + cameraOffset.x;
        float z = mapCenter.z + cameraDistance * (float)Math.cos(rad) + cameraOffset.z;
        float y = 10f + cameraDistance * 0.2f + cameraOffset.y;
        camera.position.set(x, y, z);
        camera.lookAt(mapCenter.x + cameraOffset.x, 0 + cameraOffset.y, mapCenter.z + cameraOffset.z);
        camera.up.set(Vector3.Y);
        camera.update();
    }

    public static void preRender() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
    }

    public static void render(HexMap hexMap, Gui gui, float hexRadius, Vector3 cameraOffset) {
        // Optionally update camera here if needed (already done in updateCamera)
        Vector3 camPos = camera.position;
        float hexWidth = hexRadius * 2f;
        float hexH = (float)Math.sqrt(3) * hexRadius;
        int camTileX = Math.round(camPos.x / (hexRadius * 1.5f));
        int camTileY = Math.round(camPos.z / hexH);
        int tileRenderRadius = Math.max(8, (int)(renderRadius / (hexRadius * 1.5f)) + 2);

        int minX = Math.max(0, camTileX - tileRenderRadius);
        int maxX = Math.min(mapWidth, camTileX + tileRenderRadius);
        int minY = Math.max(0, camTileY - tileRenderRadius);
        int maxY = Math.min(mapHeight, camTileY + tileRenderRadius);

        modelBatch.begin(camera);
        for (int x = minX; x < maxX; x++) {
            float px = x * hexRadius * 1.5f;
            float yOffset = (x % 2) * hexH / 2f;
            for (int y = minY; y < maxY; y++) {
                float py = y * hexH + yOffset;
                if (Math.abs(px - camPos.x) > renderRadius + hexWidth || Math.abs(py - camPos.z) > renderRadius + hexH)
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

    public static void handleCameraInput(Vector3 cameraOffset, float delta) {
        float panSpeed = 10f * delta;
        float zoomSpeed = 10f * delta;

        Vector3 forward = new Vector3(
            (float)Math.sin(Math.toRadians(cameraAngle)),
            0,
            (float)Math.cos(Math.toRadians(cameraAngle))
        ).nor();
        Vector3 right = new Vector3(forward.z, 0, -forward.x).nor();

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            cameraOffset.sub(forward.x * panSpeed, 0, forward.z * panSpeed);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            cameraOffset.add(forward.x * panSpeed, 0, forward.z * panSpeed);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            cameraOffset.sub(right.x * panSpeed, 0, right.z * panSpeed);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            cameraOffset.add(right.x * panSpeed, 0, right.z * panSpeed);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.PLUS) || Gdx.input.isKeyPressed(Input.Keys.EQUALS)) {
            cameraDistance -= zoomSpeed;
            if (cameraDistance < 5f) cameraDistance = 5f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) {
            cameraDistance += zoomSpeed;
            if (cameraDistance > 50f) cameraDistance = 50f;
        }
    }

    public static void handleMouseInput() {
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            if (!dragging) {
                lastMouseX = Gdx.input.getX();
                dragging = true;
            } else {
                int dx = Gdx.input.getX() - lastMouseX;
                cameraAngle -= dx * 0.3f;
                lastMouseX = Gdx.input.getX();
            }
        } else {
            dragging = false;
        }
    }

    public static void updateHoveredTile(Gui gui, HexMap hexMap, float HEX_RADIUS) {
        hoveredX = -1;
        hoveredY = -1;
        if (gui.getStage().hit(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), true) != null) {
            return;
        }
        Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        final float[] minDist = {Float.MAX_VALUE};
        hexMap.iterate((x, y, tile) -> {
            float px = x * HEX_RADIUS * 1.5f;
            float py = y * HEX_RADIUS * (float)Math.sqrt(3) + (x % 2) * HEX_RADIUS * (float)Math.sqrt(3) / 2f;
            float height = tile.getHeight();

            BoundingBox bbox = new BoundingBox(
                new Vector3(px - HEX_RADIUS, 0, py - HEX_RADIUS),
                new Vector3(px + HEX_RADIUS, height, py + HEX_RADIUS)
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
                    Color hoverColor = new Color(orig.color).lerp(Color.RED, 0.5f);
                    t.getModelInstance().materials.get(0).set(ColorAttribute.createDiffuse(hoverColor));
                    hoveredTile = t;
                }
            }
        });
    }
}
