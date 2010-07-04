package helloworld;

import processing.core.PApplet;
import processing.core.PFont;

public class HelloProcessingApplet extends PApplet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void setup(){
		size(300,100);
		smooth();
		noLoop();
		
		PFont font = loadFont("../data/fonts/FreeSerif-32.vlw");
		textFont(font);
	}
	
	public void draw(){
		text("Hello World",20,50);
	}

}
