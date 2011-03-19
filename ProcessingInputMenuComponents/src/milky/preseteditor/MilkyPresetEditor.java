package milky.preseteditor;

import java.awt.event.KeyEvent;
import java.util.LinkedList;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;

import milky.menu.Menu;
import milky.menu.ScriptEditor;
import milky.menu.Toggle;
import milky.menu.Trigger;
import milky.menu.events.ICompilationListener;
import milky.menu.events.ITriggerListener;
import processing.core.PApplet;

public class MilkyPresetEditor extends Menu {

	private ScriptEditor initScript = new ScriptEditor("initialization script");
	private Toggle loopToggle = new Toggle("run in loop", false);
	private ScriptEditor perFrameScript = new ScriptEditor("per-frame script");
	private Menu minimMenu = new Menu("'Minim' Config");
	private Menu wavesMenu = new Menu("[] Scripted Waves");
	private Menu shapesMenu = new Menu("[] Scripted Shapes");
	private Menu canvasOptions = new Menu("[] Canvas Options");
	private ScriptEditor perVertexScript = new ScriptEditor("per-vertex script");

	private LinkedList<CustomWave> customWaves = new LinkedList<CustomWave>();
	private int waveCounter = 0;
	private int shapeCounter = 0;
	
	private PApplet parent;

	public MilkyPresetEditor(String label, PApplet parent) {
		super(label);
		this.parent = parent;
		initMenu();
		compilePreset();
		initializePreset();
		
		loopToggle.setToggle(true); // initially run in loop
		addWave(); // start with one wave
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
				nonUnicodesPressed.remove(KeyEvent.VK_ENTER); // XXX ???
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
		addMenuItem(minimMenu);
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
		customWave.step(perFrameScript.getBindings());
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

}
