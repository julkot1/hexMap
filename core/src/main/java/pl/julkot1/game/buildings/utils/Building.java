package pl.julkot1.game.buildings.utils;

import com.badlogic.gdx.graphics.g3d.Model;

public abstract class Building {
    private Model model;
    private String name;

    public Building(String name) {
        this.model = createModel();
        this.name = name;
    }
    public Model getModel() {
        return model;
    }
    public String getName() {
        return name;
    }

    protected abstract Model createModel();

}
