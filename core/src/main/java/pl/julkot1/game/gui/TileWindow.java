package pl.julkot1.game.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import pl.julkot1.game.map.Tile;

public class TileWindow extends Table {
    private Label infoLabel;

    public void InitWindow(Gui gui, Skin skin) {
        this.setBackground(gui.createBackgroundDrawable(new Color(0.12f, 0.12f, 0.18f, 0.85f), 12));
        this.setWidth(200);
        this.setHeight(140);
        this.top().right().pad(5);
        this.pad(20);
        this.padLeft(30);
        this.padRight(30);

        infoLabel = new Label("Tile Informations", skin);
        infoLabel.setWrap(true);
        this.clear();
        this.add(infoLabel).center().width(140);
    }

    public void setTileInfo(Tile tile, int row, int col) {
        if (infoLabel == null || tile == null) return;
        String info = String.format(
            """
                Row: %d
                Col: %d
                Terrain: %s
                Temperature: %.2f C (%s)
                Moisture: %.1f (%s)
                Biome: %s
                Clicked: %s""",
            row, col,
            tile.getTerrainType(),
            tile.getTemperature() * 30, tile.getTemperatureType(),
            tile.getMoisture() * 100, tile.getMoistureType(),
            tile.getBiomeType(),
            tile.isClicked() ? "Yes" : "No"
        );
        infoLabel.setText(info);
    }
}
