/**
 * This package provides classes to facilitate the handling of opengl textures, glsl shaders and 
 * off-screen rendering in Processing.
 * @author Andres Colubri
 * @version 0.9.3
 *
 * Copyright (c) 2008 Andres Colubri
 *
 * This source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 * 
 * A copy of the GNU General Public License is available on the World
 * Wide Web at <http://www.gnu.org/copyleft/gpl.html>. You can also
 * obtain it by writing to the Free Software Foundation,
 * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package codeanticode.glgraphics;

import java.awt.Font;
import java.io.InputStream;
import java.util.ArrayList;

import processing.core.*;
import processing.opengl.*;
import javax.media.opengl.*;

/**
 * This class makes the OpenGL renderer compatible with the GLTexture object.
 */
public class GLGraphics extends PGraphicsOpenGL implements GLConstants 
{
    /**
     * Default constructor.
     */	
    public GLGraphics() 
    {
        super();
    }

    /**
     * Adds a GLWindow to be handled by this renderer.
     * @param win GLWindow
     */
    public void addWindow(GLWindow win)
    {
    	if (windowsList == null) windowsList = new ArrayList<GLWindow>();
    	windowsList.add(win);
    }
    
    /**
     * Returns the OpenGL capabilities of the drawable associated to this renderer.
     * @return GLCapabilities
     */  
    public GLCapabilities getCapabilities()
    {
        return drawable.getChosenGLCapabilities(); 
    }
  
    /**
     * Creates a new context for canvas that is shared with the context of GLGraphics.
     * @param GLCanvas canvas
     */
    public void shareContext(GLCanvas canvas)
    {
	    if (context != null) canvas.createContext(context);  	  
    }

    /**
     * Adds the additional check for all the GL windows.
     */
    public boolean canDraw()
    {
    	if (!allWindowsReady()) return false;
        return super.canDraw();
    }
    
    /**
     * Creates a new context for drawable that is shared with the context of GLGraphics.
     * @param GLDrawable drawable
     */  
    public void shareContext(GLDrawable drawable)
    {
	    if (context != null) drawable.createContext(context);  	  
    }

    public void setSize(int iwidth, int iheight)
    {
      super.setSize(iwidth, iheight);
      
      // init lights (in resize() instead of allocate() b/c needed by opengl)
      lightTypeGL = new int[MAX_LIGHTS_GL];
      lightPositionGL = new float[MAX_LIGHTS_GL][4];
      lightNormalGL = new float[MAX_LIGHTS_GL][4];
      lightDiffuseGL = new float[MAX_LIGHTS_GL][4];
      lightSpecularGL = new float[MAX_LIGHTS_GL][4];
      lightFalloffConstantGL = new float[MAX_LIGHTS_GL];
      lightFalloffLinearGL = new float[MAX_LIGHTS_GL];
      lightFalloffQuadraticGL = new float[MAX_LIGHTS_GL];
      lightSpotAngleGL = new float[MAX_LIGHTS_GL];
      lightSpotAngleCosGL = new float[MAX_LIGHTS_GL];
      lightSpotConcentrationGL = new float[MAX_LIGHTS_GL];
      currentLightSpecularGL = new float[4];
    }
    
    public GL beginGL()
    {
        super.beginGL();
        
        
        //gl.glTranslatef(0.0f, 1.0f * height, 0.0f);
        //gl.glScalef(1.0f, -1.0f, 1.0f);
        
        glMode = true;
        
        return gl;
    }

    public void endGL()
    {
        glMode = false;
        super.endGL();
    }    
    
    public void beginDraw() 
    {
    	super.beginDraw();
        lightCountGL = 0;
        lightFalloff(1, 0, 0);
        lightSpecular(0, 0, 0);    	
    }
    	
    public void endDraw() 
    {
    	super.endDraw(); // The openGL context of the renderer is released here...
    	renderWindows(); // ...this is important since the windows have their own contexts
                         // that share data with the main context.
    }
    
    public void pushMatrix()
    {
        if (glMode) gl.glPushMatrix();
        else super.pushMatrix(); 
    }
    
    public void popMatrix()
    {
        if (glMode) gl.glPopMatrix();
        else super.popMatrix();  	
    }

    public void translate(float tx, float ty)
    {
        if (glMode) gl.glTranslatef(tx, ty, 0.0f);
        else super.translate(tx, ty, 0.0f);    	
    }
    
    public void translate(float tx, float ty, float tz)
    {
        if (glMode) gl.glTranslatef(tx, ty, tz);
        else super.translate(tx, ty, tz);    	    	
    }
    
    public void rotate(float angle)
    {
        if (glMode) gl.glRotatef(PApplet.degrees(angle), 0.0f, 0.0f, 1.0f);
        else super.rotate(angle);      	
    }
    
    public void rotateX(float angle)
    {
        if (glMode) gl.glRotatef(PApplet.degrees(angle), 1.0f, 0.0f, 0.0f);
        else super.rotateX(angle);    	
    }
    
    public void rotateY(float angle)
    {
        if (glMode) gl.glRotatef(PApplet.degrees(angle), 0.0f, 1.0f, 0.0f);
        else super.rotateY(angle);
    }
    
