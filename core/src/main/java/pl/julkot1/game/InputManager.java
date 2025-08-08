package pl.julkot1.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class InputManager {
    private static InputManager instance;

    private boolean[] currentKeys = new boolean[256];
    private boolean[] previousKeys = new boolean[256];
    private boolean[] currentButtons = new boolean[5];
    private boolean[] previousButtons = new boolean[5];

    private int mouseX, mouseY;
    private int lastMouseX, lastMouseY;
    private boolean dragging = false;

    private InputManager() {}

    public static InputManager get() {
        if (instance == null) instance = new InputManager();
        return instance;
    }

    public void update() {
        System.arraycopy(currentKeys, 0, previousKeys, 0, currentKeys.length);
        System.arraycopy(currentButtons, 0, previousButtons, 0, currentButtons.length);

        for (int i = 0; i < currentKeys.length; i++) {
            currentKeys[i] = Gdx.input.isKeyPressed(i);
        }
        for (int i = 0; i < currentButtons.length; i++) {
            currentButtons[i] = Gdx.input.isButtonPressed(i);
        }
        mouseX = Gdx.input.getX();
        mouseY = Gdx.input.getY();
    }

    public boolean isKeyPressed(int key) {
        return currentKeys[key];
    }

    public boolean isKeyJustPressed(int key) {
        return currentKeys[key] && !previousKeys[key];
    }

    public boolean isButtonPressed(int button) {
        return currentButtons[button];
    }

    public boolean isButtonJustPressed(int button) {
        return currentButtons[button] && !previousButtons[button];
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void startDrag() {
        dragging = true;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    public void stopDrag() {
        dragging = false;
    }

    public int getLastMouseX() {
        return lastMouseX;
    }

    public void setLastMouseX(int x) {
        lastMouseX = x;
    }
}
