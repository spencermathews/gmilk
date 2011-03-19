package milky.menu.events;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;

public interface ICompilationListener {

	public void onCompilation(ScriptEngine engine, CompiledScript compiledScript, Bindings bindings);

}
