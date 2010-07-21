package gmilk;

import processing.core.PApplet;
import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;

public class GridRenderer {

	PApplet itsParent;
	Grid itsGrid;

	public GridRenderer(PApplet parent, Grid grid) {
		itsGrid = grid;
		itsParent = parent;
	}

	/** 
	 * renders the warped source texture to the target graphics object
	 *
	 * @param source
	 * @param target
	 */
	public void render(GLTexture source, GLGraphicsOffScreen target) {
		target.beginDraw();
		target.noStroke();
		for (int row = 0; row < itsGrid.getGridSizeY() - 1; row++) {
			renderRow(row, source, target);
		}
		target.endDraw();
	}

	/**
	 * renders one row of the grid as a GL triangle strip
	 * 
	 * @param row
	 *            - the lower of the two spanning row indexes, must be greater 0
	 * @param target 
	 */
	private final void renderRow(int row, GLTexture source, GLGraphicsOffScreen target) {
		target.beginShape(PApplet.TRIANGLE_STRIP);
		target.texture(source);
		for (int column = 0; column < itsGrid.getGridSizeX(); column++) {
			float x, y, warpX, warpY;

			x = itsGrid.getX(column, row + 1) * source.width;
			y = itsGrid.getY(column, row + 1) * source.height;
			warpX = itsGrid.getWarpX(column, row + 1) * source.width;
			warpY = itsGrid.getWarpY(column, row + 1) * source.height;

			target.vertex(x,y,0f,warpX,warpY);
			
			x = itsGrid.getX(column, row) * source.width;
			y = itsGrid.getY(column, row) * source.height;
			warpX = itsGrid.getWarpX(column, row) * source.width;
			warpY = itsGrid.getWarpY(column, row) * source.height;
			
			target.vertex(x,y,0f,warpX,warpY);
		}
		target.endShape();
	}

}
