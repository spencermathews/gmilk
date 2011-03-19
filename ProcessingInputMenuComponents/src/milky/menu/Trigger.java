package milky.menu;

import java.util.LinkedList;

import milky.menu.events.ITriggerListener;

import processing.core.PApplet;

public class Trigger extends MilkyMenuInteractiveComponent implements ITriggerListener {
	
	private LinkedList<ITriggerListener> listeners = new LinkedList<ITriggerListener>();
	
	public Trigger(String label) {
		this.label = label;
		register(this);
	}
	
	public void addListener(ITriggerListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(ITriggerListener listener){
		listeners.remove(listener);
	}
	
	@Override
	public void onTrigger() {
		updateListeners(); // Trigger components can be cascaded		
	}

	private void updateListeners(){
		for(ITriggerListener listener : listeners){
			listener.onTrigger();
		}
	}

	@Override
	protected void onActivation() {
		updateListeners(); // activating a trigger component triggers all listeners
		setActive(false); // a trigger component must not be active itself
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

}
