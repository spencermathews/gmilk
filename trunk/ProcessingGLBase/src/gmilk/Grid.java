package gmilk;

public class Grid {
	final private int gridSizeY, gridSizeX;
	final float[] nodesDx;
	final float[] nodesDy;

	/**
	 * The Grid-Type
	 * @param sizeX - number of vertical  grid nodes
	 * @param sizeY - number of horizontal grid nodes
	 * @param Hscale - output scaling, horizontally
	 * @param Vscale - output scaling, vertically
	 */
	public Grid(int sizeX, int sizeY) {
		gridSizeX = sizeX;
		gridSizeY = sizeY;
		nodesDx = new float[sizeY * sizeX];
		nodesDy = new float[sizeY * sizeX];
	}
	
	public int getGridSizeX(){
		return gridSizeX;
	}
	
	public int getGridSizeY(){
		return gridSizeY;
	}
	
	final private int getIndex(int x, int y) {
		return y * gridSizeX + x;
	}

	public float getX(int x, int y) {
		return (float) x/(gridSizeX-1);
	}
	
	public float getY(int x, int y) {
		return (float) y/(gridSizeY-1);
	}
	
	public float getDx(int x, int y){
		return nodesDx[getIndex(x, y)];
	}
	
	public float getDy(int x, int y){
		return nodesDy[getIndex(x, y)];
	}
	
	public float getWarpX(int x, int y){
		return getX(x, y) + getDx(x, y);
	}
	
	public float getWarpY(int x, int y){
		return getY(x, y) + getDy(x, y);
	}

	public void setWarpX(int x, int y, float warpX, float warpY) {
		int i = getIndex(x, y);
		nodesDx[i] = warpX;
		nodesDy[i] = warpY;
		
	}
	

}
