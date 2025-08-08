package pl.julkot1.game.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;

public class InfoPanel extends Table {
    private Label selectedLabel;
    private Label fpsLabel;
    private Label cameraLabel;
    private Label hourLabel;

    public void initPanel(Skin skin, int totalTiles, Gui gui) {
        this.setBackground(gui.createBackgroundDrawable(new Color(0.12f, 0.12f, 0.18f, 0.85f), 12));
        this.top().left().pad(10);

        selectedLabel = new Label("Selected: 0", skin);
        fpsLabel = new Label("FPS: 0", skin);
        cameraLabel = new Label("Camera: (0,0)", skin);
        hourLabel = new Label("Hour: 00:00", skin);

        this.add(selectedLabel).left().row();
        this.add(fpsLabel).left().row();
        this.add(cameraLabel).left().row();
        this.add(hourLabel).left().row();
    }

    public void setSelectedCount(int count) {
        if (selectedLabel != null)
            selectedLabel.setText("Selected: " + count);
    }

    public void setFps(int fps) {
        if (fpsLabel != null)
            fpsLabel.setText("FPS: " + fps);
    }

    public void setCameraPosition(float x, float y) {
        if (cameraLabel != null)
            cameraLabel.setText(String.format("Camera: [%.1f, %.1f]", x, y));
    }

    public void setHour(float timeOfDay) {
        if (hourLabel != null) {
            int hour = (int)(timeOfDay * 24) % 24;
            int minute = (int)((timeOfDay * 24 - hour) * 60);
            String hourStr = String.format("%02d:%02d", hour, minute);
            hourLabel.setText("Hour: " + hourStr);
        }
    }
}
