package lab.feedbackloop;

public class FPSLabel {

	private MainLoop parent;
	private String fps;

	public FPSLabel(MainLoop parent) {
		this.parent = parent;
	}

	public void SetFps(String fps) {
		this.fps = fps;
	}

	public void draw() {
		int height = 32;
		int width = 120;
		int margin = 10;
		int posX = parent.canvasWidth - width - margin;
		int posY = margin;
		parent.fill(parent.color(0, 0, 0, 128));
		parent.rect(posX, posY, width, height);
		parent.fill(255);
		parent.text("FPS: " + fps, posX + margin, posY + height - margin);
	}

}
