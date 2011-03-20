package milky.menu;

import java.awt.event.KeyEvent;
import java.util.ArrayList;

import processing.core.PApplet;

import com.dnsalias.java.timer.NativeTimer;
import com.dnsalias.java.timer.nanotimer.NanoTimer;

/**
 * A Multiline Text editor component for Processing.
 * 
 * @author Felix Woitzel, Feb 2011
 */
public class TextInput extends MilkyMenuInteractiveComponent {

	private static final String LINEBREAK = "\n";

	protected ArrayList<String> lines = new ArrayList<String>();

	int lineIndex = 0;
	int charIndex = 0;

	private int selectionBeginLineIndex = -1;
	private int selectionBeginCharIndex = -1;
	private int selectionEndLineIndex = -1;
	private int selectionEndCharIndex = -1;

	private boolean selection = false;

	public TextInput(String label) {
		register(this);
		this.prefix = "[";
		this.label = label;
		this.suffix = "]";
		lines.add("");
	}

	public void setText(ArrayList<String> text) {
		lines = text;
	}

	public String getTextAsString() {
		String string = "";
		for (String line : lines) {
			string += line + LINEBREAK;
		}

		return string;
	}

	protected int getMaxLineLength() {
		int maxLineLength = 0;
		for (int i = 0; i < lines.size(); i++) {
			int length = lines.get(i).length() + ((selectionBeginLineIndex == i) ? 1 : 0) + ((selectionEndLineIndex == i) ? 1 : 0);
			maxLineLength = Math.max(maxLineLength, length);
		}
		return maxLineLength + 1;
	}

	@Override
	protected void draw(PApplet context, int x, int y) {
		drawBackground(context, x, y, (getMaxLineLength() + 3) * settings.fontWidth + 2 * settings.margin, lines.size() * settings.fontHeight + 2
				* settings.margin);
		int lineH = 0;
		for (String l : lines) {
			String line = new String(l);
			if (lineH == lineIndex) {
				context.fill(settings.highlightColor);
			} else {
				context.fill(settings.fontColor);
			}
			if (selection) {
				int selectionIncrement = 0;
				if (lineH == selectionBeginLineIndex) {
					String head = line.substring(0, selectionBeginCharIndex);
					String tail = line.substring(selectionBeginCharIndex);
					selectionIncrement += 1;
					line = head + "[" + tail;
				}
				if (lineH == selectionEndLineIndex) {
					String head = line.substring(0, selectionEndCharIndex + selectionIncrement);
					String tail = line.substring(selectionEndCharIndex + selectionIncrement);
					selectionIncrement += 1;
					line = head + "]" + tail;
				}
			} else if (lineH == lineIndex && charIndex - 1 < lines.get(lineH).length()) {
				line += " ";
				String head = line.substring(0, charIndex);
				String tail = line.substring(charIndex + 1);
				char blinkingChar = line.charAt(charIndex);
				if (_cursorBlink()) {
					blinkingChar = '_';
				}
				line = head + blinkingChar + tail;
			}
			context.text(line, x + settings.fontWidth * 3 + settings.margin, y + (1 + lineH) * settings.fontHeight);
			lineH++;
		}
	}

	private boolean _cursorBlink() {
		NativeTimer time = new NanoTimer();
		return (time.getClockTicks() * 2 % time.getResolution()) > time.getResolution() / 2;
	}

	@Override
	protected void onUnicodeInput(char unicode) {
		clearSelection();
		_addChar(unicode);
	}

