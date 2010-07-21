package gmilk;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import codeanticode.glgraphics.GLGraphics;
import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;

@SuppressWarnings("serial")
public class Launcher extends PApplet {

	GridRenderer gridRenderer;
	Grid grid;

	GLTexture airplaneTexture, doubleBuffer;
	GLGraphicsOffScreen offScreenGraphics;

	int sizeX = 1024;
	int sizeY = 1024;

	int gridSizeX = 65; // 33 means 32 cells to render
	int gridSizeY = 65;

	PFont font;

	int oldMouseX, oldMouseY;

	public void setup() {

		size(sizeX, sizeY, GLGraphics.GLGRAPHICS);

		frameRate(60);

		font = createFont("Arial", 18);
		textFont(font);

		airplaneTexture = new GLTexture(this,
				sketchPath("textures/Airplane_vortex.jpg"));

		doubleBuffer = new GLTexture(this, sizeX, sizeY);
		offScreenGraphics = new GLGraphicsOffScreen(this, sizeX, sizeY, true, 8);
		grid = new Grid(gridSizeX, gridSizeY);
		gridRenderer = new GridRenderer(this, grid);

		doubleBuffer.clear(0);
		
		doubleBuffer.copy(airplaneTexture);
	}

	private void handleMouseMotion() {
		int mouseDx = mouseX - oldMouseX;
		int mouseDy = mouseY - oldMouseY;

		int cellWidth = (sizeX / (gridSizeX - 1));
		int cellHeight = (sizeY / (gridSizeY - 1));
		int nodeX = (mouseX + cellWidth / 2) * (gridSizeX - 1) / sizeX;
		int nodeY = (mouseY + cellHeight / 2) * (gridSizeY - 1) / sizeY;

		indicateNodeOnCanvas(nodeX, nodeY, cellWidth, cellHeight);

		oldMouseX = mouseX;
		oldMouseY = mouseY;
	}

	private void indicateNodeOnCanvas(int nodeX, int nodeY, int cellWidth,
			int cellHeight) {
		int centerX = nodeX * sizeX / (gridSizeX - 1);
		int centerY = nodeY * sizeY / (gridSizeY - 1);
		offScreenGraphics.beginDraw();
		offScreenGraphics.fill(color(0, 0, 255));
		offScreenGraphics.rect(centerX - cellWidth / 2, centerY - cellHeight
				/ 2, cellWidth, cellHeight);
		offScreenGraphics.endDraw();
	}

	public void draw() {
		gridRenderer.render(doubleBuffer, offScreenGraphics);

		handleMouseMotion();

		doubleBuffer.copy(offScreenGraphics.getTexture());

		image(doubleBuffer, 0, 0);
		text("fps: " + round(frameRate), 5, 25);
	}
}
