package demos.nehe.lesson31;

import demos.common.ResourceRetriever;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import java.io.IOException;
import java.io.InputStream;

/**
 * JOGL conversion for NeHe lesson 31
 *
 * @author Nikolaj Ougaard
 */
class Renderer implements GLEventListener {
    private GLU glu;
    private MS3DJOGLRenderer renderer = new MS3DJOGLRenderer();
    private MS3DModel model;

    private float yrot;

    /**
     * @param args
     */
    public static void main(String[] args) {
        new Renderer();
    }

    /**
     * Constructor
     */
    public Renderer() {
    }

    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        glu = new GLU();

        //Setup model
        String modelDirectory = "demos/data/models/";
        try {
            InputStream stream = ResourceRetriever.getResourceAsStream(modelDirectory + "model.ms3d");
            this.model = MS3DModel.decodeMS3DModel(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        renderer.loadTextures(gl, this.model, modelDirectory);

        gl.glShadeModel(GL.GL_SMOOTH);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepth(1.0f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
    }

    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        glu.gluLookAt(75, 75, 75, 0, 0, 0, 0, 1, 0);

        gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);

        //Preserve texture state
        boolean texEnabled = gl.glIsEnabled(GL.GL_TEXTURE_2D);

        renderer.renderModel(gl, model);

        if (texEnabled) {
            gl.glEnable(GL.GL_TEXTURE_2D);
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
        }

        yrot += 1.0f;
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();

        if (height == 0) {
            height = 1;
        }

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();

        glu.gluPerspective(45.0f, (float) width / (float) height, 0.1f, 300.0f);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();

    }

    public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
    }
}
