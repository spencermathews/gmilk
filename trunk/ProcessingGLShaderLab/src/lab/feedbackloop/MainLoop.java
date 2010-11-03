package lab.feedbackloop;

import java.io.File;
import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PFont;
import codeanticode.glgraphics.GLConstants;
import codeanticode.glgraphics.GLGraphics;
import codeanticode.glgraphics.GLGraphicsOffScreen;
import codeanticode.glgraphics.GLTexture;
import codeanticode.glgraphics.GLTextureFilter;
import codeanticode.glgraphics.GLTextureParameters;

public class MainLoop extends PApplet {

	// main feedback-loop fields
	GLTexture mainTexture0, mainTexture1;
	GLTextureFilter copyFilter;
	boolean bufferFlip;
	boolean halted;
	ArrayList<GLTextureFilter> advanceFilterCascade;
	ArrayList<GLTextureFilter> compositeFilterCascade;

	// effects fields
	GLTextureFilter advanceColorFadeOutFilter;
	ArrayList<GLTextureFilter> blurFilterCascade;
	GLTexture helperTexture, blur1, blur2, blur3, static_noise_hq, static_noise_lq;
	GLTexture airplaneTexture;

	// global variables
	public float randFrame;
	public float time;
	public PFont font;
	public float[] pixelSize;

	// helping variables
	private String filterPath;
	private String texturePath;
	private int oldMouseX;
	private int oldMouseY;
	private GLGraphicsOffScreen offScreenGraphics;
	private boolean hasSizeBeenSet = false;

	// UI
	// TODO: presetNameLabel
	private boolean showPresetNameLabel;
	// TODO: fpsLabel
	private boolean showFPSLabel;
	// TODO: hintOverlay
	private boolean showHint;
	// TODO: helpOverlay
	private boolean showHelp;
	// TODO: presetListOverlay
	private boolean showPresetList;
	// TODO: menuOverlay
	private boolean showMenu;
	// TODO: editorOverlay
	private boolean showEditor;
	// TODO: presetErrorLabel
	private boolean showErrorLabel;

	// launch configuration
	int canvasWidth = 1024;
	int canvasHeight = 1024;
	float desiredFPS = 60;
	boolean captureVideo = false;

	public void setup() {

		if (captureVideo) {
			canvasWidth = 854;
			canvasHeight = 480; // 16:9 video 480p
		}

		if (!hasSizeBeenSet) {
			hasSizeBeenSet = true;
			size(canvasWidth, canvasHeight, GLConstants.GLGRAPHICS);
		}
		frameRate(desiredFPS);
		halted = false;

		font = createFont("Lucida Console", 16); // XXX: use loadFont() for an external .vlw font.
		textFont(font);

		pixelSize = new float[] { 1.0f / canvasWidth, 1.0f / canvasHeight };

		filterPath = (new File(sketchPath)).getParent() + "/data/filters/";
		texturePath = (new File(sketchPath)).getParent() + "/data/textures/";

		initializeUIElements();
		initializeGLTextures();
		initializeGLTextureFilters();

		offScreenGraphics = new GLGraphicsOffScreen(this, canvasWidth, canvasHeight);

		airplaneTexture.filter(copyFilter, mainTexture1); // initialize with a background image
		// static_noise_hq.filter(copyFilter, mainTexture1); // initialize with a noise image
	}

	private void initializeUIElements() {
		// TODO
	}

	final private void initializeGLTextureFilters() {
		copyFilter = new GLTextureFilter(this, filterPath + "copy.xml");

		advanceColorFadeOutFilter = new GLTextureFilter(this, filterPath + "advanceColorFadeOut.xml");
		advanceColorFadeOutFilter.setParameterValue("factor", 1.0f);
		advanceColorFadeOutFilter.setParameterValue("increment", -0.004f);

		GLTextureFilter blurHorizontalPass = new GLTextureFilter(this, filterPath + "blurHorizontalPass.xml");
		GLTextureFilter blurVerticalPass = new GLTextureFilter(this, filterPath + "blurVerticalPass.xml");
		blurHorizontalPass.setParameterValue("pixelSize", pixelSize);
		blurVerticalPass.setParameterValue("pixelSize", pixelSize);
		blurFilterCascade = new ArrayList<GLTextureFilter>();
		blurFilterCascade.add(blurHorizontalPass);
		blurFilterCascade.add(blurVerticalPass);

		// XXX: deeper blur levels, how?

		advanceFilterCascade = new ArrayList<GLTextureFilter>();
		advanceFilterCascade.add(blurHorizontalPass);
		advanceFilterCascade.add(blurVerticalPass);
		advanceFilterCascade.add(advanceColorFadeOutFilter);

		compositeFilterCascade = new ArrayList<GLTextureFilter>();
		compositeFilterCascade.add(copyFilter);
	}

