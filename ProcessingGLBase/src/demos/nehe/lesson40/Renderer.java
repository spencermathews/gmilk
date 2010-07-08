package demos.nehe.lesson40;

import com.sun.opengl.util.GLUT;

import java.awt.event.KeyEvent;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

/**
 * @author Scott Rains
 */
class Renderer implements GLEventListener {

    private RopeSimulation ropeSimulation = null;
    private GLUT glut;
    private GLU glu = new GLU();
    private long previousTime;
    boolean[] g_keys;

    public Renderer(RopeSimulation ropeSimulation, boolean[] keys) {
        this.ropeSimulation = ropeSimulation;
        this.g_keys = keys;
    }

    private void glPrint(GL gl, float x, float y, float z, int font, String string) {
        gl.glPushMatrix();
        gl.glTranslatef(x, y, z); // Position Text On The Screen
        gl.glScalef(0.005f, 0.005f, 0.005f);
        glut = new GLUT();
        int width = glut.glutStrokeLength(font, string);
        gl.glTranslatef(-width, 0, 0); // Right align text with position
        for (int i = 0; i < string.length(); i++) {
            glut.glutStrokeCharacter(font, string.charAt(i));
        }
        gl.glPopMatrix();
    }

    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);                        // Black Background
        gl.glClearDepth(1.0f);                        // Depth Buffer Setup
        gl.glShadeModel(GL.GL_SMOOTH);                                  // Select Smooth Shading
        gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);     // Set Perspective Calculations To Most Accurate
    }

    public void display(GLAutoDrawable drawable) {
        long currentTime = System.currentTimeMillis();
        update(currentTime - previousTime);
        previousTime = currentTime;

        GL gl = drawable.getGL();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();                                            // Reset The Modelview Matrix

        // Position Camera 40 Meters Up In Z-Direction.
        // Set The Up Vector In Y-Direction So That +X Directs To Right And +Y Directs To Up On The Window.
        glu.gluLookAt(0, 0, 5, 0, 0, 0, 0, 1, 0);

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear Screen And Depth Buffer
        // Draw A Plane To Represent The Ground (Different Colors To Create A Fade)
        gl.glBegin(GL.GL_QUADS);
        gl.glColor3ub((byte) 0, (byte) 0, (byte) 255);          // Set Color To Light Blue
        gl.glVertex3f(20, ropeSimulation.groundHeight, 20);
        gl.glVertex3f(-20, ropeSimulation.groundHeight, 20);
        gl.glColor3ub((byte) 0, (byte) 0, (byte) 0);            // Set Color To Black
        gl.glVertex3f(-20, ropeSimulation.groundHeight, -20);
        gl.glVertex3f(20, ropeSimulation.groundHeight, -20);
        gl.glEnd();

        // Start Drawing Shadow Of The Rope
        gl.glColor3ub((byte) 0, (byte) 0, (byte) 0);                    // Set Color To Black
        for (int a = 0; a < ropeSimulation.numOfMasses - 1; ++a) {
            Mass mass1 = ropeSimulation.getMass(a);
            Vector3D pos1 = mass1.pos;

            Mass mass2 = ropeSimulation.getMass(a + 1);
            Vector3D pos2 = mass2.pos;

            gl.glLineWidth(2);
            gl.glBegin(GL.GL_LINES);
            gl.glVertex3f(pos1.x, ropeSimulation.groundHeight, pos1.z); // Draw Shadow At groundHeight
            gl.glVertex3f(pos2.x, ropeSimulation.groundHeight, pos2.z); // Draw Shadow At groundHeight
            gl.glEnd();
        }
        // Drawing Shadow Ends Here.

        // Start Drawing The Rope.
        gl.glColor3ub((byte) 255, (byte) 255, (byte) 0);                // Set Color To Yellow
        for (int a = 0; a < ropeSimulation.numOfMasses - 1; ++a) {
            Mass mass1 = ropeSimulation.getMass(a);
            Vector3D pos1 = mass1.pos;

            Mass mass2 = ropeSimulation.getMass(a + 1);
            Vector3D pos2 = mass2.pos;

            gl.glLineWidth(4);
            gl.glBegin(GL.GL_LINES);
            gl.glVertex3f(pos1.x, pos1.y, pos1.z);
            gl.glVertex3f(pos2.x, pos2.y, pos2.z);
            gl.glEnd();
        }
        // Drawing The Rope Ends Here.

        gl.glFlush();                                                   // Flush The GL Rendering Pipeline
    }

    /**
     * Perform Motion Updates Here (implementing the original "void Update (DWORD milliseconds)" call from Lesson.cpp)
     * Note: I think the key handling should be preformed inside the simulation but
     * keeping it here to stay true the original c++ code
     */
    private void update(long milliseconds) {
        Vector3D ropeConnectionVel = new Vector3D();                                                // Create A Temporary Vector3D

        // Keys Are Used To Move The Rope
        if (g_keys[KeyEvent.VK_RIGHT])                                    // Is The Right Arrow Being Pressed?
        {
            ropeConnectionVel.x += 3.0f;                                        // Add Velocity In +X Direction
        }

        if (g_keys[KeyEvent.VK_LEFT])                                    // Is The Left Arrow Being Pressed?
        {
            ropeConnectionVel.x -= 3.0f;                                        // Add Velocity In -X Direction
        }

        if (g_keys[KeyEvent.VK_UP])                                    // Is The Up Arrow Being Pressed?
        {
            ropeConnectionVel.z -= 3.0f;                                        // Add Velocity In +Z Direction
        }

        if (g_keys[KeyEvent.VK_DOWN])                                    // Is The Down Arrow Being Pressed?
        {
            ropeConnectionVel.z += 3.0f;                                        // Add Velocity In -Z Direction
        }

        if (g_keys[KeyEvent.VK_HOME])                                    // Is The Home Key Pressed?
        {
            ropeConnectionVel.y += 3.0f;                                        // Add Velocity In +Y Direction
        }

        if (g_keys[KeyEvent.VK_END])                                    // Is The End Key Pressed?
        {
            ropeConnectionVel.y -= 3.0f;                                        // Add Velocity In -Y Direction
        }

        ropeSimulation.setRopeConnectionVel(ropeConnectionVel);                // Set The Obtained ropeConnectionVel In The Simulation

        float dt = milliseconds / 1000.0f;                                        // Let's Convert Milliseconds To Seconds

        float maxPossible_dt = 0.002f;                                            // Maximum Possible dt Is 0.002 Seconds
        // This Is Needed To Prevent Pass Over Of A Non-Precise dt Value

        int numOfIterations = (int) (dt / maxPossible_dt) + 1;                    // Calculate Number Of Iterations To Be Made At This Update Depending On maxPossible_dt And dt
        if (numOfIterations != 0)                                                // Avoid Division By Zero
        {
            dt = dt / numOfIterations;                                            // dt Should Be Updated According To numOfIterations
        }

        for (int a = 0; a < numOfIterations; ++a)                                // We Need To Iterate Simulations "numOfIterations" Times
        {
            ropeSimulation.operate(dt);
        }
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();

        height = (height == 0) ? 1 : height;

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();

        glu.gluPerspective(45, (float) width / height, 1, 100);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {

    }
}