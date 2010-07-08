package demos.nehe.lesson40;

import demos.common.GLDisplay;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.KeyStroke;

/**
 * Modified version of Pepijn Van Eeckhoudt's "InputHandler" - Scott Rains<br/>
 * This version implements the "old school" boolean array to pass around key presses in a similar manor to
 * Erkin Tunca's original c++ code (see "LRESULT CALLBACK WindowProc (HWND hWnd, UINT uMsg, WPARAM wParam, LPARAM lParam)" in NeHeGL.cpp)
 *
 * @author Scott Rains
 */
class InputHandler implements KeyListener {

    final boolean[] g_keys;

    public InputHandler(GLDisplay glDisplay, boolean[] keys) {
        this.g_keys = keys;
        glDisplay.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "Add Velocity In +X Direction");
        glDisplay.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "Add Velocity In -X Direction");
        glDisplay.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "Add Velocity In +Z Direction");
        glDisplay.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "Add Velocity In -Z Direction");
        glDisplay.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "Add Velocity In +Y Direction");
        glDisplay.registerKeyStrokeForHelp(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "Add Velocity In -Y Direction");
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() < 256) {
            g_keys[e.getKeyCode()] = false;
        }
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() < 256) {
            g_keys[e.getKeyCode()] = true;
        }
    }
}