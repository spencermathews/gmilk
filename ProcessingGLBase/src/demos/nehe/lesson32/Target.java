package demos.nehe.lesson32;

import javax.media.opengl.GL;

class Target implements Comparable {
    private int rot; // Rotation (0-None, 1-Clockwise, 2-Counter Clockwise)

    private boolean hit; // Object Hit?

    private int frame; // Current Explosion Frame

    private int dir; // Object Direction (0-Left, 1-Right, 2-Up, 3-Down)

    private TextureImage objectTexture; // Object Texture ID
    private TextureImage explosionTexture; // Object Texture ID

    private float x; // Object X Position

    private float y; // Object Y Position

    private float spin; // Object Spin

    private float distance; // Object Distance
    private int targetIndex;
    private boolean exploded;
    private float width;
    private float height;

    public Target(
            TargetType type,
            int level, int targetIndex,
            TextureImage texture, TextureImage explosionTexture,
            float width, float height
    ) {
        this.targetIndex = targetIndex;
        this.width = width;
        this.height = height;
        exploded = false;

        rot = 1; // Clockwise Rotation
        hit = false; // Reset Object Has Been Hit Status To False
        objectTexture = texture; // Assign A New Texture
        this.explosionTexture = explosionTexture;
        distance = -((float) (rand() % 4001) / 100.0f); // Random Distance
        y = -1.5f + ((float) (rand() % 451) / 100.0f); // Random Y Position
        // Random Starting X Position Based On Distance Of Object And Random Amount For A Delay (Positive Value)
        x = ((distance - 15.0f) / 2.0f) - (5 * level) - (float) (rand() % (5 * level));
        dir = (rand() % 2);     // Pick A Random Direction
        if (dir == 0) {
            rot = 2; // Counter Clockwise Rotation
            x = -x; // Start On The Left Side (Negative Value)
        }

        if (type.equals(TargetType.FACE)) {
            // Blue Face
            y = -2.0f; // Always Rolling On The Ground
        }
        if (type.equals(TargetType.BUCKET)) {
            dir = 3; // Falling Down
            x = (float) (rand() % (int) (distance - 10.0f)) + ((distance - 10.0f) / 2.0f);
            y = 4.5f; // Random X, Start At Top Of The Screen
        }

        if (type.equals(TargetType.VASE)) {
            dir = 2; // Start Off Flying Up
            x = (float) (rand() % (int) (distance - 10.0f)) + ((distance - 10.0f) / 2.0f);
            y = -3.0f - (float) (rand() % (5 * level));     // Random X, Start Under Ground + Random Value
        }
    }

    public boolean isHit() {
        return hit;
    }

    public void markAsHit() {
        this.hit = true;
    }

    private int rand() {
        return (int) (Math.random() * Integer.MAX_VALUE);
    }

    public String toString() {
        return "X:" + x + " Y:" + y;
    }

    public int compareTo(Object o) {
        Target target = (Target) o;

        float diff = target.distance - this.distance;
        if (diff > 0) {
            return -1;
        } else if (diff < 0) {
            return 1;
        } else {
            return 0;
        }
    }

    public boolean update(long milliseconds) {
        int miss = 0;

        if (rot == 1) {
            // If Rotation Is Clockwise
            spin -= 0.2f * (float) (targetIndex + milliseconds); // Spin Clockwise
        }
        if (rot == 2) {
            // If Rotation Is Counter Clockwise
            spin += 0.2f * (float) (targetIndex + milliseconds); // Spin Counter Clockwise
        }
        if (dir == 1) {
            // If Direction Is Right
            x += 0.012f * (float) (milliseconds); // Move Right
        }
        if (dir == 0) {
            // If Direction Is Left
            x -= 0.012f * (float) (milliseconds); // Move Left
        }
        if (dir == 2) {
            // If Direction Is Up
            y += 0.012f * (float) (milliseconds); // Move Up
        }
        if (dir == 3) {
            // If Direction Is Down
            y -= 0.0025f * (float) (milliseconds); // Move Down
        }
        // If We Are To Far Left, Direction Is Left And The Was Not Hit
        if ((x < (distance - 15.0f) / 2.0f) && (dir == 0) && !hit) {
            miss += 1; // Increase miss (Missed
            hit = true; // Set hit To True To Manually Blow Up The Object
        }

        // If We Are To Far Right, Direction Is Left And The Was Not Hit
        if ((x > -(distance - 15.0f) / 2.0f) && (dir == 1) && !hit) {
            miss += 1; // Increase miss (Missed
            hit = true; // Set hit To True To Manually Blow Up The Object
        }

        // If We Are To Far Down, Direction Is Down And The Was Not Hit
        if ((y < -2.0f) && (dir == 3) && !hit) {
            miss += 1; // Increase miss (Missed
            hit = true; // Set hit To True To Manually Blow Up The Object
        }

        if ((y > 4.5f) && (dir == 2)) {
            // If We Are To Far Up And The Direction Is Up
            dir = 3; // Change The Direction To Down
        }

        return miss > 0;
    }

    public void drawTarget(GL gl) {
        gl.glTranslatef(x, y, distance);   // Position The Object (x,y)
        if (isHit()) {
            drawExplosion(gl);                        // Draw An Explosion
        } else {
            gl.glRotatef(spin, 0.0f, 0.0f, 1.0f);    // Rotate The Object
            Renderer.drawObject(gl, width, height, objectTexture.getTexID()); // Draw The Object
        }
    }

    private void drawExplosion(GL gl) {
        // Draws An Animated Explosion For Object "num"
        float ex = (float) ((frame / 4) % 4) / 4.0f;   // Calculate Explosion X Frame (0.0f - 0.75f)
        float ey = (float) ((frame / 4) / 4) / 4.0f;   // Calculate Explosion Y Frame (0.0f - 0.75f)
        gl.glBindTexture(GL.GL_TEXTURE_2D, explosionTexture.getTexID());       // Select The Explosion Texture
        gl.glBegin(GL.GL_QUADS);                                        // Begin Drawing A Quad
        {
            gl.glTexCoord2f(ex, 1.0f - (ey));
            gl.glVertex3f(-1.0f, -1.0f, 0.0f);                      // Bottom Left
            gl.glTexCoord2f(ex + 0.25f, 1.0f - (ey));
            gl.glVertex3f(1.0f, -1.0f, 0.0f);                       // Bottom Right
            gl.glTexCoord2f(ex + 0.25f, 1.0f - (ey + 0.25f));
            gl.glVertex3f(1.0f, 1.0f, 0.0f);                        // Top Right
            gl.glTexCoord2f(ex, 1.0f - (ey + 0.25f));
            gl.glVertex3f(-1.0f, 1.0f, 0.0f);                       // Top Left
        }
        gl.glEnd(); // Done Drawing Quad
        frame += 1;                                // Increase Current Explosion Frame
        if (frame > 63) {
            exploded = true;
        }
    }

    public boolean isExploded() {
        return exploded;
    }
}
