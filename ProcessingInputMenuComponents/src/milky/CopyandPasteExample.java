package milky;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.awt.datatransfer.*;


/**
 * http://www.javaworld.com/javatips/jw-javatip61.html?page=4
 * 
 * http://www.javaworld.com/javaworld/javatips/javatip61/simpletext/listing1.txt
 */
public class CopyandPasteExample extends Applet implements ClipboardOwner {

	private TextField sourceText = new TextField(40);
	private TextField destinationText = new TextField(40);
	private Button cmdCopy = new Button();
	private Button cmdPaste = new Button();
	private Label labelSource = new Label();
	private Label labelDestination = new Label();
	private Clipboard clipboard;

	public CopyandPasteExample() {
	}

	public void init() {

		this.setBackground(Color.lightGray);
		clipboard = getToolkit().getSystemClipboard();
		cmdCopy.setLabel("Copy source to clipboard");
		cmdPaste.setLabel("Paste to the destination field");
		labelSource.setAlignment(Label.RIGHT);
		labelSource.setText("Source text");
		labelDestination.setAlignment(Label.RIGHT);
		labelDestination.setText("Copy to the clipboard");
		this.add(labelSource);
		this.add(sourceText);
		this.add(labelDestination);
		this.add(destinationText);
		this.add(cmdCopy);
		this.add(cmdPaste);

		cmdCopy.addActionListener(new cmdCopyActionListener());
		cmdPaste.addActionListener(new cmdPasteActionListener());

	}

	public void lostOwnership(Clipboard parClipboard, Transferable parTransferable) {
		System.out.println("Lost ownership");
	}

	class cmdCopyActionListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			StringSelection fieldContent = new StringSelection(sourceText.getText());
			clipboard.setContents(fieldContent, CopyandPasteExample.this);
		}
	}

	class cmdPasteActionListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {

			Transferable clipboardContent = clipboard.getContents(this);

			if ((clipboardContent != null) && (clipboardContent.isDataFlavorSupported(DataFlavor.stringFlavor))) {
				try {
					String tempString;
					tempString = (String) clipboardContent.getTransferData(DataFlavor.stringFlavor);
					destinationText.setText(tempString);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
