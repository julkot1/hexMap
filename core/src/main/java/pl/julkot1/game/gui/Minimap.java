package pl.julkot1.game.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import pl.julkot1.game.map.HexMap;
import pl.julkot1.game.map.Tile;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.graphics.g2d.Batch;


public class Minimap extends Table {
    private Image minimapImage;
    private Texture minimapTexture;
    private int minimapWidth;
    private int minimapHeight;
    private int scale = 1;
    private HexMap map;

    public interface MinimapClickListener {
        void onTileClicked(int tileX, int tileY);
    }
    private MinimapClickListener clickListener;

    public void setMinimapClickListener(MinimapClickListener listener) {
        this.clickListener = listener;
    }

    public void initMinimap() {
        this.setBackground(new com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable(
            new com.badlogic.gdx.graphics.g2d.NinePatch(
                new com.badlogic.gdx.graphics.Texture(
                    new com.badlogic.gdx.graphics.Pixmap(64, 64, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888)
                ), 8, 8, 8, 8
            )
        ));
        minimapImage = new Image();
        this.add(minimapImage).width(400).height(400).bottom().pad(20);

        minimapImage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (map == null || clickListener == null) return false;
                // y is from top, minimapImage is from bottom
                float imgW = minimapImage.getWidth();
                float imgH = minimapImage.getHeight();
                float px = x / imgW;
                float py = 1f - (y / imgH);
                int tileX = (int)(px * minimapWidth * scale);
                int tileY = (int)(py * minimapHeight * scale);
                // Clamp to map bounds
                tileX = Math.max(0, Math.min(map.getCols() - 1, tileX));
                tileY = Math.max(0, Math.min(map.getRows() - 1, tileY));
                clickListener.onTileClicked(tileX, tileY);
                return true;
            }
        });
    }

    public void update(HexMap map) {
        if (map == null) return;
        this.map = map;
        int rows = map.getRows();
        int cols = map.getCols();

        // Downscale for minimap (max 1000x1000)
        int maxSize = 1000;
        scale = Math.max(1, Math.max(rows, cols) / maxSize);
        minimapWidth = cols / scale;
        minimapHeight = rows / scale;

        Pixmap pixmap = new Pixmap(minimapWidth, minimapHeight, Pixmap.Format.RGBA8888);
        for (int y = 0; y < minimapHeight; y++) {
            for (int x = 0; x < minimapWidth; x++) {
                Tile tile = map.getTile(x * scale, y * scale);
                Color c = tile.getColor();
                pixmap.setColor(c);
                pixmap.drawPixel(x, y);
            }
        }
        if (minimapTexture != null) minimapTexture.dispose();
        minimapTexture = new Texture(pixmap);
        pixmap.dispose();
        minimapImage.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(minimapTexture)));
    }

    // Camera position and view size for overlay
    private float cameraX = -1, cameraY = -1, viewWidth = -1, viewHeight = -1;

    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    public void setCameraView(float cameraX, float cameraY, float viewWidth, float viewHeight) {
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        // Draw camera position as a point (circle) overlay
        if (cameraX >= 0 && cameraY >= 0 && minimapImage != null && map != null) {
            batch.end();
            shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(Color.RED);

            float imgX = minimapImage.localToStageCoordinates(new com.badlogic.gdx.math.Vector2(0, 0)).x;
            float imgY = minimapImage.localToStageCoordinates(new com.badlogic.gdx.math.Vector2(0, 0)).y;
            float imgW = minimapImage.getWidth();
            float imgH = minimapImage.getHeight();

            float px = (cameraX / Math.max(1f, (map.getCols() - 1))) * (imgW - 1);
            float py = imgH - (cameraY / Math.max(1f, (map.getRows() - 1))) * (imgH - 1);

            // Clamp to minimap bounds
            px = Math.max(0, Math.min(imgW - 1, px));
            py = Math.max(0, Math.min(imgH - 1, py));

            float pointX = imgX + px;
            float pointY = imgY + py;

            float radius = Math.max(3f, imgW * 0.012f);
            shapeRenderer.circle(pointX, pointY, radius);

            shapeRenderer.end();
            batch.begin();
        }
    }
}
