package demos.nehe.lesson32;

import com.sun.opengl.util.BufferUtil;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;

class Renderer implements GLEventListener {
    private GLU glu = new GLU();
    private long previousTime = System.currentTimeMillis();

    private ArrayList targets = new ArrayList();
    private TextureImage[] textures; // Storage For 10 Textures

    private Rectangle2D.Float[] size = {
            new Rectangle2D.Float(0, 0, 1.0f, 1.0f),
            new Rectangle2D.Float(0, 0, 1.0f, 1.0f),
            new Rectangle2D.Float(0, 0, 1.0f, 1.0f),
            new Rectangle2D.Float(0, 0, 0.5f, 1.0f),
            new Rectangle2D.Float(0, 0, 0.75f, 1.5f)
    };
    // User Defined Variables

    private int base; // Font Display List

    private float roll = 0.0f; // Rolling Clouds

    private int level = 1; // Current Level

    private int miss; // Missed Targets

    private int kills; // Level Kill Counter

    private int score; // Current Score

    private boolean isGameOver = true; // Game Over?

    private int mouseX;
    private int mouseY;
    private boolean isClicked = false;
    private boolean restart;
    private TargetType[] targetTypes;


    private void BuildFont(GL gl) {
        base = gl.glGenLists(95); // Creating 95 Display Lists
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[9].getTexID()); // Bind Our Font Texture
        for (int loop = 0; loop < 95; loop++) {
            float cx = (loop % 16) / 16.0f;                         // X Position Of Current Character
            float cy = (loop / 16) / 16.0f;                          // Y Position Of Current Character

            gl.glNewList(base + loop, GL.GL_COMPILE);               // Start Building A List
            gl.glBegin(GL.GL_QUADS);                                // Use A Quad For Each Character
            {
                gl.glTexCoord2f(cx, 1.0f - cy - 0.0625f);
                gl.glVertex2i(0, 0);                    // Texture / Vertex Coord (Bottom Left)
                gl.glTexCoord2f(cx + 0.0625f, 1.0f - cy - 0.0625f);
                gl.glVertex2i(16, 0);                   // Texutre / Vertex Coord (Bottom Right)
                gl.glTexCoord2f(cx + 0.0625f, 1.0f - cy);
                gl.glVertex2i(16, 16);                  // Texture / Vertex Coord (Top Right)
                gl.glTexCoord2f(cx, 1.0f - cy);
                gl.glVertex2i(0, 16);                   // Texture / Vertex Coord (Top Left)
            }
            gl.glEnd();                                     // Done Building Our Quad (Character)
            gl.glTranslated(10, 0, 0);                      // Move To The Right Of The Character
            gl.glEndList();                                         // Done Building The Display List
        } // Loop Until All 95 Are Built
    }

    public Renderer() {
    }

    private ByteBuffer stringBuffer = BufferUtil.newByteBuffer(256);

    private void glPrint(GL gl, int x, int y, String string) // Where The Printing Happens
    {
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[9].getTexID());            // Select Our Font Texture
        gl.glDisable(GL.GL_DEPTH_TEST);                            // Disables Depth Testing
        gl.glMatrixMode(GL.GL_PROJECTION);                        // Select The Projection Matrix
        gl.glPushMatrix();                                        // Store The Projection Matrix
        gl.glLoadIdentity();                                    // Reset The Projection Matrix
        gl.glOrtho(0, 640, 0, 480, -1, 1);                            // Set Up An Ortho Screen
        gl.glMatrixMode(GL.GL_MODELVIEW);                            // Select The Modelview Matrix
        gl.glPushMatrix();                                        // Store The Modelview Matrix
        gl.glLoadIdentity();                                    // Reset The Modelview Matrix
        gl.glTranslated(x, y, 0);                                // Position The Text (0,0 - Bottom Left)
        gl.glListBase(base - 32);// + (128 * set));						// Choose The Font Set (0 or 1)

        if (stringBuffer.capacity() < string.length()) {
            stringBuffer = BufferUtil.newByteBuffer(string.length());
        }

        stringBuffer.clear();
        stringBuffer.put(string.getBytes());
        stringBuffer.flip();
        gl.glCallLists(string.length(), GL.GL_BYTE, stringBuffer);            // Write The Text To The Screen
        gl.glMatrixMode(GL.GL_PROJECTION);                        // Select The Projection Matrix
        gl.glPopMatrix();                                        // Restore The Old Projection Matrix
        gl.glMatrixMode(GL.GL_MODELVIEW);                            // Select The Modelview Matrix
        gl.glPopMatrix();                                        // Restore The Old Projection Matrix
        gl.glEnable(GL.GL_DEPTH_TEST);                            // Enables Depth Testing
    }

    public void init(GLAutoDrawable drawable) {

        //	gl.glAlphaFunc(GL_GREATER,0.1f);								// Set Alpha Testing     (disable blending)
        //	gl.glEnable(GL_ALPHA_TEST);									// Enable Alpha Testing  (disable blending)
        GL gl = drawable.getGL();

        targetTypes = new TargetType[]{
                TargetType.FACE,
                TargetType.BUCKET,
                TargetType.TARGET,
                TargetType.COKE,
                TargetType.VASE
        };

        textures = new TextureImage[10];
        try {
            textures[0] = TGALoader.loadTGA(gl, "demos/data/images/BlueFace.tga"); // Load The BlueFace Texture
            textures[1] = TGALoader.loadTGA(gl, "demos/data/images/Bucket.tga"); // Load The Bucket Texture
            textures[2] = TGALoader.loadTGA(gl, "demos/data/images/Target.tga"); // Load The Target Texture
            textures[3] = TGALoader.loadTGA(gl, "demos/data/images/Coke.tga"); // Load The Coke Texture
            textures[4] = TGALoader.loadTGA(gl, "demos/data/images/Vase.tga"); // Load The Vase Texture
            textures[5] = TGALoader.loadTGA(gl, "demos/data/images/Explode.tga"); // Load The Explosion Texture
            textures[6] = TGALoader.loadTGA(gl, "demos/data/images/Ground.tga"); // Load The Ground Texture
            textures[7] = TGALoader.loadTGA(gl, "demos/data/images/Sky.tga"); // Load The Sky Texture
            textures[8] = TGALoader.loadTGA(gl, "demos/data/images/Crosshair.tga"); // Load The Crosshair Texture
            textures[9] = TGALoader.loadTGA(gl, "demos/data/images/Font.tga"); // Load The Font Texture
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        BuildFont(gl); // Build Our Font Display List
        gl.glClearColor(0.0F, 0.0F, 0.0F, 0.0F); // Black Background
        gl.glClearDepth(1.0F); // Depth Buffer Setup
        gl.glDepthFunc(GL.GL_LEQUAL); // Type Of Depth Testing
        gl.glEnable(GL.GL_DEPTH_TEST); // Enable Depth Testing
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA); // Enable Alpha Blending (disable alpha testing)
        gl.glEnable(GL.GL_BLEND); // Enable Blending       (disable alpha testing)
        //	gl.glAlphaFunc(GL_GREATER,0.1f);								// Set Alpha Testing     (disable blending)
        //	gl.glEnable(GL_ALPHA_TEST);									// Enable Alpha Testing  (disable blending)
        gl.glEnable(GL.GL_TEXTURE_2D); // Enable Texture Mapping
        gl.glEnable(GL.GL_CULL_FACE); // Remove Back Face

        resetGame();
    }

    public void display(GLAutoDrawable drawable) {
        long currentTime = System.currentTimeMillis();
        update(currentTime - previousTime);
        previousTime = currentTime;

        GL gl = drawable.getGL();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);    // Clear Screen And Depth Buffer
        gl.glLoadIdentity();                                            // Reset The Modelview Matrix
        gl.glPushMatrix();                                              // Push The Modelview Matrix
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[7].getTexID());       // Select The Sky Texture
        gl.glBegin(GL.GL_QUADS);                                        // Begin Drawing Quads
        {
            gl.glTexCoord2f(1.0f, roll / 1.5f + 1.0f);
            gl.glVertex3f(28.0f, +7.0f, -50.0f);                    // Top Right
            gl.glTexCoord2f(0.0f, roll / 1.5f + 1.0f);
            gl.glVertex3f(-28.0f, +7.0f, -50.0f);                   // Top Left
            gl.glTexCoord2f(0.0f, roll / 1.5f + 0.0f);
            gl.glVertex3f(-28.0f, -3.0f, -50.0f);                   // Bottom Left
            gl.glTexCoord2f(1.0f, roll / 1.5f + 0.0f);
            gl.glVertex3f(28.0f, -3.0f, -50.0f);                    // Bottom Right
            gl.glTexCoord2f(1.5f, roll + 1.0f);
            gl.glVertex3f(28.0f, +7.0f, -50.0f);                    // Top Right
            gl.glTexCoord2f(0.5f, roll + 1.0f);
            gl.glVertex3f(-28.0f, +7.0f, -50.0f);                   // Top Left
            gl.glTexCoord2f(0.5f, roll + 0.0f);
            gl.glVertex3f(-28.0f, -3.0f, -50.0f);                   // Bottom Left
            gl.glTexCoord2f(1.5f, roll + 0.0f);
            gl.glVertex3f(28.0f, -3.0f, -50.0f);                    // Bottom Right
            gl.glTexCoord2f(1.0f, roll / 1.5f + 1.0f);
            gl.glVertex3f(28.0f, +7.0f, 0.0f);                      // Top Right
            gl.glTexCoord2f(0.0f, roll / 1.5f + 1.0f);
            gl.glVertex3f(-28.0f, +7.0f, 0.0f);                     // Top Left
            gl.glTexCoord2f(0.0f, roll / 1.5f + 0.0f);
            gl.glVertex3f(-28.0f, +7.0f, -50.0f);                   // Bottom Left
            gl.glTexCoord2f(1.0f, roll / 1.5f + 0.0f);
            gl.glVertex3f(28.0f, +7.0f, -50.0f);                    // Bottom Right
            gl.glTexCoord2f(1.5f, roll + 1.0f);
            gl.glVertex3f(28.0f, +7.0f, 0.0f);                      // Top Right
            gl.glTexCoord2f(0.5f, roll + 1.0f);
            gl.glVertex3f(-28.0f, +7.0f, 0.0f);                     // Top Left
            gl.glTexCoord2f(0.5f, roll + 0.0f);
            gl.glVertex3f(-28.0f, +7.0f, -50.0f);                   // Bottom Left
            gl.glTexCoord2f(1.5f, roll + 0.0f);
            gl.glVertex3f(28.0f, +7.0f, -50.0f);                    // Bottom Right
        }
        gl.glEnd();                                                     // Done Drawing Quads

        gl.glBindTexture(GL.GL_TEXTURE_2D, textures[6].getTexID());       // Select The Ground Texture
        gl.glBegin(GL.GL_QUADS);                                        // Draw A Quad
        {
            gl.glTexCoord2f(7.0f, 4.0f - roll);
            gl.glVertex3f(27.0f, -3.0f, -50.0f);                    // Top Right
            gl.glTexCoord2f(0.0f, 4.0f - roll);
            gl.glVertex3f(-27.0f, -3.0f, -50.0f);                   // Top Left
            gl.glTexCoord2f(0.0f, 0.0f - roll);
            gl.glVertex3f(-27.0f, -3.0f, 0.0f);                     // Bottom Left
            gl.glTexCoord2f(7.0f, 0.0f - roll);
            gl.glVertex3f(27.0f, -3.0f, 0.0f);                      // Bottom Right
        }
        gl.glEnd();                                                     // Done Drawing Quad
        drawTargets(gl);                                                // Draw Our Targets
        gl.glPopMatrix();                                               // Pop The Modelview Matrix

        // Crosshair (In Ortho View)
        gl.glMatrixMode(GL.GL_PROJECTION);                              // Select The Projection Matrix
        gl.glPushMatrix();                                              // Store The Projection Matrix
        gl.glLoadIdentity();                                            // Reset The Projection Matrix
        gl.glOrtho(0, drawable.getWidth(), 0, drawable.getHeight(), -1, 1); // Set Up An Ortho Screen
        gl.glMatrixMode(GL.GL_MODELVIEW);                               // Select The Modelview Matrix
        gl.glTranslated(mouseX, drawable.getHeight() - mouseY, 0.0f); // Move To The Current Mouse Position

        drawObject(gl, 16, 16, textures[8].getTexID());                                      // Draw The Crosshair

        if (isClicked) {
            doSelection(gl);
        }
        isClicked = false;

        // Game Stats / Title
        glPrint(gl, 240, 450, "NeHe Productions");            // Print Title
        glPrint(gl, 10, 10, "Level: " + level);                // Print Level
        glPrint(gl, 250, 10, "Score: " + score);                     // Print Score

        if (miss > 9) {
            // Have We Missed 10 Objects?
            miss = 9; // Limit Misses To 10
            isGameOver = true; // Game Over TRUE
        }

        if (isGameOver) {
            glPrint(gl, 490, 10, "GAME OVER"); // Game Over Message
        } else {
            glPrint(gl, 490, 10, "Morale: " + (10 - miss) + "/10"); // Print Morale #/10
        }
        gl.glMatrixMode(GL.GL_PROJECTION); // Select The Projection Matrix
        gl.glPopMatrix(); // Restore The Old Projection Matrix
        gl.glMatrixMode(GL.GL_MODELVIEW); // Select The Modelview Matrix
        gl.glFlush(); // Flush The GL Rendering Pipeline
    }

    public void setPickPoint(Point point, boolean fire) {
        mouseX = point.x;
        mouseY = point.y;
        isClicked = fire;
    }

    public void doSelection(GL gl) {
        int BUFSIZE = 512;
        //   int[] buffer = new int[BUFSIZE]; // Set Up A Selection Buffer
        IntBuffer selectBuffer = BufferUtil.newIntBuffer(BUFSIZE);
        int hits; // The Number Of Objects That We Selected
        if (isGameOver) {
            // Is Game Over?
            return; // If So, Don't Bother Checking For Hits
        }
        //PlaySound("data/shot.wav",NULL,SND_ASYNC);					// Play Gun Shot Sound
        new AePlayWave("resources/demos/data/samples/shot.wav").start();

        // The Size Of The Viewport. [0] Is <x>, [1] Is <y>, [2] Is <length>, [3] Is <width>
        int[] viewport = new int[4];

        // This Sets The Array <viewport> To The Size And Location Of The Screen Relative To The Window
        gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
        gl.glSelectBuffer(BUFSIZE, selectBuffer); // Tell OpenGL To Use Our Array For Selection
        // Puts OpenGL In Selection Mode. Nothing Will Be Drawn.  Object ID's and Extents Are Stored In The Buffer.
        gl.glRenderMode(GL.GL_SELECT);

        gl.glInitNames(); // Initializes The Name Stack
        gl.glPushName(0); // Push 0 (At Least One Entry) Onto The Stack
        gl.glMatrixMode(GL.GL_PROJECTION); // Selects The Projection Matrix
        gl.glPushMatrix(); // Push The Projection Matrix
        gl.glLoadIdentity(); // Resets The Matrix
        // This Creates A Matrix That Will Zoom Up To A Small Portion Of The Screen, Where The Mouse Is.
        glu.gluPickMatrix((double) mouseX, (double) (viewport[3] - mouseY), 1.0f, 1.0f, viewport, 0);

        // Apply The Perspective Matrix
        glu.gluPerspective(45.0f, (float) (viewport[2] - viewport[0]) / (float) (viewport[3] - viewport[1]), 0.1f, 100.0f);
        gl.glMatrixMode(GL.GL_MODELVIEW); // Select The Modelview Matrix
        drawTargets(gl); // Render The Targets To The Selection Buffer
        gl.glMatrixMode(GL.GL_PROJECTION); // Select The Projection Matrix
        gl.glPopMatrix(); // Pop The Projection Matrix
        gl.glMatrixMode(GL.GL_MODELVIEW); // Select The Modelview Matrix
        hits = gl.glRenderMode(GL.GL_RENDER); // Switch To Render Mode, Find Out How Many

        // Objects Were Drawn Where The Mouse Was
        if (hits > 0) {
            int choose = selectBuffer.get(3);//)buffer[3];                                 // Make Our Selection The First Object
            int depth = selectBuffer.get(1);//buffer[1];                                  // Store How Far Away It Is
            for (int loop = 1; loop < hits; loop++) {
                // Loop Through All The Detected Hits
                // If This Object Is Closer To Us Than The One We Have Selected
                if (selectBuffer.get(loop * 4 + 1) /*buffer[loop * 4 + 1]*/ < depth) {
                    choose = selectBuffer.get(loop * 4 + 3);//buffer[loop * 4 + 3];          // Select The Closer Object
                    depth = selectBuffer.get(loop * 4 + 1);//buffer[loop * 4 + 1];           // Store How Far Away It Is
                }
            }

            Target target = (Target) targets.get(choose);
            if (!target.isHit()) {
                // If The Object Hasn't Already Been Hit
                target.markAsHit();             // Mark The Object As Being Hit
                score += 1;                                     // Increase Score
                kills += 1;                                     // Increase Level Kills
                if (kills > level * 5) {
                    miss = 0;                               // Misses Reset Back To Zero
                    kills = 0;                              // Reset Level Kills
                    level += 1;                             // Increase Level
                    if (level > 30) {
                        // Higher Than 30?
                        level = 30;                     // Set Level To 30 (Are You A God?)
                    }
                }
            }
        }
    }

    public void restartGame() {
        if (isGameOver) {
            restart = true;
        }
    }

    private void resetGame() {
        for (int loop = 0; loop < 30; loop++) {
            // Loop Through 30 Objects
            createNewTarget(loop); // Initialize Each Object
        }
        isGameOver = false; // Set game (Game Over) To False
        score = 0; // Set score To 0
        level = 1; // Set level Back To 1
        kills = 0; // Zero Player Kills
        miss = 0; // Set miss (Missed Shots) To 0

        isClicked = false;
    }

    /**
     * Perform motion updates here (implementing the original "void Update (DWORD milliseconds)" call from Lesson.cpp)
     * Note: I think the key handling should be preformed inside the simulation but
     * keeping it here to stay true the original c++ code
     *
     * @param milliseconds the number of ellapsed milliseconds
     */
    private void update(long milliseconds) {
        if (restart && isGameOver) {
            resetGame();
            restart = false;
        }

        roll -= milliseconds * 0.00005f; // Roll The Clouds

        for (int loop = 0; loop < level; loop++) {
            updateTarget(milliseconds, loop);
        }
    }

    private void updateTarget(long milliseconds, int loop) {
        Target object = (Target) targets.get(loop);
        boolean missed = object.update(milliseconds);
        if (missed) {
            miss++;
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

    public void createNewTarget(int num) {
        // Initialize An Object
        int targetTypeIndex = rand() % targetTypes.length;
        TargetType targetType = targetTypes[targetTypeIndex];

        Target element = new Target(
                targetType,
                level, num,
                textures[targetTypeIndex], textures[5],
                size[targetTypeIndex].width, size[targetTypeIndex].height
        );
        targets.add(num, element);
        // Sort Objects By Distance
        Collections.sort(targets);
    }

    private void removeTarget(int target) {
        if (target < targets.size()) {
            targets.remove(target);
        }
    }

    private int rand() {
        return (int) (Math.random() * Integer.MAX_VALUE);
    }

    private void drawTargets(GL gl) {
        // Draws The Targets (Needs To Be Seperate)
        gl.glLoadIdentity();                                            // Reset The Modelview Matrix
        gl.glTranslatef(0.0f, 0.0f, -10.0f);                            // Move Into The Screen 20 Units
        for (int loop = 0; loop < level; loop++) {
            gl.glLoadName(loop);                                    // Assign Object A Name (ID)
            gl.glPushMatrix();                                      // Push The Modelview Matrix
            Target target = (Target) targets.get(loop);
            target.drawTarget(gl);
            gl.glPopMatrix(); // Pop The Modelview Matrix
            if (target.isExploded()) {
                removeTarget(loop);
                createNewTarget(loop);
            }
        }
    }

    /**
     * Utility method that draw a textured quad.
     *
     * @param gl     the GL interface
     * @param width  the width of the quad
     * @param height the height of the quad
     * @param texid  the texture ID to use
     */
    public static void drawObject(GL gl, float width, float height, int texid) {
        // Draw Object Using Requested Width, Height And Texture
        gl.glBindTexture(GL.GL_TEXTURE_2D, texid); // Select The Correct Texture
        gl.glBegin(GL.GL_QUADS); // Start Drawing A Quad
        {
            gl.glTexCoord2f(0.0f, 0.0f);
            gl.glVertex3f(-width, -height, 0.0f); // Bottom Left
            gl.glTexCoord2f(1.0f, 0.0f);
            gl.glVertex3f(width, -height, 0.0f); // Bottom Right
            gl.glTexCoord2f(1.0f, 1.0f);
            gl.glVertex3f(width, height, 0.0f); // Top Right
            gl.glTexCoord2f(0.0f, 1.0f);
            gl.glVertex3f(-width, height, 0.0f); // Top Left
        }
        gl.glEnd(); // Done Drawing Quad
    }
}