package pl.julkot1.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.collision.BoundingBox;
import pl.julkot1.game.gui.Gui;
import pl.julkot1.game.map.HexMap;
import pl.julkot1.game.map.MapGenerator;
import pl.julkot1.game.map.MapRender;
import pl.julkot1.game.map.Tile;

public class Screen3D implements Screen {
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private Model hexModel;
    private Model cubeModel;

    public static final int MAP_WIDTH = 100;
    public static final int MAP_HEIGHT = 100;
    public static final float HEX_RADIUS = 1f;
    public static final float HEX_HEIGHT = 0.5f;

    private float cameraAngle = 0f;
    private float cameraDistance = 8f;
    private final Vector3 mapCenter = new Vector3(MAP_WIDTH * HEX_RADIUS * 0.75f, 0, MAP_HEIGHT * HEX_RADIUS * (float)Math.sqrt(3) / 2f);

    private int lastMouseX;
    private boolean dragging = false;

    private final Vector3 cameraOffset = new Vector3(0, 0, 0);

    private HexMap hexMap;

    private Gui gui;

    private boolean wasLeftPressed = false;

    private float timeOfDay = 0f;
    private static final float DAY_LENGTH = 20f;
    private DirectionalLight sunLight;

    private int hoveredX = -1, hoveredY = -1;
    private Tile hoveredTile = null;

