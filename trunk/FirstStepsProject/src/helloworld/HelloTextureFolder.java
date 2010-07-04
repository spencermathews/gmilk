package helloworld;

import processing.core.PApplet;
import processing.core.PImage;

public class HelloTextureFolder extends PApplet {
	PImage imageEye;
	
	@Override
	public void setup() {
		size(320,240);
		imageEye = loadImage("../data/textures/EyeHDR.jpg");
	}
	
	@Override
	public void draw() {
		image(imageEye, 0, 0, 320, 240);
	}

}
