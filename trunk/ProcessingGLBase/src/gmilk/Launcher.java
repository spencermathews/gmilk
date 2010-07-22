package gmilk;

import processing.core.PApplet;
import processing.core.PFont;
import codeanticode.glgraphics.GLGraphics;
import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;

@SuppressWarnings("serial")
public class Launcher extends PApplet {

	NavierStokesSolver fluidSolver;
	GridRenderer gridRenderer;
	Grid grid;

	GLTexture airplaneTexture, doubleBuffer;
	GLGraphicsOffScreen offScreenGraphics;

	int sizeX = 1024;
	int sizeY = 1024;

	int gridSizeX = 64; // 33 means 32 cells to render
	int gridSizeY = 64;

	PFont font;
	int rainbow = 0;
	int bordercolor;

	int oldMouseX, oldMouseY, mouseDx, mouseDy;

	public void setup() {
		size(sizeX, sizeY, GLGraphics.GLGRAPHICS);
		frameRate(60);

		font = createFont("Arial", 18);
		textFont(font);

		airplaneTexture = new GLTexture(this,
				sketchPath("textures/Airplane_vortex.jpg"));

		doubleBuffer = new GLTexture(this, sizeX, sizeY);
		offScreenGraphics = new GLGraphicsOffScreen(this, sizeX, sizeY, false);
		
		fluidSolver = new NavierStokesSolver();
		
		grid = new Grid(gridSizeX, gridSizeY);
		gridRenderer = new GridRenderer(this, grid);

		doubleBuffer.clear(0);

		doubleBuffer.copy(airplaneTexture);
	}

	public void draw() {
		update();
		gridRenderer.render(doubleBuffer, offScreenGraphics);

		drawBorders();

		doubleBuffer.copy(offScreenGraphics.getTexture());
		
		image(doubleBuffer, 0, 0);

		String fps = "fps: " + round(frameRate);
//		System.out.println(fps);
		text(fps, 5, 25);
	}

	private void update() {
		mouseDx = mouseX - oldMouseX;
		mouseDy = mouseY - oldMouseY;

		int cellWidth = (sizeX / (gridSizeX - 1));
		int cellHeight = (sizeY / (gridSizeY - 1));
		int nodeX = (mouseX + cellWidth / 2) * (gridSizeX - 1) / sizeX;
		int nodeY = (mouseY + cellHeight / 2) * (gridSizeY - 1) / sizeY;

		float dt = 1.0f / frameRate;
		float visc = 0.00f;
		float diff = 0.1f;

		float v = -0.002f;
		float vx = mouseDx*v;
		float vy = mouseDy*v;
		
		fluidSolver.setWarp(nodeX, nodeY, vx, vy);
		fluidSolver.tick(dt, visc, diff);
		for (int y = 0; y<gridSizeY; y++){
			for( int x = 0; x < gridSizeX; x++){
				float warpX = fluidSolver.getDx(x, y); 
				float warpY = fluidSolver.getDy(x, y); 
				grid.setWarpX(x, y, warpX, warpY);
			}
		}
		
		oldMouseX = mouseX;
		oldMouseY = mouseY;
	}


	private void drawBorders() {
		rainbow++;
		rainbow = (rainbow > 255) ? 0 : rainbow;
		colorMode(HSB);
		int rgb = color(rainbow, 255, 255);
		float r, g, b;
		r = red(rgb);
		g = green(rgb);
		b = blue(rgb);
		colorMode(RGB);
		bordercolor = color(r, g, b);

		offScreenGraphics.beginDraw();
		offScreenGraphics.strokeWeight(10);
		offScreenGraphics.stroke(bordercolor);
		offScreenGraphics.line(0, 0, width - 1, 0);
		offScreenGraphics.line(0, 0, 0, height - 1);
		offScreenGraphics.line(width - 1, 0, width - 1, height - 1);
		offScreenGraphics.line(0, height - 1, width - 1, height - 1);
		offScreenGraphics.endDraw();
	}

}
