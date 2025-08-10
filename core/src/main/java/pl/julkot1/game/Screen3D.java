package pl.julkot1.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
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
    public static final int MAP_WIDTH = 100;
    public static final int MAP_HEIGHT = 100;
    public static final float HEX_RADIUS = 1f;
    public static final float HEX_HEIGHT = 0.5f;

    private final Vector3 mapCenter = new Vector3(MAP_WIDTH * HEX_RADIUS * 0.75f, 0, MAP_HEIGHT * HEX_RADIUS * (float)Math.sqrt(3) / 2f);

    private final Vector3 cameraOffset = new Vector3(0, 0, 0);

    private HexMap hexMap;

    private Gui gui;

    private boolean wasLeftPressed = false;

    private Model hexModel;
    private Model cubeModel;

    @Override
    public void show() {
        MapRender.initCameraAndEnvironment(
            MAP_WIDTH, MAP_HEIGHT, HEX_RADIUS, HEX_HEIGHT, mapCenter
        );

        hexMap = new HexMap(MAP_WIDTH, MAP_HEIGHT, HexMap.Colors.randomColors());
        MapGenerator.generateNoiseMap(hexMap);

        ModelBuilder modelBuilder = new ModelBuilder();
        hexModel = modelBuilder.createCylinder(
                HEX_RADIUS * 2, HEX_HEIGHT, HEX_RADIUS * 2, 6,
                new Material(ColorAttribute.createDiffuse(Color.WHITE)),
                com.badlogic.gdx.graphics.VertexAttributes.Usage.Position | com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal
        );

        cubeModel = modelBuilder.createCylinder(
                HEX_RADIUS, HEX_RADIUS, HEX_RADIUS, 12,
                new Material( ColorAttribute.createDiffuse(new Color(1f, 0.5f, 0.2f, 0.3f)),
                    ColorAttribute.createEmissive(Color.BLUE),
                    com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute.createShininess(16f)),
                com.badlogic.gdx.graphics.VertexAttributes.Usage.Position | com.badlogic.gdx.graphics.VertexAttributes.Usage.Normal
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

                Color renderColor = tile.getColor();
                instance.materials.get(0).set(ColorAttribute.createDiffuse(renderColor));

                tile.setCubeModel(null);
            }
        }
    }

    @Override
    public void render(float delta) {
        MapRender.handleCameraInput(cameraOffset, delta);
        MapRender.handleMouseInput();

        MapRender.updateHoveredTile(gui, hexMap, HEX_RADIUS);

        MapRender.updateCamera(cameraOffset);

        MapRender.preRender();

        MapRender.render(
            hexMap,
            gui,
            HEX_RADIUS,
            cameraOffset
        );
        Vector3 cameraPosition = MapRender.getCamera().position;
        gui.getMinimap().setCameraView(cameraPosition.x, cameraPosition.z);

        handleTileClick();

        gui.render();

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

    @Override
    public void resize(int width, int height) {
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
        hexModel.dispose();
        cubeModel.dispose();
        if (gui != null) gui.dispose();
    }

}
