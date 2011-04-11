package milky.menu;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.jdom.Element;

import milky.menu.events.ICompilationListener;

public class JavascriptEditor extends TextInput {

	protected ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");
	protected Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
	protected Compilable compiler;
	protected CompiledScript compiledScript;

	private LinkedList<ICompilationListener> compilationListeners = new LinkedList<ICompilationListener>();

	public JavascriptEditor(String label) {
		super(label);
		lines.set(0, "// javascript");
		if (engine instanceof Compilable) {
			compiler = (Compilable) engine;
		}
		compile();
	}

	@Override
	public Element getXML() {
		Element textElem = super.getXML();
		Element javascriptElem = new Element("javascript");
		javascriptElem.setAttribute("label", textElem.getAttributeValue("label"));
		javascriptElem.setText(textElem.getText());
		return javascriptElem;
	}

	public LinkedList<ICompilationListener> getListeners() {
		return compilationListeners;
	}

	public void addCompilationListener(ICompilationListener compilationListener) {
		compilationListeners.add(compilationListener);
	}

	public void removeListener(ICompilationListener compilationListener) {
		compilationListeners.remove(compilationListener);
	}

	private void updateCompilationListeners() {
		for (ICompilationListener compilationListener : compilationListeners) {
			compilationListener.onCompilation(engine, compiledScript, bindings);
		}
	}

	@Override
	protected void saveAndClose() {
		compileAndClose();
	}

	protected void compileAndClose() {
		if (compile()) {
			super.saveAndClose();
			updateCompilationListeners();
		}
	}

	public boolean compile() {
		String script = getTextAsString();
		try {
			compiledScript = compiler.compile(script);
		} catch (ScriptException e) {
			onScriptError(e);
			return false;
		}
		return true;
	}

	public void execute() {
		try {
			compiledScript.eval();
		} catch (ScriptException e) {
			onScriptError(e);
			return;
		}
	}

	public void execute(Bindings bindings) {
		try {
			for (String key : bindings.keySet()) {
				engine.put(key, bindings.get(key));
			}
			compiledScript.eval();
		} catch (ScriptException e) {
			onScriptError(e);
			return;
		}
	}

	@Override
	protected void onNonUnicodeInput() {
		super.onNonUnicodeInput();
		if (nonUnicodesPressed.contains(KeyEvent.VK_F5)) {
			compile();
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_F6)) {
			execute();
		}
	}

	private static final TextInput scriptError = new TextInput("script error");

	protected void onScriptError(ScriptException e) {
		setStatus("script error in " + getLabel());
		System.err.print(e);
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("Script Error: " + getLabel());
		lines.add("line: " + e.getLineNumber());
		lines.add("column: " + e.getColumnNumber());
		lines.add("text: " + e.toString());
		lines.add("message: " + e.getMessage());
		scriptError.setText(lines);
		scriptError.setParent(this);
		scriptError.setActive(true);
	}

	public Bindings getBindings() {
		return bindings;
	}

}
