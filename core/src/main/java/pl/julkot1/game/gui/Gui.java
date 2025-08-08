package pl.julkot1.game.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import pl.julkot1.game.map.HexMap;

public class Gui {
    private final Stage stage;
    private final Table mainContainer;
    private final RenderPanel renderPanel;
    private final InfoPanel infoPanel;

    private Runnable onButtonClicked;

    private final int totalTiles;

    private final TileWindow tileWindow;

    private final Minimap minimap;

    public Gui(int tileCount) {
        stage = new Stage(new ScreenViewport());
        totalTiles = tileCount;

        BitmapFont font = generateFont("font.TTF", 18, Color.WHITE);

        Skin skin = new Skin();
        skin.add("default", font);

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = createBackgroundDrawable(new Color(0.2f, 0.4f, 0.7f, 1f), 8);
        buttonStyle.down = createBackgroundDrawable(new Color(0.15f, 0.3f, 0.5f, 1f), 8);
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        skin.add("default", buttonStyle);

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = font;
        textFieldStyle.fontColor = Color.WHITE;
        textFieldStyle.background = createBackgroundDrawable(new Color(0.12f, 0.12f, 0.18f, 0.85f), 12);
        skin.add("default", textFieldStyle);

        // Main container (fills parent)
        mainContainer = new Table();
        mainContainer.setFillParent(true);

        // Split into two columns: left for info/render, right for tile window
        Table leftPanel = new Table();
        leftPanel.top().left();
        infoPanel = new InfoPanel();
        infoPanel.initPanel(skin, totalTiles, this);
        renderPanel = new RenderPanel();
        renderPanel.initPanel(skin, this);
        leftPanel.add(infoPanel).top().left().pad(10).row();
        leftPanel.add().expandY().row(); // Spacer
        leftPanel.add(renderPanel).bottom().left().pad(10);

        tileWindow = new TileWindow();
        tileWindow.InitWindow(this, skin);
        tileWindow.setVisible(false);

        Table rightPanel = new Table();
        rightPanel.add().expandY().row(); // Spacer to push tileWindow to center
        rightPanel.add(tileWindow).center().pad(10).row();
        rightPanel.add().expandY().row();

        // Minimap (bottom right)
        minimap = new Minimap();
        minimap.initMinimap();

        // Add minimap to rightPanel at the bottom
        rightPanel.add().expandY().expandX().row();
        rightPanel.add(minimap).bottom().right().pad(10);

        mainContainer.add(leftPanel).expand().fillY().left();
        mainContainer.add(rightPanel).width(400).expandY().right().bottom();

        stage.addActor(mainContainer);
    }

    public void setOnButtonClicked(Runnable onButtonClicked) {
        this.onButtonClicked = onButtonClicked;
    }

    // Called by RenderPanel's button
    public void triggerButton() {
        if (onButtonClicked != null) onButtonClicked.run();
    }

    private BitmapFont generateFont(String ttfPath, int size, Color color) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(ttfPath));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;
        BitmapFont font = generator.generateFont(parameter);
        generator.dispose();
        return font;
    }

    Drawable createBackgroundDrawable(Color color, int cornerRadius) {
        int size = 64;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        NinePatch patch = new NinePatch(new Texture(pixmap), cornerRadius, cornerRadius, cornerRadius, cornerRadius);
        pixmap.dispose();
        return new NinePatchDrawable(patch);
    }

    // Proxy methods for InfoPanel
    public void setSelectedCount(int count) {
        infoPanel.setSelectedCount(count);
    }

    public void setFps(int fps) {
        infoPanel.setFps(fps);
    }

    public void setCameraPosition(float x, float y) {
        infoPanel.setCameraPosition(x, y);
    }

    public void setHour(float timeOfDay) {
        infoPanel.setHour(timeOfDay);
    }

    public RenderPanel getRenderPanel() {
        return renderPanel;
    }

    public InfoPanel getInfoPanel() {
        return infoPanel;
    }

    public TileWindow getTileWindow() {
        return tileWindow;
    }

    public void render() {
        stage.act();
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }

    public Stage getStage() {
        return stage;
    }

    public void updateMinimap(HexMap map) {
        minimap.update(map);
    }

    public Minimap getMinimap() {
        return minimap;
    }
}
