package milky.menu;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;

import processing.core.PApplet;

public class Menu extends MilkyMenuInteractiveComponent {

	protected String prefix = "<";
	protected String suffix = ">";
	protected ArrayList<MilkyMenuInteractiveComponent> menuItems = new ArrayList<MilkyMenuInteractiveComponent>();
	protected MilkyMenuInteractiveComponent selectedItem;

	public Menu(String label) {
		this.label = label;
		register(this);
	}

	public void addMenuItem(MilkyMenuInteractiveComponent item) {
		if (selectedItem == null) {
			selectedItem = item;
		}
		if (!menuItems.contains(item)) {
			item.parent = this;
			menuItems.add(item);
		}
	}

	public void removeItem(MilkyMenuInteractiveComponent item) {
		menuItems.remove(item);
		if (item.parent == this) {
			item.parent = null;
		}
	}

	@Override
	protected void draw(PApplet context, int x, int y) {
		int m = settings.margin;
		String[] labels = getItemLabels();
		int line = 0;
		int w = settings.fontHeight * labels.length;
		int h = settings.fontWidth * getMaxLabelCharLength(labels);
		drawBackground(context, x, y, h + 2 * m, w + 2 * m);
		for (MilkyMenuInteractiveComponent item : menuItems) {
			if (item == selectedItem) {
				context.fill(settings.highlightColor);
			} else {
				context.fill(settings.fontColor);
			}
			context.text(labels[line], x + m, y + settings.fontHeight * (line + 1));
			line++;
		}
	}

	@Override
	protected void onNonUnicodeInput() {
		if (nonUnicodesPressed.contains(KeyEvent.VK_UP)) {
			up();
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_DOWN)) {
			down();
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_ENTER) || nonUnicodesPressed.contains(KeyEvent.VK_RIGHT)) {
			if (selectedItem != null)
				selectedItem.setActive(true);
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_ESCAPE) | nonUnicodesPressed.contains(KeyEvent.VK_BACK_SPACE)
				| nonUnicodesPressed.contains(KeyEvent.VK_LEFT)) {
			close();
		}
	}

	private void up() {
		if (selectedItem != null) {
			int currentIndex = menuItems.indexOf(selectedItem);
			if (currentIndex > 0) {
				selectedItem = menuItems.get(currentIndex - 1);
			}
		}

	}

	private void down() {
		if (selectedItem != null) {
			int currentIndex = menuItems.indexOf(selectedItem);
			if (currentIndex < menuItems.size() - 1) {
				selectedItem = menuItems.get(currentIndex + 1);
			}
		}
	}

	@Override
	protected void onUnicodeInput(char unicode) {
		// TODO Auto-generated method stub

	}

	private int getMaxLabelCharLength(String[] labels) {
		int maxLabelLength = 0;
		for (int i = 0; i < labels.length; i++) {
			int length = labels[i].length();
			maxLabelLength = Math.max(maxLabelLength, length);
		}
		return maxLabelLength;
	}

	private String[] getItemLabels() {
		LinkedList<String> labels = new LinkedList<String>();
		for (MilkyMenuInteractiveComponent item : menuItems) {
			labels.add(item.prefix + item.label + item.suffix);
		}
		return labels.toArray(new String[] {});
	}

}
