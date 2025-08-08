package pl.julkot1.game.buildings.utils;

import java.util.ArrayList;
import java.util.List;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Set;
import org.reflections.Reflections;

public class BuildingManager {

    private final List<Class<? extends Building>> registeredBuildings = new ArrayList<>();

    public BuildingManager() {
        scanAndRegisterBuildings();
    }

    private void scanAndRegisterBuildings() {

        Reflections reflections = new Reflections("pl.julkot1.game.buildings");
        Set<Class<? extends Building>> buildingClasses = reflections.getSubTypesOf(Building.class);

        for (Class<? extends Building> clazz : buildingClasses) {
            if (clazz.isAnnotationPresent(BuildingClass.class) && !Modifier.isAbstract(clazz.getModifiers())) {
                registeredBuildings.add(clazz);
            }
        }
    }

    public List<Class<? extends Building>> getRegisteredBuildings() {
        return registeredBuildings;
    }
}