	private void initializeGLTextures() {
		airplaneTexture = new GLTexture(this, texturePath + "Airplane_vortex.jpg");

		GLTextureParameters gp = new GLTextureParameters();
		gp.minFilter = GLGraphics.LINEAR; // XXX: could use NEAREST for best pixel access
		gp.magFilter = GLGraphics.LINEAR;

		mainTexture0 = new GLTexture(this, canvasWidth, canvasHeight, gp);
		mainTexture1 = new GLTexture(this, canvasWidth, canvasHeight, gp);
		helperTexture = new GLTexture(this, canvasWidth, canvasHeight, gp);

		static_noise_hq = new GLTexture(this, canvasWidth, canvasHeight, gp);
		static_noise_hq.loadPixels();
		for (int y = 0; y < canvasHeight; y++) {
			for (int x = 0; x < canvasWidth; x++) {
				static_noise_hq.pixels[y * canvasWidth + x] = color(random(255), random(255), random(255));
			}
		}
		static_noise_hq.loadTexture();

		blur1 = new GLTexture(this, canvasWidth, canvasHeight, gp);
		// XXX: blur2, blur3, noise, ...

	}

	public void draw() {
		GLTexture previousFrame, newFrame;
		if (bufferFlip) {
			previousFrame = mainTexture0;
			newFrame = mainTexture1;
		} else {
			previousFrame = mainTexture1;
			newFrame = mainTexture0;
		}

		if (halted) {
			applyFilterCascade(previousFrame, newFrame, null); // don't advance, only copy
		} else {
			advanceFeedbackLoop(previousFrame, newFrame);
			drawIntoFeedbackLoop(newFrame);
			calculateBlur(previousFrame);
			applyFilterCascade(newFrame, previousFrame, compositeFilterCascade); // previousFrame will be overwritten anyway
		}

		previousFrame.render(0, 0, (float) canvasWidth, (float) canvasHeight); // render the composition

		drawOverlays(previousFrame);
		bufferFlip = !bufferFlip;
	}

	private void advanceFeedbackLoop(GLTexture previousFrame, GLTexture newFrame) {
		applyFilterCascade(previousFrame, newFrame, advanceFilterCascade); // advance frame
	}

	private void calculateBlur(GLTexture source) {
		applyFilterCascade(source, blur1, blurFilterCascade);
	}

	private void applyFilterCascade(GLTexture source, GLTexture target, ArrayList<GLTextureFilter> filterCascade) {

		if (filterCascade == null) {
			filterCascade = new ArrayList<GLTextureFilter>();
		}

		if (filterCascade.size() < 1) {
			// empty filter list means copy
			source.filter(copyFilter, target);
			return;
		}

		boolean swap;
		if (filterCascade.size() % 2 == 1) {
			source.filter(filterCascade.get(0), target);
			swap = false;
		} else {
			source.filter(filterCascade.get(0), helperTexture);
			swap = true;
		}

		GLTexture buffer, result;

		for (int filterIndex = 1; filterIndex < filterCascade.size(); filterIndex++) {
			if (swap) {
				result = target;
				buffer = helperTexture;
			} else {
				result = helperTexture;
				buffer = target;
			}
			buffer.filter(filterCascade.get(filterIndex), result);
			swap = !swap;
		}
	}

	private void drawOverlays(GLTexture compositeTexture) {
		// draw UI here
		// TODO
		text("FPS: " + floor(frameRate), 50, 50);
	}

	private void drawIntoFeedbackLoop(GLTexture feedbackLoopTexture) {
		offScreenGraphics.beginDraw();
		offScreenGraphics.clear(0, 0); // initialize with full transparent
		// TODO: draw in waveforms and shapes here
		offScreenGraphics.noStroke();
		offScreenGraphics.fill(255, 255);
		// offScreenGraphics.textFont(font);
		offScreenGraphics.ellipse(mouseX, mouseY, 40, 40);
		offScreenGraphics.endDraw();
		feedbackLoopTexture.copy(offScreenGraphics.getTexture()); // copy overlay texture into feedback loop
	}

	public static void main(String args[]) {
		PApplet.main(new String[] { "--present", "lab.feedbackloop.MainLoop" });
	}

}
