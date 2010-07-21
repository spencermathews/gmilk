package gmilk;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PImage;
import codeanticode.glgraphics.GLGraphics;
import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;

@SuppressWarnings("serial")
public class Launcher extends PApplet {

	PGraphics offscreenBuffer;
	GridRenderer gridRenderer;
	Grid grid;

	GLTexture glTexture, glTextureDoubleBuffer;
	GLGraphicsOffScreen offScreenGL;

	int sizeX = 1024;
	int sizeY = 1024;

	PImage texture;
	PFont font;

	public void setup() {

		size(sizeX, sizeY, GLGraphics.GLGRAPHICS);

		frameRate(60);

		font = createFont("Arial", 12);

		glTexture = new GLTexture(this,
				sketchPath("textures/Airplane_vortex.jpg"));
		glTextureDoubleBuffer = new GLTexture(this, sizeX, sizeY);
		glTextureDoubleBuffer.copy(glTexture);

		offScreenGL = new GLGraphicsOffScreen(this, sizeX, sizeY);

		offScreenGL.getTexture().copy(glTexture);

		int gridSizeX = 33; // -1 cells to render
		int gridSizeY = 33;
		grid = new Grid(gridSizeX, gridSizeY);

		gridRenderer = new GridRenderer(this, grid);

		background(0);
	}

	public void draw() {
		glTextureDoubleBuffer.clear(255);

		gridRenderer.render(glTextureDoubleBuffer, offScreenGL);
		glTextureDoubleBuffer.copy(offScreenGL.getTexture());

		image(glTextureDoubleBuffer, 0, 0);

		System.out.println(round(frameRate));
	}

}
