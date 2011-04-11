package milky.preset;

import java.awt.event.KeyEvent;
import java.util.LinkedList;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;

import milky.menu.JavascriptEditor;
import milky.menu.Menu;
import milky.menu.Toggle;
import milky.menu.Trigger;
import milky.menu.events.ICompilationListener;
import milky.menu.events.ITriggerListener;

import org.jdom.Element;

import processing.core.PApplet;

public class PresetEditor extends Menu {

	private String loadedPresetName = "anonymous - permaculture";

	private JavascriptEditor initScript = new JavascriptEditor("initialization script");
	private Toggle loopToggle = new Toggle("run in loop", false);
	private JavascriptEditor perFrameScript = new JavascriptEditor("per-frame script");
	private Menu minimConfig = new Menu("'Minim' Config");
	private Menu wavesMenu = new Menu("[] Scripted Waves");
	private Menu shapesMenu = new Menu("[] Scripted Shapes");
	private Menu canvasOptions = new Menu("[] Canvas Options");
	private JavascriptEditor perVertexScript = new JavascriptEditor("per-vertex script");

	private LinkedList<CustomWave> customWaves = new LinkedList<CustomWave>();
	private int waveCounter = 0;
	private int shapeCounter = 0;

	private PApplet parent;

	public PresetEditor(String label, PApplet parent) {
		super(label);
		this.parent = parent;
		initMenu();
		compilePreset();
		initializePreset();

		loopToggle.setToggleState(true); // initially run in loop
		addWave(); // start with one wave
	}

	public void setLoadedPresetName(String loadedPresetName) {
		this.loadedPresetName = loadedPresetName;
	}

	public String getLoadedPresetName() {
		return loadedPresetName;
	}

	public void renderPreset(PApplet context) {
		if (loopToggle.getToggleState()) {
			step();
		}
		for (CustomWave wave : customWaves) {
			wave.render(context);
		}
	}

	private void initMenu() {
		Trigger initializeTrigger = new Trigger("initialize");
		initializeTrigger.addListener(new ITriggerListener() {
			public void onTrigger() {
				initializePreset();
			}
		});

		Trigger singleStep = new Trigger("execute single step");
		singleStep.addListener(new ITriggerListener() {
			public void onTrigger() {
				nonUnicodesPressed.remove(KeyEvent.VK_ENTER); // XXX ??? (could possibly instantly close a script error message)
				step();
			}
		});

		initScript.addCompilationListener(new ICompilationListener() {
			public void onCompilation(ScriptEngine engine, CompiledScript compiledScript, Bindings bindings) {
				initializePreset();
			}
		});

		addMenuItem(initScript);
		addMenuItem(initializeTrigger);
		addMenuItem(perFrameScript);
		addMenuItem(singleStep);
		addMenuItem(loopToggle);
		addMenuItem(minimConfig);
		addMenuItem(wavesMenu);
		addMenuItem(shapesMenu);
		addMenuItem(canvasOptions);
		addMenuItem(perVertexScript);

		initMinimMenu();
		initWavesMenu();
		initShapesMenu();
	}

	private void initMinimMenu() {

	}

	private void initShapesMenu() {
		Trigger addShapeTrigger = new Trigger("+ New Shape");
		ITriggerListener addShapeListener = new ITriggerListener() {
			public void onTrigger() {
				shapeCounter++;
				// TODO: CustomShape
			}
		};
		addShapeTrigger.addListener(addShapeListener);
		shapesMenu.addMenuItem(addShapeTrigger);
	}

	private void initWavesMenu() {
		Trigger addWaveTrigger = new Trigger("+ New Wave");
		ITriggerListener addWaveListener = new ITriggerListener() {
			public void onTrigger() {
				addWave();
			}

		};
		addWaveTrigger.addListener(addWaveListener);
		wavesMenu.addMenuItem(addWaveTrigger);
	}

