package demos.nehe.lesson40;

import demos.common.GLDisplay;

/**
 * @author Scott Rains
 */
public class Lesson40 {

    public static void main(String[] args) {

        boolean[] g_keys = new boolean[256];
        RopeSimulation ropeSimulation = new RopeSimulation(80, 0.05f, 10000.0f, 0.05f, 0.2f, new Vector3D(0, -9.81f, 0), 0.02f, 100.0f, 0.2f, 2.0f, -1.5f);

        GLDisplay neheGLDisplay = GLDisplay.createGLDisplay("Lesson 40: Rope Physics");
        Renderer renderer = new Renderer(ropeSimulation, g_keys);
        InputHandler inputHandler = new InputHandler(neheGLDisplay, g_keys);

        neheGLDisplay.addGLEventListener(renderer);
        neheGLDisplay.addKeyListener(inputHandler);
        neheGLDisplay.start();
    }
}