    public void rotateZ(float angle)
    {
        if (glMode) gl.glRotatef(PApplet.degrees(angle), 0.0f, 0.0f, 1.0f);
        else super.rotateZ(angle);    	
    }
    
    public void rotate(float angle, float vx, float vy, float vz)
    {
        if (glMode) gl.glRotatef(PApplet.degrees(angle), vx, vy, vz);
        else super.rotate(angle, vz, vy, vz);   	
    }
    
    public void scale(float s)
    {
        if (glMode) gl.glScalef(s, s, s);
        else super.scale(s);     	
    }
    
    public void scale(float sx, float sy)
    {
        if (glMode) gl.glScalef(sx, sy, 1.0f);
        else super.scale(sx, sy);    	
    }
    
    public void scale(float x, float y, float z)
    {
        if (glMode) gl.glScalef(x, y, x);
        else super.scale(x, y, z);	
    }

    public void lights() 
    {
      if (offScreenRenderer != null) { offScreenRenderer.lights(); return; }
      
    	if (glMode) {
    	    gl.glEnable( GL.GL_LIGHTING );

    	    gl.glEnable(GL.GL_COLOR_MATERIAL);
    	    gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE);    		
    		
    	    // need to make sure colorMode is RGB 255 here
    	    int colorModeSaved = colorMode;
    	    colorMode = RGB;

    	    lightFalloff(1, 0, 0);
    	    lightSpecular(0, 0, 0);

    	    ambientLight(colorModeX * 0.5f,
    	                 colorModeY * 0.5f,
    	                 colorModeZ * 0.5f);
    	    directionalLight(colorModeX * 0.5f,
    	                     colorModeY * 0.5f,
    	                     colorModeZ * 0.5f,
    	                     0, 0, -1);

    	    colorMode = colorModeSaved;    		
    	}
    	else super.lights();    	
    }
    
    public void saveLights() {
      if (offScreenRenderer != null) { offScreenRenderer.saveLights(); return; }
      
      if (glMode) saveLightCount = lightCountGL;
      else saveLightCount = lightCount; 
    }
    
    public void restoreLights() {
      if (offScreenRenderer != null) { offScreenRenderer.restoreLights(); return; }
      
      if (glMode) {
        lightCountGL = saveLightCount; 
        for (int i = 0; i < lightCountGL; i++) {
          glLightEnable(i);
          if (lightTypeGL[i] == AMBIENT) {
            glLightAmbient(i);
            glLightPosition(i);
            glLightFalloff(i);            
          }else if (lightTypeGL[i] == DIRECTIONAL) {
            glLightNoAmbient(i);
            glLightDirection(i);
            glLightDiffuse(i);
            glLightSpecular(i);
            glLightFalloff(i);                 
          }else if (lightTypeGL[i] == POINT) {
            glLightPosition(i);
            glLightDirection(i);
            glLightDiffuse(i);
            glLightSpecular(i);
            glLightFalloff(i);
            glLightSpotAngle(i);
            glLightSpotConcentration(i);                 
          }else if (lightTypeGL[i] == SPOT) {
            glLightNoAmbient(i);
            glLightPosition(i);
            glLightDirection(i);
            glLightDiffuse(i);
            glLightSpecular(i);
            glLightFalloff(i);
            glLightSpotAngle(i);
            glLightSpotConcentration(i);   
          }
        }
      } else {
        lightCount = saveLightCount;
      }
    }
    
    public void resetLights() {
      if (offScreenRenderer != null) { offScreenRenderer.resetLights(); return; }
      
    	if (glMode) { 
    		for (int i = 0; i < lightCountGL; i++) glLightDisable(i);
    		lightCountGL = 0;
    	}
    	else lightCount = 0;
    }

    public void noLights() 
    {
      if (offScreenRenderer != null) { offScreenRenderer.noLights(); return; }
      
    	if (glMode) {
    	  gl.glDisable( GL.GL_LIGHTING );		
          lightCountGL = 0;		
    	}
    	else super.noLights();       	
    }    
         
    
    /**
     * Add an ambient light based on the current color mode.
     */
    public void ambientLight(float r, float g, float b) {
      if (glMode) ambientLight(r, g, b, 0, 0, 0);
      else super.ambientLight(r, g, b, 0, 0, 0);
    }


    /**
     * Add an ambient light based on the current color mode.
     * This version includes an (x, y, z) position for situations
     * where the falloff distance is used.
     */
    public void ambientLight(float r, float g, float b,
                             float x, float y, float z) {
      if (offScreenRenderer != null) { offScreenRenderer.ambientLight(r, g, b, x, y, z); return; }
      
      if (!glMode) { super.ambientLight(r, g, b, x, y, z); return; }
      
      if (lightCountGL == MAX_LIGHTS_GL) {
        throw new RuntimeException("can only create " + MAX_LIGHTS_GL + " lights");
      }
      colorCalc(r, g, b);
      lightDiffuseGL[lightCountGL][0] = calcR;
      lightDiffuseGL[lightCountGL][1] = calcG;
      lightDiffuseGL[lightCountGL][2] = calcB;
      lightDiffuseGL[lightCountGL][3] = 1.0f;
      
      lightTypeGL[lightCountGL] = AMBIENT;
      lightFalloffConstantGL[lightCountGL] = currentLightFalloffConstantGL;
      lightFalloffLinearGL[lightCountGL] = currentLightFalloffLinearGL;
      lightFalloffQuadraticGL[lightCountGL] = currentLightFalloffQuadraticGL;
      lightPositionGL[lightCountGL][0] = x;
      lightPositionGL[lightCountGL][1] = y;
      lightPositionGL[lightCountGL][2] = z;
      lightPositionGL[lightCountGL][3] = 0.0f;
      
      glLightEnable(lightCountGL);
      glLightAmbient(lightCountGL);
      glLightPosition(lightCountGL);
      glLightFalloff(lightCountGL);
      
      lightCountGL++;
    }


    public void directionalLight(float r, float g, float b,
                                 float nx, float ny, float nz) {
      if (offScreenRenderer != null) { offScreenRenderer.directionalLight(r, g, b, nx, ny, nz); return; }
      
      if (!glMode) { super.directionalLight(r, g, b, nx, ny, nz); return; }      
      
      if (lightCountGL == MAX_LIGHTS_GL) {
          throw new RuntimeException("can only create " + MAX_LIGHTS_GL + " lights");
      }
      colorCalc(r, g, b);
      lightDiffuseGL[lightCountGL][0] = calcR;
      lightDiffuseGL[lightCountGL][1] = calcG;
      lightDiffuseGL[lightCountGL][2] = calcB;
      lightDiffuseGL[lightCountGL][3] = 1.0f;      

      lightTypeGL[lightCountGL] = DIRECTIONAL;
      lightFalloffConstantGL[lightCountGL] = currentLightFalloffConstantGL;
      lightFalloffLinearGL[lightCountGL] = currentLightFalloffLinearGL;
      lightFalloffQuadraticGL[lightCountGL] = currentLightFalloffQuadraticGL;
      lightSpecularGL[lightCountGL][0] = currentLightSpecularGL[0];
      lightSpecularGL[lightCountGL][1] = currentLightSpecularGL[1];
      lightSpecularGL[lightCountGL][2] = currentLightSpecularGL[2];
      lightSpecularGL[lightCountGL][2] = currentLightSpecularGL[3];
      
      float invn = 1.0f / PApplet.dist(0, 0, 0, nx, ny, nz);
      lightNormalGL[lightCountGL][0] = invn * nx;
      lightNormalGL[lightCountGL][1] = invn * ny;
      lightNormalGL[lightCountGL][2] = invn * nz;
      lightNormalGL[lightCountGL][3] = 0.0f;      
      
      glLightEnable(lightCountGL);
      glLightNoAmbient(lightCountGL);
      glLightDirection(lightCountGL);
      glLightDiffuse(lightCountGL);
      glLightSpecular(lightCountGL);
      glLightFalloff(lightCountGL);      
      
      lightCountGL++;
    }


    public void pointLight(float r, float g, float b,
                           float x, float y, float z) {
      if (offScreenRenderer != null) { offScreenRenderer.pointLight(r, g, b, x, y, z); return; }
      
      if (!glMode) { super.pointLight(r, g, b, x, y, z); return; }
      
      if (lightCountGL == MAX_LIGHTS_GL) {
           throw new RuntimeException("can only create " + MAX_LIGHTS_GL + " lights");
      }
      colorCalc(r, g, b);
      lightDiffuseGL[lightCountGL][0] = calcR;
      lightDiffuseGL[lightCountGL][1] = calcG;
      lightDiffuseGL[lightCountGL][2] = calcB;
      lightDiffuseGL[lightCountGL][3] = 1.0f;   
      
      lightTypeGL[lightCountGL] = POINT;
      lightFalloffConstantGL[lightCountGL] = currentLightFalloffConstantGL;
      lightFalloffLinearGL[lightCountGL] = currentLightFalloffLinearGL;
      lightFalloffQuadraticGL[lightCountGL] = currentLightFalloffQuadraticGL;
      lightSpecularGL[lightCountGL][0] = currentLightSpecularGL[0];
      lightSpecularGL[lightCountGL][1] = currentLightSpecularGL[1];
      lightSpecularGL[lightCountGL][2] = currentLightSpecularGL[2];
      
      lightPositionGL[lightCountGL][0] = x;
      lightPositionGL[lightCountGL][1] = y;
      lightPositionGL[lightCountGL][2] = z;
      lightPositionGL[lightCountGL][3] = 0.0f;
      
      glLightEnable(lightCountGL);
      glLightNoAmbient(lightCountGL);
      glLightPosition(lightCountGL);
      glLightDiffuse(lightCountGL);
      glLightSpecular(lightCountGL);
      glLightFalloff(lightCountGL);      
      
      lightCountGL++;
    }

    public void spotLight(float r, float g, float b,
                          float x, float y, float z,
                          float nx, float ny, float nz,
                          float angle, float concentration) {
      if (offScreenRenderer != null) { offScreenRenderer.spotLight(r, g, b, x, y, z, nx, ny, nz, angle, concentration); return; }      

      if (!glMode) { super.spotLight(r, g, b, x, y, z, nx, ny, nz, angle, concentration); return; }      
      
      if (lightCountGL == MAX_LIGHTS_GL) {
        throw new RuntimeException("can only create " + MAX_LIGHTS_GL + " lights");
      }
      colorCalc(r, g, b);
      lightDiffuseGL[lightCountGL][0] = calcR;
      lightDiffuseGL[lightCountGL][1] = calcG;
      lightDiffuseGL[lightCountGL][2] = calcB;
      lightDiffuseGL[lightCountGL][3] = 1.0f;      

      lightTypeGL[lightCountGL] = SPOT;
      lightFalloffConstantGL[lightCountGL] = currentLightFalloffConstantGL;
      lightFalloffLinearGL[lightCountGL] = currentLightFalloffLinearGL;
      lightFalloffQuadraticGL[lightCountGL] = currentLightFalloffQuadraticGL;
      lightSpecularGL[lightCountGL][0] = currentLightSpecularGL[0];
      lightSpecularGL[lightCountGL][1] = currentLightSpecularGL[1];
      lightSpecularGL[lightCountGL][2] = currentLightSpecularGL[2];
      
      lightPositionGL[lightCountGL][0] = x;
      lightPositionGL[lightCountGL][1] = y;
      lightPositionGL[lightCountGL][2] = z;
      lightPositionGL[lightCountGL][3] = 0.0f;
      
      float invn = 1.0f / PApplet.dist(0, 0, 0, nx, ny, nz);
      lightNormalGL[lightCountGL][0] = invn * nx;
      lightNormalGL[lightCountGL][1] = invn * ny;
      lightNormalGL[lightCountGL][2] = invn * nz;
      lightNormalGL[lightCountGL][3] = 0.0f;  
      
      lightSpotAngleGL[lightCountGL] = angle;
      lightSpotAngleCosGL[lightCountGL] = Math.max(0, (float) Math.cos(angle));
      lightSpotConcentrationGL[lightCountGL] = concentration;
      
      glLightEnable(lightCountGL);
      glLightNoAmbient(lightCountGL);
      glLightPosition(lightCountGL);
      glLightDirection(lightCountGL);
      glLightDiffuse(lightCountGL);
      glLightSpecular(lightCountGL);
      glLightFalloff(lightCountGL);
      glLightSpotAngle(lightCountGL);
      glLightSpotConcentration(lightCountGL);      
      
      lightCountGL++;
    }


    /**
     * Set the light falloff rates for the last light that was created.
     * Default is lightFalloff(1, 0, 0).
     */
    public void lightFalloff(float constant, float linear, float quadratic) {
      if (offScreenRenderer != null) { offScreenRenderer.lightFalloff(constant, linear, quadratic); return; }

      if (!glMode) { super.lightFalloff(constant, linear, quadratic); return; }
      
      currentLightFalloffConstantGL = constant;
      currentLightFalloffLinearGL = linear;
      currentLightFalloffQuadraticGL = quadratic;
    }

    /**
     * Set the specular color of the last light created.
     */
    public void lightSpecular(float x, float y, float z) {
      if (offScreenRenderer != null) { offScreenRenderer.lightSpecular(x, y, z); return; }

      if (!glMode) { super.lightSpecular(x, y, z); return; }
      
      colorCalc(x, y, z);
      currentLightSpecularGL[0] = calcR;
      currentLightSpecularGL[1] = calcG;
      currentLightSpecularGL[2] = calcB;
      currentLightSpecularGL[3] = 1.0f;      
    }
    
    private void glLightAmbient(int num) {
      gl.glLightfv(GL.GL_LIGHT0 + num,
                   GL.GL_AMBIENT, lightDiffuseGL[num], 0);
    }

    private void glLightNoAmbient(int num) {
      gl.glLightfv(GL.GL_LIGHT0 + num,
                   GL.GL_AMBIENT, zeroBufferGL, 0);
    }

    private void glLightDiffuse(int num) {
      gl.glLightfv(GL.GL_LIGHT0 + num,
                   GL.GL_DIFFUSE, lightDiffuseGL[num], 0);
    }

    private void glLightDirection(int num) {
      if (lightTypeGL[num] == DIRECTIONAL) {
        // TODO this expects a fourth arg that will be set to 1
        //      this is why lightBuffer is length 4,
        //      and the [3] element set to 1 in the constructor.
        //      however this may be a source of problems since
        //      it seems a bit "hack"
        gl.glLightfv(GL.GL_LIGHT0 + num, GL.GL_POSITION,
                     lightNormalGL[num], 0);
      } else {  // spotlight
        // this one only needs the 3 arg version
        gl.glLightfv(GL.GL_LIGHT0 + num, GL.GL_SPOT_DIRECTION,
                     lightNormalGL[num], 0);
      }
    }


    private void glLightEnable(int num) {
      gl.glEnable(GL.GL_LIGHT0 + num);
    }

    private void glLightDisable(int num) {
        gl.glDisable(GL.GL_LIGHT0 + num);
      }
    
    private void glLightFalloff(int num) {
      gl.glLightf(GL.GL_LIGHT0 + num,
                  GL.GL_CONSTANT_ATTENUATION, lightFalloffConstantGL[num]);
      gl.glLightf(GL.GL_LIGHT0 + num,
                  GL.GL_LINEAR_ATTENUATION, lightFalloffLinearGL[num]);
      gl.glLightf(GL.GL_LIGHT0 + num,
                  GL.GL_QUADRATIC_ATTENUATION, lightFalloffQuadraticGL[num]);
    }


    private void glLightPosition(int num) {
      gl.glLightfv(GL.GL_LIGHT0 + num, GL.GL_POSITION, lightPositionGL[num], 0);
    }


    private void glLightSpecular(int num) {
      gl.glLightfv(GL.GL_LIGHT0 + num, GL.GL_SPECULAR, lightSpecularGL[num], 0);
    }


    private void glLightSpotAngle(int num) {
      gl.glLightf(GL.GL_LIGHT0 + num,
                  GL.GL_SPOT_CUTOFF, lightSpotAngleGL[num]);
    }


    private void glLightSpotConcentration(int num) {
      gl.glLightf(GL.GL_LIGHT0 + num,
                  GL.GL_SPOT_EXPONENT, lightSpotConcentrationGL[num]);
    }

    public GLFont loadGLFont(String filename) {
      try {
        InputStream input = parent.createInput(filename);
        return new GLFont(parent, input);

      } catch (Exception e) {
        parent.die("Could not load font " + filename + ". " +
            "Make sure that the font has been copied " +
            "to the data folder of your sketch.", e);
      }
      return null;
    }

    public GLFont createGLFont(String name, float size) {
      return createGLFont(name, size, true, GLFont.DEFAULT_CHARSET);
    }

    public GLFont createGLFont(String name, float size, boolean smooth) {
      return createGLFont(name, size, smooth, GLFont.DEFAULT_CHARSET);
    }

    public GLFont createGLFont(String name, float size,
                            boolean smooth, char charset[]) {
      String lowerName = name.toLowerCase();
      Font baseFont = null;

      try {
        if (lowerName.endsWith(".otf") || lowerName.endsWith(".ttf")) {
          InputStream stream = parent.createInput(name);
          if (stream == null) {
            System.err.println("The font \"" + name + "\" " +
                               "is missing or inaccessible, make sure " +
                               "the URL is valid or that the file has been " +
                               "added to your sketch and is readable.");
            return null;
          }
          baseFont = Font.createFont(Font.TRUETYPE_FONT, parent.createInput(name));
        } else {
          //baseFont = new Font(name, Font.PLAIN, 1);
          baseFont = PFont.findFont(name);
        }
      } catch (Exception e) {
        System.err.println("Problem using createFont() with " + name);
        e.printStackTrace();
      }
      return new GLFont(parent, baseFont.deriveFont(size), smooth, charset);
    }
    
    public void endCamera()
    {
    	if (offScreenRenderer != null)
    	{
    		offScreenRenderer.endCamera();
    	}
    	else
    	{    	
            super.endCamera();
            if (glMode) loadGLModelviewMatrix();
    	}
    }
     
    public void camera()
    {
    	if (offScreenRenderer != null)
    	{
    		offScreenRenderer.camera();
    	}
    	else
    	{
            super.camera();
            if (glMode) loadGLModelviewMatrix();
    	}
    }
     
    public void camera(float eyeX, float eyeY, float eyeZ,
                       float centerX, float centerY, float centerZ,
                       float upX, float upY, float upZ)
    {
    	if (offScreenRenderer != null)
    	{	
    		// An instance of GLGraphicsOffScreen is being used, so any call to
    		// the transformation methods should be directed to the offscreen renderer.
    		offScreenRenderer.camera(eyeX, eyeY, eyeZ,
                                     centerX, centerY, centerZ,
                                     upX, upY, upZ);
    	}
    	else
    	{
            super.camera(eyeX, eyeY, eyeZ,
                         centerX, centerY, centerZ,
                         upX, upY, upZ);
            if (glMode) 
            {
            	loadGLModelviewMatrix();

                gl.glScalef(1.0f, -1.0f, 1.0f);
            }
    	}
    }

    public void ortho()
    {
    	if (offScreenRenderer != null)
    	{	    	
    		offScreenRenderer.ortho();
	    }
	    else
	    {    	
            super.ortho();
            if (glMode) loadGLProjectionMatrix();
	    }
    }
     
    public void ortho(float left, float right,
                      float bottom, float top,
                      float near, float far)
    {
    	if (offScreenRenderer != null)
    	{
    		offScreenRenderer.ortho(left, right, bottom, top, near, far);   		
        }
        else
        {      	
            super.ortho(left, right, bottom, top, near, far);
            if (glMode) loadGLProjectionMatrix();
	    }            
    }
     
    public void perspective()
    {
    	if (offScreenRenderer != null)
    	{
    		offScreenRenderer.perspective();
        }
        else
        {       	
            super.perspective();
            if (glMode) loadGLProjectionMatrix();
	    }            
    }
     
    public void perspective(float fov, float aspect, float near, float far)
    {
    	if (offScreenRenderer != null)
    	{
    		offScreenRenderer.perspective(fov, aspect, near, far);
        }
        else
        {    	
            super.perspective(fov, aspect, near, far);
            if (glMode) loadGLProjectionMatrix();
        }            
    }
     
    public void frustum(float left, float right,
                        float bottom, float top,
                        float near, float far)
    {
    	if (offScreenRenderer != null)
    	{
    		offScreenRenderer.frustum(left, right, bottom, top, near, far);    		
        }
        else
        {
        	super.frustum(left, right, bottom, top, near, far);
            if (glMode) loadGLProjectionMatrix();    	 
        }
    }

    
  public float screenX(float x, float y) {
    return screenX(x, y, 0);
  }


  public float screenY(float x, float y) {
    return screenY(x, y, 0);
  }


  public float screenX(float x, float y, float z) {
    if (offScreenRenderer != null) return offScreenRenderer.screenX(x, y, z);
    return super.screenX(x, y, z);
  }


  public float screenY(float x, float y, float z) {
    if (offScreenRenderer != null) return offScreenRenderer.screenY(x, y, z);
    return super.screenY(x, y, z);
  }


  public float screenZ(float x, float y, float z) {
    if (offScreenRenderer != null) return offScreenRenderer.screenZ(x, y, z);
    return super.screenZ(x, y, z);
  }


  public float modelX(float x, float y, float z) {
    if (offScreenRenderer != null) return offScreenRenderer.modelX(x, y, z);
    return super.modelX(x, y, z);    
  }


  public float modelY(float x, float y, float z) {    
    if (offScreenRenderer != null) return offScreenRenderer.modelY(x, y, z);
    return super.modelY(x, y, z);
  }

  
  public float modelZ(float x, float y, float z) {
    if (offScreenRenderer != null) return offScreenRenderer.modelZ(x, y, z);
    return super.modelZ(x, y, z);
  }    
    
    public void model(GLModel model)
    {
    	model.render();
    }        
    
    public void model(GLModel model, GLModelEffect effect)
    {
    	model.render(effect);
    }    
    
    public void model(GLModel model, int first, int last)
    {
    	model.render(first, last, null);
    }    
    
    public void model(GLModel model, int first, int last, GLModelEffect effect)
    {
    	model(model, 0, 0, 0, first, last, effect);
    }

    public void model(GLModel model, float x, float y, float z, int first, int last, GLModelEffect effect)
    {
    	pushMatrix();
    	translate(x, y, z);
    	model.render(first, last, effect);
    	popMatrix();
    }
    
    public void setBlendMode(int MODE)
    {
        blend = true;
        blendMode = MODE;
    }    

    public void setOffScreenRenderer(GLGraphicsOffScreen renderer)
    {
        if (offScreenRenderer != null && renderer != null) 
            System.err.println("The offscreen renderer has already been set. Probably some internal error!");	
        else 
            offScreenRenderer = renderer;
    }
    
    protected void renderTriangles(int start, int stop) 
    {
        report("render_triangles in");
        
	    blend0 = gl.glIsEnabled(GL.GL_BLEND);
        if (blend) 
        {
            gl.glEnable(GL.GL_BLEND);
            if (blendMode == BLEND) gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            else if (blendMode == ADD) gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
            else if (blendMode == MULTIPLY) gl.glBlendFunc(GL.GL_DST_COLOR, GL.GL_SRC_COLOR);
            else if (blendMode == SUBTRACT) gl.glBlendFunc(GL.GL_ONE_MINUS_DST_COLOR, GL.GL_ZERO);
            else if (blendMode == BACKGROUND_ALPHA)
            {
            	gl.glBlendColor(0.0f, 0.0f, 0.0f, 1.0f);
            	gl.glBlendFunc(GL.GL_ONE, GL.GL_CONSTANT_COLOR);
            }
        }	    
        
        for (int i = start; i < stop; i++) 
        {
            float a[] = vertices[triangles[i][VERTEX1]];
            float b[] = vertices[triangles[i][VERTEX2]];
            float c[] = vertices[triangles[i][VERTEX3]];

            // This is only true when not textured.
            // We really should pass specular straight through to triangle rendering.
            float ar = clamp(triangleColors[i][0][TRI_DIFFUSE_R] + triangleColors[i][0][TRI_SPECULAR_R]);
            float ag = clamp(triangleColors[i][0][TRI_DIFFUSE_G] + triangleColors[i][0][TRI_SPECULAR_G]);
            float ab = clamp(triangleColors[i][0][TRI_DIFFUSE_B] + triangleColors[i][0][TRI_SPECULAR_B]);
            float br = clamp(triangleColors[i][1][TRI_DIFFUSE_R] + triangleColors[i][1][TRI_SPECULAR_R]);
            float bg = clamp(triangleColors[i][1][TRI_DIFFUSE_G] + triangleColors[i][1][TRI_SPECULAR_G]);
            float bb = clamp(triangleColors[i][1][TRI_DIFFUSE_B] + triangleColors[i][1][TRI_SPECULAR_B]);
            float cr = clamp(triangleColors[i][2][TRI_DIFFUSE_R] + triangleColors[i][2][TRI_SPECULAR_R]);
            float cg = clamp(triangleColors[i][2][TRI_DIFFUSE_G] + triangleColors[i][2][TRI_SPECULAR_G]);
            float cb = clamp(triangleColors[i][2][TRI_DIFFUSE_B] + triangleColors[i][2][TRI_SPECULAR_B]);

            int textureIndex = triangles[i][TEXTURE_INDEX];
            if (textureIndex != -1)
            {
                report("before enable");
                gl.glEnable(GL.GL_TEXTURE_2D);
                report("after enable");

	            float uscale = 1.0f;
	            float vscale = 1.0f;
 		
                PImage texture = textures[textureIndex];
                
	            if (texture instanceof GLTexture)
	            {
	                  GLTexture tex = (GLTexture)texture;
  	                gl.glBindTexture(tex.getTextureTarget(), tex.getTextureID());
			
	                uscale *= tex.getMaxTextureCoordS();
	                vscale *= tex.getMaxTextureCoordT();

	                float cx = 0.0f;
	                float sx = +1.0f;
	                if (tex.isFlippedX())
	                {
	                    cx = 1.0f;			
	                    sx = -1.0f;
	                }

	                float cy = 0.0f;
	                float sy = +1.0f;
	                if (tex.isFlippedY())
                    {
                        cy = 1.0f;			
	                    sy = -1.0f;
	                }

                    gl.glBegin(GL.GL_TRIANGLES);

                        gl.glColor4f(ar, ag, ab, a[A]);
                        gl.glTexCoord2f((cx +  sx * a[U]) * uscale, (cy +  sy * a[V]) * vscale);		
	                    gl.glNormal3f(a[NX], a[NY], a[NZ]);
	                    gl.glEdgeFlag(a[EDGE] == 1);
	                    gl.glVertex3f(a[VX], a[VY], a[VZ]);

	                    gl.glColor4f(br, bg, bb, b[A]);
	                    gl.glTexCoord2f((cx +  sx * b[U]) * uscale, (cy +  sy * b[V]) * vscale);
	                    gl.glNormal3f(b[NX], b[NY], b[NZ]);
	                    gl.glEdgeFlag(a[EDGE] == 1);
	                    gl.glVertex3f(b[VX], b[VY], b[VZ]);

	                    gl.glColor4f(cr, cg, cb, c[A]);
	                    gl.glTexCoord2f((cx +  sx * c[U]) * uscale, (cy +  sy * c[V]) * vscale);			
	                    gl.glNormal3f(c[NX], c[NY], c[NZ]);
	                    gl.glEdgeFlag(a[EDGE] == 1);
                        gl.glVertex3f(c[VX], c[VY], c[VZ]);
			
                   gl.glEnd();
      	  
	               gl.glBindTexture(tex.getTextureTarget(), 0);
                } 
	            else 
                {
	                // Default texturing using a PImage.

                    report("before bind");
                    bindTexture(texture);
                    report("after bind");

                    ImageCache cash = (ImageCache) texture.getCache(this);
                    uscale = (float) texture.width / (float) cash.twidth;
                    vscale = (float) texture.height / (float) cash.theight;

	                gl.glBegin(GL.GL_TRIANGLES);

                        // System.out.println(a[U] + " " + a[V] + " " + uscale + " " + vscale);
	                    // System.out.println(ar + " " + ag + " " + ab + " " + a[A]);
                        // ar = ag = ab = 1;
                        gl.glColor4f(ar, ag, ab, a[A]);
                        gl.glTexCoord2f(a[U] * uscale, a[V] * vscale);
                        gl.glNormal3f(a[NX], a[NY], a[NZ]);
                        gl.glEdgeFlag(a[EDGE] == 1);
                        gl.glVertex3f(a[VX], a[VY], a[VZ]);

                        gl.glColor4f(br, bg, bb, b[A]);
                        gl.glTexCoord2f(b[U] * uscale, b[V] * vscale);
                        gl.glNormal3f(b[NX], b[NY], b[NZ]);
                        gl.glEdgeFlag(a[EDGE] == 1);
                        gl.glVertex3f(b[VX], b[VY], b[VZ]);

                        gl.glColor4f(cr, cg, cb, c[A]);
                        gl.glTexCoord2f(c[U] * uscale, c[V] * vscale);
                        gl.glNormal3f(c[NX], c[NY], c[NZ]);
	                    gl.glEdgeFlag(a[EDGE] == 1);
                        gl.glVertex3f(c[VX], c[VY], c[VZ]);
                    gl.glEnd();

                    report("non-binding 6");

                    gl.glDisable(GL.GL_TEXTURE_2D);
                }
            } 
            else 
            {  
                // no texture
                gl.glBegin(GL.GL_TRIANGLES);

                    gl.glColor4f(ar, ag, ab, a[A]);
                    gl.glNormal3f(a[NX], a[NY], a[NZ]);
                    gl.glEdgeFlag(a[EDGE] == 1);
                    gl.glVertex3f(a[VX], a[VY], a[VZ]);

                    gl.glColor4f(br, bg, bb, b[A]);
                    gl.glNormal3f(b[NX], b[NY], b[NZ]);
                    gl.glEdgeFlag(a[EDGE] == 1);
                    gl.glVertex3f(b[VX], b[VY], b[VZ]);

                    gl.glColor4f(cr, cg, cb, c[A]);
                    gl.glNormal3f(c[NX], c[NY], c[NZ]);
                    gl.glEdgeFlag(a[EDGE] == 1);
                    gl.glVertex3f(c[VX], c[VY], c[VZ]);

                gl.glEnd();
            }
        }

        // If there was noblending originally
        if (!blend0 && blend) gl.glDisable(GL.GL_BLEND);
        // Default blending mode in PGraphicsOpenGL.
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        
        report("render_triangles out");
    }
    
    protected boolean allWindowsReady()
    {
    	if (windowsList == null) return true;
    	boolean res = true;    	
        for (int i = 0; i < windowsList.size(); i++)
        {
            GLWindow win = (GLWindow)windowsList.get(i);
            if (!win.ready())
            {
            	res = false;
            	break;
            }
        }
        return res;
    }
    
    protected void renderWindows()
    {
    	if (windowsList == null) return;
        for (int i = 0; i < windowsList.size(); i++)
        {
            GLWindow win = (GLWindow)windowsList.get(i);
            if (!win.getOverride()) win.render();
        }    	
    }

    protected void loadGLModelviewMatrix()
    {
        gl.glMatrixMode(GL.GL_MODELVIEW);
        copyModelviewTM();
        gl.glLoadMatrixf(modelviewTM, 0);
    }
    
    protected void multGLModelviewMatrix()
    {
        gl.glMatrixMode(GL.GL_MODELVIEW);        
        copyModelviewTM();
        gl.glMultMatrixf(modelviewTM, 0);
    }

    protected void loadGLProjectionMatrix()
    {
        gl.glMatrixMode(GL.GL_PROJECTION);
        copyProjectionTM();
        gl.glLoadMatrixf(projectionTM, 0);
        
        gl.glMatrixMode(GL.GL_MODELVIEW); // We leave the modelview as the active matrix.        
    }
    
    protected void multGLProjectionMatrix()
    {
        gl.glMatrixMode(GL.GL_PROJECTION);        
        copyProjectionTM();
        gl.glMultMatrixf(projectionTM, 0);
        
        gl.glMatrixMode(GL.GL_MODELVIEW); // We leave the modelview as the active matrix.        
    }    
    
    protected void copyProjectionTM()
    {
        if (projectionTM == null) projectionTM = new float[16];
        
        projectionTM[0] = projection.m00;
        projectionTM[1] = projection.m10;
        projectionTM[2] = projection.m20;
        projectionTM[3] = projection.m30;

        projectionTM[4] = projection.m01;
        projectionTM[5] = projection.m11;
        projectionTM[6] = projection.m21;
        projectionTM[7] = projection.m31;

        projectionTM[8] = projection.m02;
        projectionTM[9] = projection.m12;
        projectionTM[10] = projection.m22;
        projectionTM[11] = projection.m32;

        projectionTM[12] = projection.m03;
        projectionTM[13] = projection.m13;
        projectionTM[14] = projection.m23;
        projectionTM[15] = projection.m33;
    }

    protected void copyModelviewTM()
    {
        if (modelviewTM == null) modelviewTM = new float[16];

        modelviewTM[0] = modelview.m00;
        modelviewTM[1] = modelview.m10;
        modelviewTM[2] = modelview.m20;
        modelviewTM[3] = modelview.m30;

        modelviewTM[4] = modelview.m01;
        modelviewTM[5] = modelview.m11;
        modelviewTM[6] = modelview.m21;
        modelviewTM[7] = modelview.m31;

        modelviewTM[8] = modelview.m02;
        modelviewTM[9] = modelview.m12;
        modelviewTM[10] = modelview.m22;
        modelviewTM[11] = modelview.m32;

        modelviewTM[12] = modelview.m03;
        modelviewTM[13] = modelview.m13;
        modelviewTM[14] = modelview.m23;
        modelviewTM[15] = modelview.m33;
    }
    
    protected GLGraphicsOffScreen offScreenRenderer = null;
    
    protected boolean glMode = false;
    protected ArrayList<GLWindow> windowsList = null;
    protected float[] modelviewTM;
    protected float[] projectionTM;
    
	protected boolean blend;
	protected boolean blend0;
	protected int blendMode;
	
	
	// Light-related variables, taken out from PGraphics3D.
	
	  /**
	   * Maximum lights by default is 8, the minimum defined by OpenGL.
	   */
	  public static final int MAX_LIGHTS_GL = 8;

	  public int lightCountGL = 0;

	  /** Light types */
	  public int[] lightTypeGL;

	  /** Light positions */
	  public float[][] lightPositionGL;

	  /** Light direction (normalized vector) */
	  public float[][] lightNormalGL;

	  /** Light falloff */
	  public float[] lightFalloffConstantGL;
	  public float[] lightFalloffLinearGL;
	  public float[] lightFalloffQuadraticGL;

	  /** Light spot angle */
	  public float[] lightSpotAngleGL;

	  /** Cosine of light spot angle */
	  public float[] lightSpotAngleCosGL;

	  /** Light spot concentration */
	  public float[] lightSpotConcentrationGL;

	  /** Diffuse colors for lights.
	   *  For an ambient light, this will hold the ambient color.
	   *  Internally these are stored as numbers between 0 and 1. */
	  public float[][] lightDiffuseGL;

	  /** Specular colors for lights.
	      Internally these are stored as numbers between 0 and 1. */
	  public float[][] lightSpecularGL;

	  /** Current specular color for lighting */
	  public float[] currentLightSpecularGL;

	  /** Current light falloff */
	  public float currentLightFalloffConstantGL;
	  public float currentLightFalloffLinearGL;
	  public float currentLightFalloffQuadraticGL;
	  
	  public float[] zeroBufferGL = { 0.0f, 0.0f, 0.0f, 0.0f };
	  
	  public int saveLightCount;
}
