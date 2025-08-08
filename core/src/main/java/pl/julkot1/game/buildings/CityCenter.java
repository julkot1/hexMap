package pl.julkot1.game.buildings;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import pl.julkot1.game.Screen3D;
import pl.julkot1.game.buildings.utils.Building;
import pl.julkot1.game.buildings.utils.BuildingClass;

@BuildingClass
public class CityCenter extends Building {
    /**
     * Creates a new City Center building.
     * This building serves as the central hub for the city, providing essential services and facilities.
     */
    protected Model createModel() {
         ModelBuilder modelBuilder = new ModelBuilder();
         return modelBuilder.createCylinder(
             Screen3D.HEX_RADIUS,   Screen3D.HEX_RADIUS,   Screen3D.HEX_RADIUS, 12,
            new Material( ColorAttribute.createDiffuse(new Color(0.2f, 0.4f, 0.2f, 0.3f)),
                FloatAttribute.createShininess(16f)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );
    }
    public CityCenter() {
        super("City Center");
    }
}
