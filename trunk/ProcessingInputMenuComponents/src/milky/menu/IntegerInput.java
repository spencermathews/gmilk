package milky.menu;

import java.awt.event.KeyEvent;
import java.util.LinkedList;

import milky.menu.events.IIntegerInputListener;

import processing.core.PApplet;

public class IntegerInput extends MilkyMenuInteractiveComponent {

	private int value, max, min;
	private String varName;
	private LinkedList<IIntegerInputListener> listeners = new LinkedList<IIntegerInputListener>();

	public IntegerInput(String varName, int min, int max) {
		this.min = min;
		this.max = max;
		this.varName = varName;
		setValue(min);
		register(this);

	}

	@Override
	protected void draw(PApplet context, int x, int y) {
		String tooltip = " Use cursor keys or page up/down to change value";
		String varLabel = " Current value of '" + varName + "': ";
		int maxLength = Math.max(tooltip.length(), varLabel.length()) + 1;
		int m = settings.margin;
		int height = settings.fontHeight * 3;
		int width = settings.fontWidth * maxLength;
		drawBackground(context, x, y, width + 2 * m, height + 2 * m);
		context.fill(settings.fontColor);
		context.text(tooltip, x + m, y + m + settings.fontHeight);
		context.text(varLabel, x + m, y + m + settings.fontHeight * 2);
		context.fill(settings.highlightColor);
		context.text("  " + getValue(), x + m, y + m + settings.fontHeight * 3);
	}

	@Override
	protected void onNonUnicodeInput() {
		if (nonUnicodesPressed.contains(KeyEvent.VK_ESCAPE) || nonUnicodesPressed.contains(KeyEvent.VK_ENTER)) {
			close();
			return;
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_UP)) {
			setValue(value + 1);
			updateListeners();
			return;
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_DOWN)) {
			setValue(value - 1);
			updateListeners();
			return;
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_PAGE_UP)) {
			setValue(value + 10);
			updateListeners();
			return;
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_PAGE_DOWN)) {
			setValue(value - 10);
			updateListeners();
			return;
		}
	}

	@Override
	protected void onUnicodeInput(char unicode) {
		// Do nothing
	}

	public void setValue(int value) {
		this.value = Math.min(max, Math.max(value, min));
		label = varName + " [" + this.value + "]";
	}

	public int getValue() {
		return value;
	}

	public LinkedList<IIntegerInputListener> getListeners() {
		return listeners;
	}
	
	public void addListener(IIntegerInputListener listener){
		listeners.add(listener);
	}

	public void removeListener(IIntegerInputListener listener){
		listeners.add(listener);
	}
	
	private void updateListeners(){
		for(IIntegerInputListener listener : listeners){
			listener.onInput(value);
		}
	}

}