    @Override
    public void show() {
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(10, 15, 20);
        camera.lookAt(10, 0, 5);
        camera.near = 5f;
        camera.far = 100f;
        camera.update();

        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));

        // Setup shadow light
        // Add this field
        DirectionalShadowLight shadowLight = new DirectionalShadowLight(2048, 2048, 1f, 1f, 100f, 100f);

        environment.add( shadowLight.set(1f, 1f, 1f, -1f, -0.8f, -0.2f));
        environment.shadowMap = shadowLight;
        sunLight = shadowLight;

        hexMap = new HexMap(MAP_WIDTH, MAP_HEIGHT, HexMap.Colors.randomColors());
        MapGenerator.generateNoiseMap(hexMap);

        ModelBuilder modelBuilder = new ModelBuilder();
        hexModel = modelBuilder.createCylinder(
                HEX_RADIUS * 2, HEX_HEIGHT, HEX_RADIUS * 2, 6,
                new Material(ColorAttribute.createDiffuse(Color.WHITE)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );

        cubeModel = modelBuilder.createCylinder(
                HEX_RADIUS, HEX_RADIUS, HEX_RADIUS, 12,
                new Material( ColorAttribute.createDiffuse(new Color(1f, 0.5f, 0.2f, 0.3f)),
                    ColorAttribute.createEmissive(Color.BLUE),
                    FloatAttribute.createShininess(16f)),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );

        updateMapModel();

        gui = new Gui(MAP_WIDTH * MAP_HEIGHT);
        gui.updateMinimap(hexMap);

        gui.getMinimap().setMinimapClickListener((tileX, tileY) -> {
            float px = tileX * HEX_RADIUS * 1.5f;
            float py = tileY * HEX_RADIUS * (float) Math.sqrt(3) + (tileX % 2) * HEX_RADIUS * (float) Math.sqrt(3) / 2f;

            cameraOffset.x = px - mapCenter.x;
            cameraOffset.z = py - mapCenter.z;
        });

        gui.setOnButtonClicked(() -> {
            MapGenerator.generateNoiseMap(hexMap);
            gui.updateMinimap(hexMap);

            updateMapModel();
        });


    }

    private void updateMapModel() {
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                float px = x * HEX_RADIUS * 1.5f;
                float py = y * HEX_RADIUS * (float) Math.sqrt(3) + (x % 2) * HEX_RADIUS * (float) Math.sqrt(3) / 2f;
                Tile tile = hexMap.getTile(x, y);
                tile.setModel(hexModel);
                ModelInstance instance = tile.getModelInstance();

                instance.transform.setToTranslation(px, tile.getHeight() / 2f, py);
                instance.transform.scale(1f, tile.getHeight() / HEX_HEIGHT, 1f);

                // Set color based on tile type
                Color renderColor = tile.getColor();
                instance.materials.get(0).set(ColorAttribute.createDiffuse(renderColor));

                // Remove any spawned cube on map regen
                tile.setCubeModel(null);
            }
        }
    }

    @Override
    public void render(float delta) {
        updateDayNightCycle(delta);

        handleCameraInput(delta);
        handleMouseInput();

        updateHoveredTile();

        updateCamera();

        float camMapX = camera.position.x / (HEX_RADIUS * 1.5f);
        float camMapY = camera.position.z / (HEX_RADIUS * (float)Math.sqrt(3));
        gui.getMinimap().setCameraView(camMapX, camMapY);

        gui.setFps(Gdx.graphics.getFramesPerSecond());
        gui.setCameraPosition(camera.position.x, camera.position.z);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        camera.update();

        MapRender.render(
            modelBatch,
            camera,
            environment,
            hexMap
        );

        handleTileClick();

        gui.render();
    }

    private void updateDayNightCycle(float delta) {
        timeOfDay += (24f / DAY_LENGTH) * delta;
        if (timeOfDay > 24f) timeOfDay -= 24f;
    }



    private void handleTileClick() {
        boolean leftPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        if (!leftPressed && wasLeftPressed) {
            if (gui.getStage().hit(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), true) != null) {
                wasLeftPressed = false;
                return;
            }

            int mouseX = Gdx.input.getX();
            int mouseY = Gdx.input.getY();

            Ray ray = camera.getPickRay(mouseX, mouseY);

            float minDist = Float.MAX_VALUE;
            int selectedX = -1, selectedY = -1;

            for (int x = 0; x < MAP_WIDTH; x++) {
                for (int y = 0; y < MAP_HEIGHT; y++) {
                    float px = x * HEX_RADIUS * 1.5f;
                    float py = y * HEX_RADIUS * (float)Math.sqrt(3) + (x % 2) * HEX_RADIUS * (float)Math.sqrt(3) / 2f;
                    float height = hexMap.getHeight(x, y);

                    BoundingBox bbox = new BoundingBox(
                        new Vector3(px - HEX_RADIUS, 0, py - HEX_RADIUS),
                        new Vector3(px + HEX_RADIUS, height, py + HEX_RADIUS)
                    );

                    if (Intersector.intersectRayBoundsFast(ray, bbox)) {
                        float dist = ray.origin.dst(px, 0, py);
                        if (dist < minDist) {
                            minDist = dist;
                            selectedX = x;
                            selectedY = y;
                        }
                    }
                }
            }

            if (selectedX != -1 && selectedY != -1) {
                Tile tile = hexMap.getTile(selectedX, selectedY);
                gui.getTileWindow().setTileInfo(tile, selectedX, selectedY);
                gui.getTileWindow().setVisible(true);

                if (tile.getCubeInstance() != null) {
                    tile.setCubeModel(null);
                } else {
                    tile.setCubeModel(cubeModel);
                    ModelInstance cubeInstance = tile.getCubeInstance();
                    if (cubeInstance != null) {
                        float px = selectedX * HEX_RADIUS * 1.5f;
                        float py = selectedY * HEX_RADIUS * (float)Math.sqrt(3) + (selectedX % 2) * HEX_RADIUS * (float)Math.sqrt(3) / 2f;
                        float tileTop = tile.getHeight();
                        cubeInstance.transform.setToTranslation(px, tileTop + HEX_RADIUS / 2f, py);
                    }
                }
            }
        }
        Gdx.input.setInputProcessor(gui.getStage());
        wasLeftPressed = leftPressed;
    }

    private void handleCameraInput(float delta) {
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

        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.PLUS) || Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.EQUALS)) {
            cameraDistance -= zoomSpeed;
            if (cameraDistance < 5f) cameraDistance = 5f;
        }
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.MINUS)) {
            cameraDistance += zoomSpeed;
            if (cameraDistance > 50f) cameraDistance = 50f;
        }
    }

    private void handleMouseInput() {
        if (Gdx.input.isButtonPressed(com.badlogic.gdx.Input.Buttons.LEFT)) {
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

    private void updateCamera() {
        float rad = (float)Math.toRadians(cameraAngle);
        float x = mapCenter.x + cameraDistance * (float)Math.sin(rad) + cameraOffset.x;
        float z = mapCenter.z + cameraDistance * (float)Math.cos(rad) + cameraOffset.z;
        float y = 10f + cameraDistance * 0.2f + cameraOffset.y;
        camera.position.set(x, y, z);
        camera.lookAt(mapCenter.x + cameraOffset.x, 0 + cameraOffset.y, mapCenter.z + cameraOffset.z);
        camera.up.set(Vector3.Y);
    }

    private void updateHoveredTile() {
        hoveredX = -1;
        hoveredY = -1;
        if (gui.getStage().hit(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY(), true) != null) {
            return;
        }
        Ray ray = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY());
        final float[] minDist = {Float.MAX_VALUE};
        hexMap.iterate((x, y, tile) -> {;
            float px = x * HEX_RADIUS * 1.5f;
            float py = y * HEX_RADIUS * (float)Math.sqrt(3) + (x % 2) * HEX_RADIUS * (float)Math.sqrt(3) / 2f;
            float height = tile.getHeight();

            BoundingBox bbox = new BoundingBox(
                new Vector3(px - HEX_RADIUS, 0, py - HEX_RADIUS),
                new Vector3(px + HEX_RADIUS, height, py + HEX_RADIUS)
            );

            if (Intersector.intersectRayBoundsFast(ray, bbox)) {
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

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        if (gui != null) gui.resize(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        modelBatch.dispose();
        hexModel.dispose();
        cubeModel.dispose();
        if (gui != null) gui.dispose();
    }

}
