package demos.nehe.lesson31;

import demos.common.GLDisplay;

/**
 * @author Nikolaj Ougaard
 */
public class Lesson31 {
    public static void main(String[] args) {
        GLDisplay neheGLDisplay = GLDisplay.createGLDisplay("Lesson 31: Model Loading");
        Renderer renderer = new Renderer();
        neheGLDisplay.addGLEventListener(renderer);
        neheGLDisplay.start();
    }
}
