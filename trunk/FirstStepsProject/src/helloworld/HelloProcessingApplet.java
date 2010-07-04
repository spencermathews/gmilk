package helloworld;

import processing.core.PApplet;
import processing.core.PFont;

public class HelloProcessingApplet extends PApplet {
	
	public void setup(){
		size(300,100);
		smooth();
		noLoop();
	}
	
	public void draw(){
		line(0,0,300,100);
	}

}
