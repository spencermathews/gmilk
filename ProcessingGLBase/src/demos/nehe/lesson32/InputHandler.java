package demos.nehe.lesson32;

import demos.common.GLDisplay;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.KeyStroke;

class InputHandler implements KeyListener, MouseListener, MouseMotionListener {
    private final Renderer renderer;

    public InputHandler(GLDisplay glDisplay, Renderer renderer) {
        this.renderer = renderer;
        glDisplay.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "Restart game");
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_SPACE:
                renderer.restartGame();
                break;
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        renderer.setPickPoint(e.getPoint(), true);
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
        renderer.setPickPoint(e.getPoint(), false);
    }
}