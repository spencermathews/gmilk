package milky.menu;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;

import milky.menu.events.ITextChangeListener;
import milky.menu.events.ITriggerListener;

import org.apache.commons.lang.StringEscapeUtils;
import org.jdom.Element;

import processing.core.PApplet;

/**
 * A Multiline Text editor component for Processing.
 * 
 * @author Felix Woitzel, Feb 2011
 */
public class TextInput extends InteractiveMenuComponent {

	private static final String LINEBREAK = "\n";

	protected ArrayList<String> lines = new ArrayList<String>();

	protected boolean readOnly = false;

	protected boolean multiLine = true;

	int lineIndex = 0;
	int charIndex = 0;

	private int selectionBeginLineIndex = -1;
	private int selectionBeginCharIndex = -1;
	private int selectionEndLineIndex = -1;
	private int selectionEndCharIndex = -1;

	private boolean selection = false;
	private LinkedList<ITextChangeListener> textChangeListeners = new LinkedList<ITextChangeListener>();
	private LinkedList<ITriggerListener> saveListeners = new LinkedList<ITriggerListener>();

	protected String savedText;

	public TextInput(String label) {
		register(this);
		this.prefix = "[";
		this.setLabel(label);
		this.suffix = "]";
		lines.add("");
	}

	@Override
	public void setXML(Element textElem) {
		setMultiLine(Boolean.parseBoolean(textElem.getAttributeValue("multiLine")));
		setReadOnly(Boolean.parseBoolean(textElem.getAttributeValue("readOnly")));
		setLabel(StringEscapeUtils.unescapeXml(textElem.getAttributeValue("label")));
		savedText = textElem.getText();
		setText(StringEscapeUtils.unescapeXml(savedText));
		checkCursorPosition();
	}

