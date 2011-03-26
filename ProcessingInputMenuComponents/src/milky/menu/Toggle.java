package milky.menu;

import java.util.LinkedList;

import milky.menu.events.IToggleListener;
import processing.core.PApplet;

public class Toggle extends MilkyMenuInteractiveComponent {

	private String toggleName;
	private boolean toggleState;
	private LinkedList<IToggleListener> listeners = new LinkedList<IToggleListener>();

	public Toggle(String label, boolean toggle) {
		register(this);
		toggleName = label;
		setToggle(toggle);
	}

	public LinkedList<IToggleListener> getListeners() {
		return listeners;
	}
	
	public void addListener(IToggleListener listener){
		getListeners().add(listener);
	}

	public void removeListener(IToggleListener listener){
		getListeners().remove(listener);
	}
	
	public void setToggle(boolean toggle) {
		toggleState = toggle;
		label = toggleName + " " + ((toggle) ? "[ON]" : "[OFF]");
	}

	public boolean getToggleState() {
		return toggleState;
	}

	private void updateListeners(){
		for(IToggleListener listener : listeners){
			listener.onToggleEvent(this);
		}
	}
	
	@Override
	protected void draw(PApplet context, int x, int y) {
	}

	@Override
	protected void onNonUnicodeInput() {
	}

	@Override
	protected void onUnicodeInput(char unicode) {
	}

	@Override
	protected void onActivation() {
		setToggle(!toggleState);
		updateListeners();
		setActive(false); // a toggle component must not be active
	}

	@Override
	protected void copy() {
	}

	@Override
	protected void paste() {
	}
}
