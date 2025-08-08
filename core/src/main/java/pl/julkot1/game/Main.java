package pl.julkot1.game;

import com.badlogic.gdx.Game;
import pl.julkot1.game.buildings.utils.BuildingManager;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    @Override
    public void create() {
        BuildingManager buildingManager = new BuildingManager();
        // Print registered buildings for debugging
        setScreen(new Screen3D());
    }
}