	private void addWave() {
		waveCounter++;
		CustomWave customWave = new CustomWave("~ Wave " + waveCounter);
		customWaves.add(customWave);
		wavesMenu.addMenuItem(customWave);
		// customWave.step(perFrameScript.getBindings()); // XXX: why anyway? (commented out because this line produced a bug on initialization, when
		// the per-frame code has not been run for the first time)
	}

	private void compilePreset() {
		initScript.compile();
		perFrameScript.compile();
		perVertexScript.compile();
		for (CustomWave wave : customWaves) {
			wave.compile();
		}
		// TODO: CustomShapes
	}

	protected void initializePreset() {
		initScript.execute();
		perFrameScript.execute(initScript.getBindings());
		selectedItem = loopToggle;
	}

	protected void step() {

		Bindings initBindings = initScript.getBindings();
		putPresetVariables(initBindings);

		perFrameScript.execute(initBindings);

		Bindings perFrameBindings = perFrameScript.getBindings();
		putPresetVariables(perFrameBindings);

		for (CustomWave wave : customWaves) {
			wave.step(perFrameBindings);
		}

		// TODO: shapes

		executeVertexScript();
	}

	private void putPresetVariables(Bindings initBindings) {
		initBindings.put("frame", parent.frameCount);
		initBindings.put("fps", parent.frameRate);
		initBindings.put("height", parent.height);
		initBindings.put("width", parent.width);
	}

	private void executeVertexScript() {
	}

	public void stopExecution() {
		loopToggle.setToggleState(false);
	}

	public void loadPreset(Element presetRoot) {
		setLoadedPresetName(presetRoot.getAttributeValue(PresetConstants.PRESET_NAME));
		setXML(presetRoot);
	}

	@Override
	public Element getXML() {
		Element presetRoot = new Element(PresetConstants.PRESET);

		Element initElem = new Element(PresetConstants.INIT_SCRIPT);
		initElem.addContent(initScript.getXML());

		Element perFrameElem = new Element(PresetConstants.PER_FRAME_SCRIPT);
		perFrameElem.addContent(perFrameScript.getXML());

		Element minimElem = new Element(PresetConstants.MINIM_CONFIG);
		minimElem.addContent(minimConfig.getXML());

		Element wavesElem = new Element(PresetConstants.CUSTOM_WAVES);
		for (CustomWave wave : customWaves) {
			Element waveElem = new Element(PresetConstants.CUSTOM_WAVE);
			waveElem.addContent(wave.getXML());
			wavesElem.addContent(waveElem);
		}

		Element shapesElem = new Element(PresetConstants.CUSTOM_SHAPES);
		// TODO

		Element canvasElem = new Element(PresetConstants.CANVAS_OPTIONS);
		canvasElem.addContent(canvasOptions.getXML());

		Element perVertexElem = new Element(PresetConstants.PER_VERTEX_SCRIPT);
		perVertexElem.addContent(perVertexScript.getXML());

		presetRoot.addContent(initElem);
		presetRoot.addContent(perFrameElem);
		presetRoot.addContent(minimElem);
		presetRoot.addContent(wavesElem);
		presetRoot.addContent(shapesElem);
		presetRoot.addContent(canvasElem);
		presetRoot.addContent(perVertexElem);

		return presetRoot;
	}

	@Override
	public void setXML(Element node) {
		initMenu();
		loopToggle.setToggleState(false);
		initScript.setXML(node.getChild(PresetConstants.INIT_SCRIPT));
		perFrameScript.setXML(node.getChild(PresetConstants.PER_FRAME_SCRIPT));

		for (Object wave : node.getChildren(PresetConstants.CUSTOM_WAVE)) {
			if (wave instanceof Element) {
				Element waveXML = (Element) wave;
				waveCounter++;
				CustomWave customWave = new CustomWave("~ Wave " + waveCounter); // will be overwritten by xml data ^^
				customWave.setXML(waveXML);
				customWaves.add(customWave);
				wavesMenu.addMenuItem(customWave);
			}
		}

		canvasOptions.setXML(node.getChild(PresetConstants.CANVAS_OPTIONS));
		perVertexScript.setXML(node.getChild(PresetConstants.PER_VERTEX_SCRIPT));
	}
}
