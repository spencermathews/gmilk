package helloworld;

import processing.core.PApplet;

public class HelloGL extends PApplet {
	
	public void setup() {
		size(400,300,OPENGL);
	}
	
	public void draw() {
		line(0,0,width,height);
	}

}
