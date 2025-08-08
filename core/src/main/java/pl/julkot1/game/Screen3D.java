package pl.julkot1.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
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
    public static final int MAP_WIDTH = 100;
    public static final int MAP_HEIGHT = 100;
    public static final float HEX_RADIUS = 1f;
    public static final float HEX_HEIGHT = 0.5f;

    private int lastMouseX;
    private boolean dragging = false;

    private final Vector3 cameraOffset = new Vector3(0, 0, 0);

    private HexMap hexMap;

    private Gui gui;

    private boolean wasLeftPressed = false;

    private float timeOfDay = 0f;
    private static final float DAY_LENGTH = 20f;

    private int hoveredX = -1, hoveredY = -1;
    private Tile hoveredTile = null;

    @Override
    public void show() {
        MapRender.init(
            MAP_WIDTH, MAP_HEIGHT, HEX_RADIUS, HEX_HEIGHT,
            null
        );

        hexMap = new HexMap(MAP_WIDTH, MAP_HEIGHT, HexMap.Colors.randomColors());
        MapGenerator.generateNoiseMap(hexMap);

        Model hexModel = ModelManager.get().getHexModel(HEX_RADIUS, HEX_HEIGHT);
        Model cubeModel = ModelManager.get().getCubeModel(HEX_RADIUS, new Color(0.24f, 0.24f, 0.4f, 1f));

        updateMapModel(hexModel, cubeModel);

        gui = new Gui(MAP_WIDTH * MAP_HEIGHT);
        gui.updateMinimap(hexMap);

        gui.getMinimap().setMinimapClickListener((tileX, tileY) -> {
            float px = tileX * HEX_RADIUS * 1.5f;
            float py = tileY * HEX_RADIUS * (float) Math.sqrt(3) + (tileX % 2) * HEX_RADIUS * (float) Math.sqrt(3) / 2f;

            cameraOffset.x = px - MapRender.getMapCenter().x;
            cameraOffset.z = py - MapRender.getMapCenter().z;
        });

        gui.setOnButtonClicked(() -> {
            MapGenerator.generateNoiseMap(hexMap);
            gui.updateMinimap(hexMap);

            updateMapModel(hexModel, cubeModel);
        });
    }

    private void updateMapModel(Model hexModel, Model cubeModel) {
        for (int x = 0; x < MAP_WIDTH; x++) {
            for (int y = 0; y < MAP_HEIGHT; y++) {
                float px = x * HEX_RADIUS * 1.5f;
                float py = y * HEX_RADIUS * (float) Math.sqrt(3) + (x % 2) * HEX_RADIUS * (float) Math.sqrt(3) / 2f;
                Tile tile = hexMap.getTile(x, y);
                tile.setModel(hexModel);
                ModelInstance instance = tile.getModelInstance();

                instance.transform.setToTranslation(px, tile.getHeight() / 2f, py);
                instance.transform.scale(1f, tile.getHeight() / HEX_HEIGHT, 1f);

                Color renderColor = tile.getColor();
                instance.materials.get(0).set(ColorAttribute.createDiffuse(renderColor));

                tile.setCubeModel(null);
            }
        }
    }

    @Override
    public void render(float delta) {
        InputManager.get().update();

        updateDayNightCycle(delta);

        MapRender.handleCameraInput(delta, cameraOffset);

        handleMouseInput();

        MapRender.updateHoveredTile(gui, hexMap, HEX_RADIUS);

        updateCamera();

        PerspectiveCamera camera = MapRender.getCamera();

        float camMapX = camera.position.x / (HEX_RADIUS * 1.5f);
        float camMapY = camera.position.z / (HEX_RADIUS * (float)Math.sqrt(3));
        gui.getMinimap().setCameraView(camMapX, camMapY);

        gui.setFps(Gdx.graphics.getFramesPerSecond());
        gui.setCameraPosition(camera.position.x, camera.position.z);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        camera.update();

        MapRender.render(hexMap);

        handleTileClick();

        gui.render();
    }

    private void updateDayNightCycle(float delta) {
        timeOfDay += (24f / DAY_LENGTH) * delta;
        if (timeOfDay > 24f) timeOfDay -= 24f;
        MapRender.setTimeOfDay(timeOfDay);
    }

    private void handleTileClick() {
        InputManager input = InputManager.get();
        boolean leftPressed = input.isButtonPressed(Input.Buttons.LEFT);
        if (!leftPressed && wasLeftPressed) {
            if (gui.getStage().hit(input.getMouseX(), Gdx.graphics.getHeight() - input.getMouseY(), true) != null) {
                wasLeftPressed = false;
                return;
            }

            int mouseX = input.getMouseX();
            int mouseY = input.getMouseY();

            Ray ray = MapRender.getCamera().getPickRay(mouseX, mouseY);

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
                    tile.setCubeModel(ModelManager.get().getCubeModel(HEX_RADIUS, new Color(0.24f, 0.24f, 0.4f, 1f)));
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

    private void handleMouseInput() {
        InputManager input = InputManager.get();
        if (input.isButtonPressed(Input.Buttons.LEFT)) {
            if (!input.isDragging()) {
                input.startDrag();
            } else {
                int dx = input.getMouseX() - input.getLastMouseX();
                MapRender.adjustCameraAngle(-dx * 0.3f);
                input.setLastMouseX(input.getMouseX());
            }
        } else {
            input.stopDrag();
        }
    }

    private void updateCamera() {
        MapRender.setCameraOffset(cameraOffset);
    }

    @Override
    public void resize(int width, int height) {
        MapRender.resize(width, height);
        if (gui != null) gui.resize(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        MapRender.dispose();
        if (gui != null) gui.dispose();
        ModelManager.get().dispose();
    }

}
