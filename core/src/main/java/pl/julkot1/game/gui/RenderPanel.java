package pl.julkot1.game.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import pl.julkot1.game.map.MapGenerator;

public class RenderPanel extends Table {
    private TextField seedField;
    public void initPanel(Skin skin, Gui gui) {
        this.bottom().left().pad(10);
        this.setBackground(gui.createBackgroundDrawable(new Color(0.12f, 0.12f, 0.18f, 0.85f), 12));

        TextButton regenButton = new TextButton("Regenerate", skin);
        this.add(regenButton).pad(10).left();

        seedField = new TextField("", skin);
        seedField.setMessageText("Enter seed (optional)");
        this.add(seedField).pad(10).left().width(200);

        regenButton.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                String seed = seedField.getText();
                if (!seed.isEmpty()) {
                    MapGenerator.SEED = seed.hashCode();
                    System.out.println("Using seed: " + MapGenerator.SEED);
                }
                gui.triggerButton();
            }
        });
    }
}
