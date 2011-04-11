package milky;

import java.awt.event.KeyEvent;
import java.io.File;

import milky.menu.InteractiveMenuComponent;
import milky.menu.Trigger;
import milky.menu.events.ITriggerListener;
import milky.preset.PresetEditor;
import milky.preset.PresetLoader;
import processing.core.PApplet;
import processing.core.PFont;

@SuppressWarnings("serial")
public class P5MilkyLauncher extends PApplet {
	public static final String FONT_NAME = "CourierNewPSMT-13.vlw";
	PFont font;

	PresetEditor presetEditor;
	PresetLoader presetLoader;

	@Override
	public void setup() {
		size(1024, 768);
		setupMilky();
	}

	private void setupMilky() {
		font = loadFont(FONT_NAME);
		textFont(font);
		InteractiveMenuComponent.settings.fontName = FONT_NAME;
		InteractiveMenuComponent.settings.fontColor = color(255);
		InteractiveMenuComponent.settings.highlightColor = color(255, 0, 0);
		InteractiveMenuComponent.settings.highlightColor2 = color(0, 255, 255);
		InteractiveMenuComponent.settings.backgroundColor = color(0, 0, 0, 192);
		InteractiveMenuComponent.settings.backgroundColor2 = color(0, 0, 0, 64);

		InteractiveMenuComponent.init(this);

		presetEditor = new PresetEditor("presetEditor", this);

		addKeyListener(presetEditor); // the preset editor handles key input. to be more correct: the abstract superclass implements a static listener
										// for all the menu components

		InteractiveMenuComponent.addEntryPoint(KeyEvent.VK_M, presetEditor); // press 'm' to open menu

		File binDir = new File("");
		String binPath = binDir.getAbsolutePath();
		String presetDirPath = binPath.substring(0, binPath.lastIndexOf(File.separator) + 1) + "presets";

		presetLoader = new PresetLoader(presetDirPath, presetEditor);

		InteractiveMenuComponent.addEntryPoint(KeyEvent.VK_L, presetLoader); // press 'l' to open preset list

		Trigger savePresetTrigger = new Trigger("save");
		savePresetTrigger.addListener(new ITriggerListener() {
			public void onTrigger() {
				presetLoader.promptFileNameAndSavePreset();
			}
		});

		InteractiveMenuComponent.addEntryPoint(KeyEvent.VK_S, savePresetTrigger); // press 's' to save preset

		// presetEditor.setActive(true);
	}

	@Override
	public void draw() {
		background(127);
		InteractiveMenuComponent.displayMenu(this, 10, 10);
		presetEditor.renderPreset(this);
	}

}
