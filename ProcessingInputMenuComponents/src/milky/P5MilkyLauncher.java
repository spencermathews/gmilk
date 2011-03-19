package milky;

import java.awt.event.KeyEvent;

import milky.menu.MilkyMenuInteractiveComponent;
import milky.preseteditor.MilkyPresetEditor;
import processing.core.PApplet;
import processing.core.PFont;

public class P5MilkyLauncher extends PApplet {
	public static final String FONT_NAME = "CourierNewPSMT-13.vlw";
	PFont font;

	MilkyPresetEditor milkyPreset;

	@Override
	public void setup() {
		size(1024, 768);
		setupMilky();
	}

	private void setupMilky() {
		font = loadFont(FONT_NAME);
		textFont(font);
		MilkyMenuInteractiveComponent.settings.fontName = FONT_NAME;
		MilkyMenuInteractiveComponent.settings.fontColor = color(255);
		MilkyMenuInteractiveComponent.settings.highlightColor = color(255, 0, 0);
		MilkyMenuInteractiveComponent.settings.backgroundColor = color(0, 0, 0, 192);

		MilkyMenuInteractiveComponent.init(this);
		
		milkyPreset = new MilkyPresetEditor("presetEditor", this);
		
		MilkyMenuInteractiveComponent.addEntryPoint(KeyEvent.VK_M, milkyPreset); // press 'm' to open menu
		addKeyListener(milkyPreset);

		milkyPreset.setActive(true);
	}

	@Override
	public void draw() {
		background(127);
		MilkyMenuInteractiveComponent.displayMenu(this, 10, 10);
		milkyPreset.renderPreset(this);
	}

}
