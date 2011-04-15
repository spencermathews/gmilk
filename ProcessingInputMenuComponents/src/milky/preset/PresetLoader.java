package milky.preset;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import milky.menu.InteractiveMenuComponent;
import milky.menu.Menu;
import milky.menu.TextInput;
import milky.menu.Trigger;
import milky.menu.events.ITriggerListener;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class PresetLoader extends Menu {

	public static final String PRESET_FILE_EXTENSION = "gmilk.xml";

	private File presetDir;

	private static Format format = Format.getPrettyFormat();
	static {
		format.setIndent("  ");
	}

	private final PresetEditor presetEditor;

	private static final TextInput saveFileNameInput = new TextInput("fileName");
	private static final TextInput renameFileNameInput = new TextInput("fileName");

	public PresetLoader(String absolutePath, PresetEditor editor) {
		super("presetList");
		setLabel(absolutePath.substring(absolutePath.lastIndexOf(File.separator) + 1));
		prefix = "[";
		suffix = "]";
		presetDir = new File(absolutePath);
		presetEditor = editor;
		initConfirmationBoxes();
		initMenu();
	}

	protected Menu confirmOverwriteBox = new Menu("confirmation");
	protected Menu confirmDeleteBox = new Menu("confirmation");

	private void initConfirmationBoxes() {
		Trigger confirmOverwrite = new Trigger("yes");
		confirmOverwrite.addListener(new ITriggerListener() {
			public void onTrigger() {
				savePreset();
				confirmOverwriteBox.setActive(true); // ensure activation to correctly close
				confirmOverwriteBox.close();
			}
		});
		final Trigger cancelOverwrite = new Trigger("no");
		cancelOverwrite.addListener(new ITriggerListener() {
			public void onTrigger() {
				System.out.println("close confirmOverwriteBox");
				confirmOverwriteBox.setActive(true); // ensure activation to correctly close
				confirmOverwriteBox.close();
				// nonUnicodesPressed.remove(KeyEvent.VK_ENTER);
				// confirmOverwriteBox.setActive(false);
			}
		});
		confirmOverwriteBox.addMenuItem(confirmOverwrite);
		confirmOverwriteBox.addMenuItem(cancelOverwrite);
		Trigger confirmDelete = new Trigger("delete preset");
		confirmDelete.addListener(new ITriggerListener() {
			public void onTrigger() {
				deletePreset();
				confirmDeleteBox.close();
			}
		});
		Trigger cancelDelete = new Trigger("don't delete");
		cancelDelete.addListener(new ITriggerListener() {
			public void onTrigger() {
				confirmDeleteBox.close();
			}
		});
		confirmDeleteBox.setParent(this);
		confirmDeleteBox.addMenuItem(cancelDelete);
		confirmDeleteBox.addMenuItem(confirmDelete);
		saveFileNameInput.setTooltip("save preset as");
		saveFileNameInput.setMultiLine(false);
		saveFileNameInput.addSaveListener(new ITriggerListener() {
			public void onTrigger() {
				String fileName = saveFileNameInput.getTextAsString();
				if (isValidFileName(fileName)) {
					// confirmOverwriteBox.setParent(saveFileNameInput);
					confirmOverwriteAndSavePreset();
					// saveFileNameInput.close();
					// confirmOverwriteBox.setParent(null);
				} else {
					saveFileNameInput.setText(makeValidFileName(fileName));
					saveFileNameInput.setActive(true);
				}
			}
		});
		renameFileNameInput.setTooltip("rename preset");
		renameFileNameInput.setMultiLine(false);
		renameFileNameInput.setParent(this);
	}

	private void initMenu() {
		// TODO: parse all presets to a map once then load xml from internal store
		for (InteractiveMenuComponent item : menuItems) {
			item.clear();
		}
		setTooltip(presetDir.getAbsolutePath());
		menuItems.clear();
		if (presetDir.isDirectory()) {
			if (presetDir.getParentFile() != null && !presetDir.getParentFile().isHidden()) {
				String currentPath = presetDir.getAbsolutePath();
				final String parentPath = currentPath.substring(0, currentPath.lastIndexOf(File.separator));
				Trigger trigger = new Trigger("[ parent directory ]");
				trigger.addListener(new ITriggerListener() {
					public void onTrigger() {
						changeDir(parentPath);
					}
				});
				addMenuItem(trigger);
			}
			for (String directoryName : presetDir.list(new DirectoryFilter())) {
				addDirectory(directoryName);
			}
			String[] presetList = presetDir.list(new PresetFilter());
			if (presetList.length == 0) {
				addMenuItem(new Trigger("(no presets found)"));
			} else {
				for (String presetFileName : presetList) {
					addPreset(presetFileName);
				}
			}
		}
		selectedItem = menuItems.get(0);
	}

	private void addDirectory(String dirName) {
		final String dirPath = presetDir.getAbsolutePath() + File.separator + dirName;
		Trigger trigger = new Trigger("[ " + dirName + " ]");
		trigger.addListener(new ITriggerListener() {
			public void onTrigger() {
				changeDir(dirPath);
			}
		});
		addMenuItem(trigger);
	}

	protected void changeDir(String dirPath) {
		presetDir = new File(dirPath);
		initMenu();
	}

	private void addPreset(String presetFileName) {
		String presetName = presetFileName.replace("." + PRESET_FILE_EXTENSION, "");
		Trigger presetTrigger = new Trigger(presetName);
		presetTrigger.addListener(new ITriggerListener() {
			public void onTrigger() {
				loadPreset();
			}
		});
		addMenuItem(presetTrigger);
	}

	@Override
	protected void onNonUnicodeInput() {
		if (nonUnicodesPressed.contains(KeyEvent.VK_DELETE)) {
			confirmAndDeletePreset();
			return;
		}
		if (nonUnicodesPressed.contains(KeyEvent.VK_INSERT)) {
			renamePreset();
			return;
		}
		super.onNonUnicodeInput();
	}

	@Override
	protected void onUnicodeInput(char unicode) {
		// TODO: jump to first preset beginning with the pressed keyChar

	}

	private void renamePreset() {
		renameFileNameInput.setText(selectedItem.getLabel());
		renameFileNameInput.moveCursorToEndOfText();
		renameFileNameInput.setActive(true);
	}

	private void loadPreset() {
		String presetPath = getAbsolutePresetPath();
		File presetFile = new File(presetPath);
		parseAndLoadPresetFile(presetFile);
	}

	private void savePreset() {
		try {
			String presetPath = presetDir.getAbsolutePath() + File.separator + presetEditor.getLoadedPresetName() + "." + PRESET_FILE_EXTENSION;
			FileWriter preset = new FileWriter(presetPath);
			XMLOutputter out = new XMLOutputter(format);
			Element presetXML = presetEditor.getXML();
			out.output(new Document(presetXML), preset);
			preset.flush();
			preset.close();
			setStatus("file saved: " + presetPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void deletePreset() {
		deleteFile(getAbsolutePresetPath());
	}

	protected void deleteFile(String pathName) {
		File file = new File(pathName);
		if (file.exists() && !file.isDirectory()) {
			file.delete();
			int index = menuItems.indexOf(selectedItem);
			menuItems.remove(selectedItem);
			selectedItem = menuItems.get(index);
		}
	}

	private void parseAndLoadPresetFile(File presetFile) {
		SAXBuilder builder = new SAXBuilder();
		try {
			Document presetDoc = builder.build(presetFile);
			Element presetRoot = presetDoc.getRootElement();
			presetRoot.setAttribute(PresetConstants.PRESET_NAME, selectedItem.getLabel());
			presetEditor.loadPreset(presetRoot);
		} catch (JDOMException e) {
			onError(e);
			e.printStackTrace();
		} catch (IOException e) {
			onError(e);
			e.printStackTrace();
		}
	}

	private String getAbsolutePresetPath() {
		return presetDir.getAbsolutePath() + File.separator + selectedItem.getLabel() + "." + PRESET_FILE_EXTENSION;
	}

	private void confirmAndDeletePreset() {
		String presetPath = getAbsolutePresetPath();
		if ((new File(presetPath)).exists()) {
			confirmDeleteBox.setActive(true);
		}
	}

	private void confirmOverwriteAndSavePreset() {
		String presetName = saveFileNameInput.getTextAsString();
		String presetPath = presetDir.getAbsolutePath() + File.separator + presetName + "." + PRESET_FILE_EXTENSION;
		if ((new File(presetPath)).exists()) {
			setStatus("file exists, prompt overwrite " + presetPath);
			confirmOverwriteBox.setTooltip("overwrite existing preset '" + presetName + "'?");
			confirmOverwriteBox.setActive(true);
		} else {
			savePreset();
		}
	}

	public void promptFileNameAndSavePreset() {
		saveFileNameInput.setText(presetEditor.getLoadedPresetName());
		saveFileNameInput.setParent(this);
		saveFileNameInput.moveCursorToEndOfText();
		saveFileNameInput.setActive(true);
	}

	protected boolean isValidFileName(String fileName) {
		return true; // TODO: check for forbidden characters
	}

	protected static final String makeValidFileName(String fileName) {
		return fileName; // TODO: remove forbidden characters
	}

	private static final class PresetFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			// TODO: parse to check preset version
			return name.endsWith("." + PRESET_FILE_EXTENSION);
		}
	}

	private static final class DirectoryFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			File file = new File(dir.getAbsolutePath() + File.separator + name);
			return file.isDirectory();
		}
	}

}