	@Override
	public Element getXML() {
		Element xml = new Element("text");
		xml.setAttribute("label", StringEscapeUtils.escapeXml(getLabel()));
		xml.setAttribute("readOnly", "" + readOnly);
		xml.setAttribute("multiLine", "" + multiLine);
		xml.setText(StringEscapeUtils.escapeXml(getTextAsString()));
		return xml;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setMultiLine(boolean multiLine) {
		this.multiLine = multiLine;
	}

	public boolean isMultiLine() {
		return multiLine;
	}

	public void addTextChangeListener(ITextChangeListener listener) {
		textChangeListeners.add(listener);
	}

	public void removeTextChangeListener(ITextChangeListener listener) {
		textChangeListeners.remove(listener);
	}

	public LinkedList<ITextChangeListener> getTextChangeListeners() {
		return textChangeListeners;
	}

	public void addSaveListener(ITriggerListener listener) {
		saveListeners.add(listener);
	}

	public void removeSaveListener(ITriggerListener listener) {
		saveListeners.remove(listener);
	}

	public void setText(String text) {
		lines.clear();
		if (text == null) {
			text = "";
		}
		while (text.indexOf(LINEBREAK) > -1) {
			String line = text.substring(0, text.indexOf(LINEBREAK));
			lines.add(line);
			text = text.substring(text.indexOf(LINEBREAK) + 1);
		}

		lines.add(text);
		savedText = getTextAsString();
	}

	public void setText(ArrayList<String> text) {
		lines = text;
		savedText = getTextAsString();
	}

	public void moveCursorToEndOfText() {
		lineIndex = lines.size() - 1;
		charIndex = lines.get(lineIndex).length();
	}

	public String getTextAsString() {
		String string = "";
		for (String line : lines) {
			string += line;
			if (lines.indexOf(line) < lines.size() - 1) {
				string += LINEBREAK;
			}
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
		int numLines = multiLine ? lines.size() : 1;
		drawBackground(context, x, y, (getMaxLineLength() + 3) * settings.fontWidth + 2 * settings.margin, numLines * settings.fontHeight + 2
				* settings.margin);
		int lineH = 0;
		for (String l : lines) {
			String line = new String(l);
			if (lineH == lineIndex && multiLine) {
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
			} else if (lineH == lineIndex && charIndex - 1 < lines.get(lineH).length() && !readOnly) {
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
			if (!multiLine) {
				return;
			}
		}
	}

	private static final boolean _cursorBlink() {
		return (System.currentTimeMillis() % 500) > 250;
	}

	@Override
	protected void onUnicodeInput(char unicode) {
		clearSelection();
		if (!readOnly) {
			_addChar(unicode);
		}
	}

	@Override
	protected void onNonUnicodeInput() {
		boolean shiftPressed = nonUnicodesPressed.contains(KeyEvent.VK_SHIFT);
		boolean ctrlPressed = nonUnicodesPressed.contains(KeyEvent.VK_CONTROL);

		if (nonUnicodesPressed.contains(KeyEvent.VK_CONTROL) && nonUnicodesPressed.contains(KeyEvent.VK_ENTER)) {
			saveAndClose();
			return;
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_ENTER)) {
			_return();
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
		if (nonUnicodesPressed.contains(KeyEvent.VK_BACK_SPACE)) {
			_backSpace();
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_DELETE)) {
			_delete();
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_LEFT)) {
			_left(shiftPressed, ctrlPressed);
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_RIGHT)) {
			_right(shiftPressed, ctrlPressed);
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_UP)) {
			_up(shiftPressed);
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_DOWN)) {
			_down(shiftPressed);
		}

	}

	protected void saveAndClose() {
		checkTextChange();
		savedText = getTextAsString();
		close();
	}

	private void checkTextChange() {
		if (!getTextAsString().equals(savedText)) {
			for (ITextChangeListener listener : textChangeListeners) {
				listener.onTextChanged();
			}
		} else {
			updateSaveListeners();
		}
	}

	private void updateSaveListeners() {
		for (ITriggerListener listener : saveListeners) {
			listener.onTrigger();
		}
	}

	@Override
	public void close() {
		super.close();
		selection = false;
	}

	protected void checkCursorPosition() {
		if (lineIndex >= lines.size()) {
			lineIndex = lines.size() - 1;
			charIndex = lines.get(lineIndex).length();
		}
		if (charIndex > lines.get(lineIndex).length()) {
			charIndex = lines.get(lineIndex).length();
		}
	}

	private void _down(boolean shiftPressed) {
		if (lineIndex == lines.size() - 1) {
			selection &= shiftPressed;
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
		if (lineIndex == 0) {
			selection &= shiftPressed;
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
			selection &= shiftPressed;
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

	private void _right(boolean shiftPressed, boolean ctrlPressed) {
		do {
			if (lineIndex == lines.size() - 1 && charIndex == lines.get(lineIndex).length()) {
				selection &= shiftPressed;
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
		} while (ctrlPressed && !cursorAtWordEnd());
	}

	private void _home(boolean shiftPressed) {
		if (lineIndex == 0 && charIndex == 0) {
			selection &= shiftPressed;
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

	private void _left(boolean shiftPressed, boolean ctrlPressed) {
		do {
			if (lineIndex == 0 && charIndex == 0) {
				selection &= shiftPressed;
				return;
			}
			boolean newSelection = false;
			if (shiftPressed && !selection) {
				selection = true;
				newSelection = true;
				selectionEndLineIndex = lineIndex;
				selectionEndCharIndex = charIndex;
			}
			boolean moveBegin = true;
			moveBegin = (lineIndex == selectionBeginLineIndex && charIndex == selectionBeginCharIndex);
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
		} while (ctrlPressed && !cursorAtWordEnd());
	}

	private boolean cursorAtWordEnd() {
		if ((charIndex == 0) || (charIndex >= lines.get(lineIndex).length())) {
			return true;
		}
		if ((charIndex + 1 < lines.get(lineIndex).length())) {
			char before = lines.get(lineIndex).charAt(charIndex - 1);
			char it = lines.get(lineIndex).charAt(charIndex);
			char after = lines.get(lineIndex).charAt(charIndex + 1);
			// XXX: tired and confused if it's all correct ^^
			if ((it == '(' && (before != '(' || before == ' ')) || (it == ')' && (before != ')' || before == ' '))
					|| (before == ' ' || before == '(' || before == ')' || before == ']' || before == '.' || before == ';') && it != ' ' && it != '('
					&& it != ')' && it != ']' && it != ';') {
				return true;
			}
			if (it >= 'A' && it <= 'Z' && (!(before >= 'A' && before <= 'Z') || !(after >= 'A' && after <= 'Z'))) {
				return true;
			}
		}
		return false;
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

	@Override
	protected void copy() {
		if (selection) {
			String copyString = "";
			if (selectionBeginLineIndex + 1 < selectionEndLineIndex) {
				copyString = lines.get(selectionBeginLineIndex).substring(selectionBeginCharIndex) + LINEBREAK;
				for (int lIndex = selectionBeginLineIndex + 1; lIndex < selectionEndLineIndex; lIndex++) {
					copyString += lines.get(lIndex) + LINEBREAK;
				}
				copyString += lines.get(selectionEndLineIndex).substring(0, selectionEndCharIndex);
			} else if (selectionBeginLineIndex + 1 == selectionEndLineIndex) {
				copyString = lines.get(selectionBeginLineIndex).substring(selectionBeginCharIndex) + LINEBREAK;
				copyString += lines.get(selectionEndLineIndex).substring(0, selectionEndCharIndex);
			} else if (selectionBeginLineIndex == selectionEndLineIndex) {
				copyString = lines.get(selectionBeginLineIndex).substring(selectionBeginCharIndex, selectionEndCharIndex);
			}
			StringSelection copySelection = new StringSelection(copyString);
			clipboard.setContents(copySelection, this);
		}
	}

	@Override
	protected void paste() {
		if (readOnly) {
			return;
		}
		clearSelection();
		String pasteString = "";
		Transferable clipboardContent = clipboard.getContents(this);
		if ((clipboardContent != null) && (clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor))) {
			try {
				pasteString = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);
			} catch (Exception e) {
				onError(e);
			}
		}
		LinkedList<String> copyLines = new LinkedList<String>();
		while (pasteString.indexOf(LINEBREAK) > -1) {
			String copyLine = pasteString.substring(0, pasteString.indexOf(LINEBREAK));
			copyLines.add(copyLine);
			pasteString = pasteString.substring(pasteString.indexOf(LINEBREAK) + 1);
		}
		copyLines.add(pasteString);
		int numLines = copyLines.size();
		String head = lines.get(lineIndex).substring(0, charIndex);
		String tail = lines.get(lineIndex).substring(charIndex);
		if (numLines >= 2 && !multiLine) {
			lines.set(lineIndex, head + copyLines.get(0));
			lines.add(lineIndex + 1, copyLines.get(numLines - 1) + tail);
			for (int l = numLines - 2; l > 0; l--) {
				lines.add(lineIndex + 1, copyLines.get(l));
			}
			lineIndex += numLines - 1;
			charIndex = copyLines.get(numLines - 1).length();
		} else if (numLines > 0) {
			lines.set(lineIndex, head + copyLines.get(0) + tail);
			charIndex = head.length() + copyLines.get(0).length();
		}
	}

	private void _escape() {
		setText(savedText); // reset text
		close();
	}

	private void _return() {
		if (!multiLine) {
			saveAndClose();
		}
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

	protected void clearSelection() {
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
		if (readOnly) {
			return;
		}
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
		if (readOnly) {
			return;
		}
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