	@Override
	protected void onNonUnicodeInput() {
		boolean shiftPressed = nonUnicodesPressed.contains(KeyEvent.VK_SHIFT);
		boolean ctrlPressed = nonUnicodesPressed.contains(KeyEvent.VK_CONTROL);
		 
		if (nonUnicodesPressed.contains(KeyEvent.VK_CONTROL) && nonUnicodesPressed.contains(KeyEvent.VK_ENTER)) {
			saveAndClose();
			return;
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_ESCAPE)) {
			_escape();
			return;
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_END)) {
			_end(shiftPressed);
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_HOME)) {
			_home(shiftPressed);
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_ENTER)) {
			_return();
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_BACK_SPACE)) {
			_backSpace();
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_DELETE)) {
			_delete();
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_LEFT)) {
			_left(shiftPressed);
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_RIGHT)) {
			_right(shiftPressed);
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_UP)) {
			_up(shiftPressed);
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_DOWN)) {
			_down(shiftPressed);
		}

	}

	protected void saveAndClose() {
		close();
	}

	@Override
	public void close() {
		super.close();
		selection = false;
	}

	private void _down(boolean shiftPressed) {
		if(lineIndex == lines.size()-1){
			return;
		}
		boolean newSelection = false;
		if (shiftPressed && !selection) {
			selection = true;
			newSelection = true;
			selectionBeginLineIndex = lineIndex;
			selectionBeginCharIndex = charIndex;
		}
		boolean moveBegin = (lineIndex == selectionBeginLineIndex && charIndex == selectionBeginCharIndex);
		if (lineIndex + 1 < lines.size()) {
			lineIndex++;
			charIndex = (charIndex > lines.get(lineIndex).length()) ? lines.get(lineIndex).length() : charIndex;
		}
		if (newSelection) {
			selectionEndLineIndex = lineIndex;
			selectionEndCharIndex = charIndex;
		} else {
			if (!shiftPressed || (lineIndex == selectionEndLineIndex && charIndex == selectionEndCharIndex)) {
				selection = false;
			}
			if (selection) {
				if (moveBegin) {
					select(lineIndex, charIndex, selectionEndLineIndex, selectionEndCharIndex);
				} else {
					select(selectionBeginLineIndex, selectionBeginCharIndex, lineIndex, charIndex);
				}
			}
		}
	}

	private void _up(boolean shiftPressed) {
		if(lineIndex == 0){
			return;
		}
		boolean newSelection = false;
		if (shiftPressed && !selection) {
			selection = true;
			newSelection = true;
			selectionEndLineIndex = lineIndex;
			selectionEndCharIndex = charIndex;
		}
		boolean moveBegin = (lineIndex == selectionBeginLineIndex && charIndex == selectionBeginCharIndex);
		if (lineIndex > 0) {
			lineIndex--;
			charIndex = (charIndex > lines.get(lineIndex).length()) ? lines.get(lineIndex).length() : charIndex;
		}
		if (newSelection) {
			selectionBeginLineIndex = lineIndex;
			selectionBeginCharIndex = charIndex;
		} else {
			if (!shiftPressed || (lineIndex == selectionBeginLineIndex && charIndex == selectionBeginCharIndex)) {
				selection = false;
			}
			if (selection) {
				if (moveBegin) {
					select(lineIndex, charIndex, selectionEndLineIndex, selectionEndCharIndex);
				} else {
					select(selectionBeginLineIndex, selectionBeginCharIndex, lineIndex, charIndex);
				}
			}
		}
	}

	private void _end(boolean shiftPressed) {
		if (lineIndex == lines.size() - 1 && charIndex == lines.get(lineIndex).length()) {
			return;
		}
		boolean newSelection = false;
		if (shiftPressed && !selection) {
			selection = true;
			newSelection = true;
			selectionBeginLineIndex = lineIndex;
			selectionBeginCharIndex = charIndex;
		}

		boolean moveBegin = (lineIndex == selectionBeginLineIndex && charIndex == selectionBeginCharIndex);

		charIndex = lines.get(lineIndex).length();

		if (newSelection) {
			selectionEndLineIndex = lineIndex;
			selectionEndCharIndex = charIndex;
		} else {
			if (!shiftPressed
					|| (lineIndex == selectionEndLineIndex && charIndex == selectionEndCharIndex && charIndex != lines.get(lineIndex).length())) {
				selection = false;
			}
			if (selection) {
				if (moveBegin) {
					select(lineIndex, charIndex, selectionEndLineIndex, selectionEndCharIndex);
				} else {
					select(selectionBeginLineIndex, selectionBeginCharIndex, lineIndex, charIndex);
				}
			}
		}

	}

	private void _right(boolean shiftPressed) {
		if (lineIndex == lines.size() - 1 && charIndex == lines.get(lineIndex).length()) {
			return;
		}
		boolean newSelection = false;
		if (shiftPressed && !selection) {
			selection = true;
			newSelection = true;
			selectionBeginLineIndex = lineIndex;
			selectionBeginCharIndex = charIndex;
		}
		boolean moveBegin = (lineIndex == selectionBeginLineIndex && charIndex == selectionBeginCharIndex);

		if (charIndex < lines.get(lineIndex).length()) {
			charIndex++;
		} else if (lineIndex < lines.size() - 1) {
			charIndex = 0;
			lineIndex++;
		}

		if (newSelection) {
			selectionEndLineIndex = lineIndex;
			selectionEndCharIndex = charIndex;
		} else {
			if (!shiftPressed
					|| (lineIndex == selectionEndLineIndex && charIndex == selectionEndCharIndex)) {
				selection = false;
			}
			if (selection) {
				if (moveBegin) {
					select(lineIndex, charIndex, selectionEndLineIndex, selectionEndCharIndex);
				} else {
					select(selectionBeginLineIndex, selectionBeginCharIndex, lineIndex, charIndex);
				}
			}
		}
	}

	private void _home(boolean shiftPressed) {
		if (lineIndex == 0 && charIndex == 0) {
			return;
		}
		boolean newSelection = false;
		if (shiftPressed && !selection) {
			selection = true;
			newSelection = true;
			selectionEndLineIndex = lineIndex;
			selectionEndCharIndex = charIndex;
		}
		boolean moveBegin = (lineIndex == selectionBeginLineIndex && charIndex == selectionBeginCharIndex);

		charIndex = 0;

		if (selectionBeginLineIndex == selectionEndLineIndex && selectionBeginCharIndex == selectionEndCharIndex) {
			selection = false;
		}

		if (newSelection) {
			selectionBeginLineIndex = lineIndex;
			selectionBeginCharIndex = charIndex;
		} else {
			if (!shiftPressed
					|| (lineIndex == selectionBeginLineIndex && charIndex == selectionBeginCharIndex && charIndex != lines.get(lineIndex).length())) {
				selection = false;
			}
			if (selection) {
				if (moveBegin) {
					select(lineIndex, charIndex, selectionEndLineIndex, selectionEndCharIndex);
				} else {
					select(selectionBeginLineIndex, selectionBeginCharIndex, lineIndex, charIndex);
				}
			}
		}
	}

	private void _left(boolean shiftPressed) {
		if (lineIndex == 0 && charIndex == 0) {
			return;
		}
		boolean newSelection = false;
		if (shiftPressed && !selection) {
			selection = true;
			newSelection = true;
			selectionEndLineIndex = lineIndex;
			selectionEndCharIndex = charIndex;
		}
		boolean moveBegin = (lineIndex == selectionBeginLineIndex && charIndex == selectionBeginCharIndex);

		if (charIndex > 0) {
			charIndex--;
		} else if (lineIndex > 0) {
			lineIndex--;
			charIndex = lines.get(lineIndex).length();
		}

		if (newSelection) {
			selectionBeginLineIndex = lineIndex;
			selectionBeginCharIndex = charIndex;
		} else {
			if (!shiftPressed
					|| (lineIndex == selectionBeginLineIndex && charIndex == selectionBeginCharIndex)) {
				selection = false;
			}
			if (selection) {
				if (moveBegin) {
					select(lineIndex, charIndex, selectionEndLineIndex, selectionEndCharIndex);
				} else {
					select(selectionBeginLineIndex, selectionBeginCharIndex, lineIndex, charIndex);
				}
			}
		}
	}

	private void select(int lineBegin, int charBegin, int lineEnd, int charEnd) {
		if (lineBegin > lineEnd || (lineBegin == lineEnd && charBegin > charEnd)) {
			select(lineEnd, charEnd, lineBegin, charBegin);
		} else {
			selectionBeginLineIndex = lineBegin;
			selectionBeginCharIndex = charBegin;
			selectionEndLineIndex = lineEnd;
			selectionEndCharIndex = charEnd;
		}
	}

	private void _escape() {
		// TODO: discard text changes
		close();
	}

	private void _return() {
		if (selection) {
			clearSelection();
		}
		String head = lines.get(lineIndex).substring(0, charIndex);
		String tail = lines.get(lineIndex).substring(charIndex);

		lines.set(lineIndex, head);
		lines.add(lineIndex + 1, tail);

		charIndex = 0;
		lineIndex++;
	}

	private void clearSelection() {
		if (selection) {
			if (selectionBeginLineIndex < selectionEndLineIndex) {
				String head = lines.get(selectionBeginLineIndex).substring(0, selectionBeginCharIndex);
				String tail = lines.get(selectionEndLineIndex).substring(selectionEndCharIndex);
				int linesToDelete = selectionEndLineIndex - selectionBeginLineIndex;
				for (int i = 0; i < linesToDelete; i++) {
					lines.remove(selectionBeginLineIndex + 1);
				}
				lines.set(selectionBeginLineIndex, head + tail);
				// lines.remove(selectionBeginLineIndex + 1);
			} else {
				String head = lines.get(lineIndex).substring(0, selectionBeginCharIndex);
				String tail = lines.get(lineIndex).substring(selectionEndCharIndex);
				lines.set(lineIndex, head + tail);

			}
			lineIndex = selectionBeginLineIndex;
			charIndex = selectionBeginCharIndex;
		}
		selection = false;
	}

	private void _backSpace() {
		if (selection) {
			clearSelection();
		} else {
			if (charIndex == 0 && lineIndex == 0) {
				return;
			} else if (charIndex == 0) {
				String head = lines.get(lineIndex - 1);
				int headLength = lines.get(lineIndex - 1).length();
				String tail = lines.get(lineIndex);
				lines.set(lineIndex - 1, head + tail);
				lines.remove(lineIndex);
				lineIndex--;
				charIndex = headLength;
			} else {
				String head = lines.get(lineIndex).substring(0, charIndex - 1);
				String tail = lines.get(lineIndex).substring(charIndex);
				lines.set(lineIndex, head + tail);
				charIndex--;
			}
		}
	}

	private void _delete() {
		if (selection) {
			clearSelection();
		} else {
			int length = lines.get(lineIndex).length();
			if (lineIndex == lines.size() - 1 && charIndex >= length) {
				return;
			}
			if (charIndex == length) {
				String head = lines.get(lineIndex);
				String tail = lines.get(lineIndex + 1);
				lines.set(lineIndex, head + tail);
				lines.remove(lineIndex + 1);
			} else {
				String head = lines.get(lineIndex).substring(0, charIndex);
				String tail = "";
				if (charIndex < length) {
					tail = lines.get(lineIndex).substring(charIndex + 1);
				}
				lines.set(lineIndex, head + tail);
			}
		}
	}

	private void _addChar(char c) {
		String line = lines.get(lineIndex);
		line = line.substring(0, charIndex) + c + line.substring(charIndex);
		lines.set(lineIndex, line);
		charIndex++;
	}
}
