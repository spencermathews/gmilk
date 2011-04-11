package milky.preset;

import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;

import org.apache.commons.lang.StringEscapeUtils;
import org.jdom.Element;

import milky.menu.IntegerInput;
import milky.menu.InteractiveMenuComponent;
import milky.menu.Menu;
import milky.menu.JavascriptEditor;
import milky.menu.Toggle;
import processing.core.PApplet;

public class CustomWave extends Menu {

	private static final int MAX_SAMPLES = 512;

	Toggle bypass = new Toggle("bypass", false);
	IntegerInput numberOfSamples = new IntegerInput("number of samples", 2, MAX_SAMPLES);
	Toggle useDots = new Toggle("use dots", false);
	Toggle drawThick = new Toggle("draw thick", false);
	Toggle smooth = new Toggle("smooth", false);
	IntegerInput red = new IntegerInput("red   ", 0, 255);
	IntegerInput green = new IntegerInput("green ", 0, 255);
	IntegerInput blue = new IntegerInput("blue  ", 0, 255);
	IntegerInput alpha = new IntegerInput("alpha ", 0, 255);

	JavascriptEditor initScript = new JavascriptEditor("initialization script");
	JavascriptEditor perSampleScript = new JavascriptEditor("per-sample script");

	int[] rSamples = new int[MAX_SAMPLES];
	int[] gSamples = new int[MAX_SAMPLES];
	int[] bSamples = new int[MAX_SAMPLES];
	int[] aSamples = new int[MAX_SAMPLES];
	float[] xSamples = new float[MAX_SAMPLES];
	float[] ySamples = new float[MAX_SAMPLES];

	public CustomWave(String label) {
		super(label);

		red.setValue(255);
		green.setValue(255);
		blue.setValue(255);
		alpha.setValue(255);

		addMenuItem(bypass);
		addMenuItem(red);
		addMenuItem(green);
		addMenuItem(blue);
		addMenuItem(alpha);
		addMenuItem(smooth);
		addMenuItem(useDots);
		addMenuItem(drawThick);
		addMenuItem(initScript);
		addMenuItem(numberOfSamples);
		addMenuItem(perSampleScript);

		ArrayList<String> sampleCode = new ArrayList<String>();
		sampleCode.add("y = 0.5 + Math.sin(frame/5 - sample/(samples-1)*24)*0.1;");
		sampleCode.add("x = 0.25 + 0.5 * sample/(samples-1);");
		perSampleScript.setText(sampleCode);

		numberOfSamples.setValue(128);
		
		compile();
		close();
	}

	@Override
	public Element getXML() {
		Element xml = new Element(PresetConstants.CUSTOM_WAVE);
		xml.setAttribute("label", StringEscapeUtils.escapeXml(getLabel()));
		for (InteractiveMenuComponent menuItem : menuItems) {
			xml.addContent(menuItem.getXML());
		}
		return xml;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setXML(Element node) {
		List<Element> children = node.getChildren();
		setLabel(node.getAttributeValue("label"));
		menuItems.clear();
		for (Element child : children) {
			InteractiveMenuComponent childComponent = buildComponent(child);
			addMenuItem(childComponent);
		}
	}


	public void compile() {
		initScript.compile();
		perSampleScript.compile();
	}

	public void step(Bindings bindings) {
		int samples = numberOfSamples.getValue();

		bindings.put("r", red.getValue());
		bindings.put("g", green.getValue());
		bindings.put("b", blue.getValue());
		bindings.put("a", alpha.getValue());
		bindings.put("x", 0.5f);
		bindings.put("y", 0.5f);
		bindings.put("samples", samples);
		
		initScript.execute(bindings);

		for (int sample = 0; sample < samples; sample++) {
			Bindings sampleBindings = initScript.getBindings();
			sampleBindings.put("sample", sample);
			perSampleScript.execute(sampleBindings);
			pushSample(sample);
		}
	}

	private void pushSample(int sample) {
		Bindings bindings = perSampleScript.getBindings();
		rSamples[sample] = castInt(bindings.get("r"));
		gSamples[sample] = castInt(bindings.get("g"));
		bSamples[sample] = castInt(bindings.get("b"));
		aSamples[sample] = castInt(bindings.get("a"));
		xSamples[sample] = castFloat(bindings.get("x"));
		ySamples[sample] = castFloat(bindings.get("y"));
	}

	private float castFloat(Object o) {
		if (o instanceof Float) {
			return (Float) o;
		}
		if (o instanceof Double) {
			return ((Double) o).floatValue();
		}
		if (o instanceof String) {
			return Float.parseFloat((String) o);
		}

		System.out.println("no float: " + o);
		return 0;
	}

	private int castInt(Object o) {
		if (o instanceof Integer) {
			return (Integer) o;
		}
		if (o instanceof Double) {
			return ((Double) o).intValue();
		}
		if (o instanceof String) {
			return Integer.parseInt((String) o);
		}

		System.out.println("no int: " + o);
		return 0;
	}

	public void render(PApplet context) {
		float x = xSamples[0];
		float y = ySamples[0];
		int r = rSamples[0];
		int g = gSamples[0];
		int b = bSamples[0];
		int a = aSamples[0];
		int numSamples = numberOfSamples.getValue();
		
		if (smooth.getToggleState()) {
			context.smooth();
		} else {
			context.noSmooth();
		}
		
		if (drawThick.getToggleState()) {
			context.strokeWeight(1.5f);
		} else {
			context.strokeWeight(1);
		}
		for (int sample = 1; sample < numSamples; sample++) {
			float nextX = xSamples[sample];
			float nextY = ySamples[sample];
			int nextR = rSamples[sample];
			int nextG = gSamples[sample];
			int nextB = bSamples[sample];
			int nextA = aSamples[sample];

			context.fill(context.color(r, g, b, a));
			context.stroke(context.color(r, g, b, a));

			if (useDots.getToggleState()) {
				context.ellipse(x * context.width, y * context.height, 1, 1);
			} else {
				context.line(x * context.width, y * context.height, nextX * context.width, nextY * context.height);
			}

			x = nextX;
			y = nextY;
			r = nextR;
			g = nextG;
			b = nextB;
			a = nextA;
		}
		if (useDots.getToggleState()) {
			context.ellipse(xSamples[numSamples - 1] * context.width, ySamples[numSamples - 1] * context.height, 1, 1);
		}
	}

	// private void printSampleData(int sample) {
	// float x = xSamples[sample];
	// float y = ySamples[sample];
	// int r = rSamples[sample];
	// int g = gSamples[sample];
	// int b = bSamples[sample];
	// int a = aSamples[sample];
	//
	// System.out.println("sample data " + sample);
	// System.out.println("x " + x);
	// System.out.println("y " + y);
	// System.out.println("r " + r);
	// System.out.println("g " + g);
	// System.out.println("b " + b);
	// System.out.println("a " + a);
	// }

}
