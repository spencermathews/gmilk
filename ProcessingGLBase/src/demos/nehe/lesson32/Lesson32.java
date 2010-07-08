package demos.nehe.lesson32;

import demos.common.GLDisplay;

/**
 * @author Pepijn Van Eeckhoudt
 */
public class Lesson32 {
    public static void main(String[] args) {
        GLDisplay neheGLDisplay = GLDisplay.createGLDisplay("Lesson 32: Picking, Alpha Blending, Alpha Testing, Sorting");
        Renderer renderer = new Renderer();
        InputHandler inputHandler = new InputHandler(neheGLDisplay, renderer);

        neheGLDisplay.addGLEventListener(renderer);
        neheGLDisplay.addKeyListener(inputHandler);
        neheGLDisplay.addMouseListener(inputHandler);
        neheGLDisplay.addMouseMotionListener(inputHandler);
        neheGLDisplay.start();
    }
}