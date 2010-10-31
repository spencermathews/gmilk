/**
 * This package provides classes to facilitate the handling of opengl textures, glsl shaders and 
 * off-screen rendering in Processing.
 * @author Andres Colubri, with many suggestions by Aaron Koblin and sigg mus
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

import processing.core.*;
import processing.opengl.*;
import processing.xml.XMLElement;

import javax.media.opengl.*;

import com.sun.opengl.util.BufferUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * This class holds a 3D model composed of vertices, normals, colors (per vertex) and 
 * texture coordinates (also per vertex). All this data is stored in Vertex Buffer Objects
 * (VBO) for fast access. 
 * This is class is still undergoing development, the API will probably change quickly
 * in the following months as features are tested and refined.
 * In particular, with the settings of the VBOs in this first implementation (GL.GL_DYNAMIC_DRAW_ARB)
 * it is assumed that the coordinates will change often during the lifetime of the model.
 * For static models a different VBO setting (GL.GL_STATIC_DRAW_ARB) should be used.
 */
public class GLModel implements PConstants, GLConstants 
{
    /**
     * Creates an instance of GLModel with the specified parameters: number of vertices,
     * mode to draw the vertices (as points, sprites, lines, etc) and usage (static if the
     * vertices will never change after the first time are initialized, dynamic if they will 
     * change frequently or stream if they will change at every frame).
     * @param parent PApplet
     * @param numVert int
     * @param mode int
     * @param usage int 
     */	
	public GLModel(PApplet parent, int numVert, int mode, int usage)
	{
		initModelCommon(parent);
        size = numVert;
    	
        vertexMode = GL.GL_TRIANGLE_STRIP;  

        if (usage == STATIC) vboUsage = GL.GL_STATIC_DRAW_ARB;
        else if (usage == DYNAMIC) vboUsage = GL.GL_DYNAMIC_DRAW_ARB;
        else if (usage == STREAM) vboUsage = GL.GL_STREAM_COPY;
        
	    gl.glGenBuffersARB(1, vertCoordsVBO, 0);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, vertCoordsVBO[0]);
        gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, size * 4 * BufferUtil.SIZEOF_FLOAT, null, vboUsage);
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
        
	}

	public GLModel(PApplet parent, float[] vertArray, int mode, int usage)
	{
		this(parent, vertArray.length / 4, mode, usage);
		updateVertices(vertArray);
	}

	public GLModel(PApplet parent, ArrayList<PVector> vertArrayList, int mode, int usage)
	{
		this(parent, vertArrayList.size(), mode, usage);
		updateVertices(vertArrayList);
	}	

	public int getSize() 
    {
       return size;
    }
    
    /**
     * Returns the OpenGL identifier of the Vertex Buffer Object holding the coordinates of 
     * this model.
     * @return int
     */	 
	public int getCoordsVBO() { return vertCoordsVBO[0]; }
	
    /**
     * This method creates n textures, i.e.: it creates the internal OpenGL variables
     * to store n textures.
     * @param n int 
     */	
	public void initTexures(int n)
	{
		numTextures = n;
    			
		texCoordsVBO = new int[numTextures];
		textures = new GLTexture[numTextures];
        gl.glGenBuffersARB(numTextures, texCoordsVBO, 0);
        for (n = 0; n < numTextures; n++)
        {
            gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, texCoordsVBO[n]); // Bind the buffer.
            gl.glBufferDataARB(GL.GL_ARRAY_BUFFER_ARB, size * 2 * BufferUtil.SIZEOF_FLOAT, null, vboUsage);
        }
        gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);     
	}
	
    /**
     * Sets the i-th texture.
     * @param i int 
     */		
	public void setTexture(int i, GLTexture tex)
	{
		textures[i] = tex;
	}
	
    /**
     * Returns the number of textures.
     * @return int
     */		
	public int getNumTextures()
	{
		return numTextures;
	}

    /**
     * Returns the i-th texture.
     * @return GLTexture
     */			
	public GLTexture getTexture(int i)
	{
		return textures[i];
	}	
	
    /**
     * Enables vertex updating, to be done with the updateVertex()/displaceVertex() methods. 
     */   
	public void beginUpdateVertices()
	{
		gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, vertCoordsVBO[0]);
		vertices = gl.glMapBufferARB(GL.GL_ARRAY_BUFFER_ARB, GL.GL_WRITE_ONLY).asFloatBuffer();
	}
	
   /**
     * Disables vertex updating.
     */   	
	public void endUpdateVertices()
	{
		if (tmpVertArray != null)
		{
			vertices.put(tmpVertArray);
            tmpVertArray = null;
            vertices.position(0);
		}
		gl.glUnmapBufferARB(GL.GL_ARRAY_BUFFER_ARB);
	    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
	}

	
    /**
     * Updates the coordinates of vertex idx.
     * @param idx int
     * @param x float
     * @param y float
     */ 	
	public void updateVertex(int idx, float x, float y)
	{
	    updateVertex(idx, x, y, 0, 1);	
	}

    /**
     * Updates the coordinates of vertex idx.
     * @param idx int
     * @param x float
     * @param y float
     * @param z float 
     */   	
	public void updateVertex(int idx, float x, float y, float z)
	{
	    updateVertex(idx, x, y, z, 1);	
	}

    /**
     * Updates the coordinates of vertex idx.
     * @param idx int
     * @param x float
     * @param y float
     * @param z float
     * @param w float
     */     	
	public void updateVertex(int idx, float x, float y, float z, float w)
	{
	    if (tmpVertArray == null) { 
	    	tmpVertArray = new float[4 * size];
	    	vertices.get(tmpVertArray);
	    	vertices.rewind();
	    }
	    
	    tmpVertArray[4 * idx + 0] = x;
	    tmpVertArray[4 * idx + 1] = y;
	    tmpVertArray[4 * idx + 2] = z;
	    tmpVertArray[4 * idx + 3] = w;
	}

    /**
     * Adds a displacement (dx, dy) to the vertex idx.
     * @param idx int
     * @param dx float
     * @param dy float
     */   	
	public void displaceVertex(int idx, float dx, float dy)
	{	
		displaceVertex(idx, dx, dy, 0, 0);
	}	

    /**
     * Adds a displacement (dx, dy, dz) to the vertex idx.
     * @param idx int
     * @param dx float
     * @param dy float
     * @param dz float 
     */     	
	public void displaceVertex(int idx, float dx, float dy, float dz)
	{	
		displaceVertex(idx, dx, dy, dz, 0);
	}

    /**
     * Adds a displacement (dx, dy, dz, dw) to the vertex idx.
     * @param idx int
     * @param dx float
     * @param dy float
     * @param dz float
     * @param dw float   
     */       	
	public void displaceVertex(int idx, float dx, float dy, float dz, float dw)
	{
	    if (tmpVertArray == null)
	    {
	    	tmpVertArray = new float[4 * size];
	    	vertices.get(tmpVertArray);
	    	vertices.rewind();
	    }
	    
	    tmpVertArray[4 * idx + 0] += dx;
	    tmpVertArray[4 * idx + 1] += dy;
	    tmpVertArray[4 * idx + 2] += dz;
	    tmpVertArray[4 * idx + 3] += dw;
	}
	
    /**
     * Updates all the vertices using the coordinates provided in
     * the array vertArray.
     * @param vertArray float[]
     */         	
	public void updateVertices(float[] vertArray)
	{
		beginUpdateVertices();
		vertices.put(vertArray);
		endUpdateVertices();
	}

    /**
     * Updates all the vertices using the coordinates provided in
     * the array vertArray.
     * @param vertArrayList ArrayList<PVector>
     */ 	
	public void updateVertices(ArrayList<PVector> vertArrayList)
	{
		if (vertArrayList.size() != size)
		{
            System.err.println("Wrong number of vertices in the array list.");
            return;
		}
		
		float p[] = new float [4 * size];
		for(int i = 0; i < vertArrayList.size(); i++)
		{
		    PVector point = (PVector)vertArrayList.get(i);
			p[4 * i + 0] = point.x;
			p[4 * i + 1] = point.y;
			p[4 * i + 2] = point.z;
			p[4 * i + 3] = 1.0f;			
		}
		updateVertices(p);		
	}	

    /**
     * Centers the model to (0, 0, 0).
     */   
	public void centerVertices()
	{
	    centerVertices(0, 0, 0);
	}

    /**
     * Centers the model to (xc, yc, 0).
     * @param xc float
     * @param yc float   
     */   	
	public void centerVertices(float xc, float yc)
	{
	    centerVertices(xc, yc, 0);
	}	
	
    /**
     * Centers the model to (xc, yc, zc).
     * @param xc float
     * @param yc float
     * @param zc float       
     */     
	public void centerVertices(float xc, float yc, float zc)
	{
     beginUpdateVertices();
     tmpVertArray = new float[4 * size];
     vertices.get(tmpVertArray);
     vertices.rewind();

    	float xave, yave, zave;
    	xave = yave =zave = 0;
    	for(int i = 0; i < size; i++) 	{
	      xave += tmpVertArray[4 * i + 0];
	      yave += tmpVertArray[4 * i + 1];
	      zave += tmpVertArray[4 * i + 2];
    	}
    	xave /= size;
    	yave /= size;
    	zave /= size;
    	
    	System.out.println(xave + " " + yave +  " " + zave);
    	
    	for(int i = 0; i < size; i++) 	{
	      tmpVertArray[4 * i + 0] += xc - xave;
	      tmpVertArray[4 * i + 1] += yc - yave;
	      tmpVertArray[4 * i + 2] += zc - zave;
    	}
        
      endUpdateVertices();
	}
	
	public void beginUpdateTexCoords(int n)
	{
		gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, texCoordsVBO[n]);
		texCoords = gl.glMapBufferARB(GL.GL_ARRAY_BUFFER_ARB, GL.GL_WRITE_ONLY).asFloatBuffer();
	}
	
	public void updateTexCoord(int idx, float s, float t)
	{
	    if (tmpTexCoordsArray == null) { 
	    	tmpTexCoordsArray = new float[2 * size];
	    	texCoords.get(tmpTexCoordsArray);
	    	texCoords.rewind();
	    }
    	
	    tmpTexCoordsArray[2 * idx + 0] = s;
	    tmpTexCoordsArray[2 * idx + 1] = t;
	}
	
	public void displaceTexCoord(int idx, float ds, float dt)
	{
	    if (tmpTexCoordsArray == null)
	    {
	    	tmpTexCoordsArray = new float[2 * size];
	    	texCoords.get(tmpTexCoordsArray);
	    	texCoords.rewind();
	    }
	    
	    tmpTexCoordsArray[2 * idx + 0] += ds;
	    tmpTexCoordsArray[2 * idx + 1] += dt;
	}	
	
	public void endUpdateTexCoords()
	{
		if (tmpTexCoordsArray != null)
		{
			texCoords.put(tmpTexCoordsArray);
			tmpTexCoordsArray = null;
			texCoords.position(0);
		}
		gl.glUnmapBufferARB(GL.GL_ARRAY_BUFFER_ARB);
	    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
	}

	public void updateTexCoords(int n, float[] texCoordsArray)
	{
		beginUpdateTexCoords(n);
		texCoords.put(texCoordsArray);
		endUpdateTexCoords();
	}

	public void updateTexCoords(int n, ArrayList<PVector> texCoordsArrayList)
	{
		if (texCoordsArrayList.size() != size)
		{
            System.err.println("Wrong number of texture coordinates in the array list.");
            return;
		}
		
		float p[] = new float [2 * size];
		for(int i = 0; i < texCoordsArrayList.size(); i++)
		{
		    PVector point = (PVector)texCoordsArrayList.get(i);
			p[2 * i + 0] = point.x;
			p[2 * i + 1] = point.y;		
		}
		updateTexCoords(n, p);		
	}	
	
	public void render()
	{
	    render(0, size - 1, null);
	}

	public void render(GLModelEffect effect)
	{
	    render(0, size - 1, effect);
	}	
	
	public void render(int first, int last)
	{
	    render(0, size - 1, null);		
	}
	
	public void render(int first, int last, GLModelEffect effect)
	{
	    
        if (effect != null) effect.start();
	    
        
	    if (normCoordsVBO != null)
	    {
	    	gl.glEnableClientState(GL.GL_NORMAL_ARRAY);
            gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, normCoordsVBO[0]);
            gl.glNormalPointer(GL.GL_FLOAT, 4 * BufferUtil.SIZEOF_FLOAT, 0);
	    }
	    	    
	    if (texCoordsVBO != null)
	    {
	    	gl.glEnable(textures[0].getTextureTarget());

            // Binding texture units.
            for (int n = 0; n < numTextures; n++)
            {
            	gl.glActiveTexture(GL.GL_TEXTURE0 + n);
                gl.glBindTexture(GL.GL_TEXTURE_2D, textures[n].getTextureID()); 
            }	    	
	    	
            	// Regular texturing.
                gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
                for (int n = 0; n < numTextures; n++)
                {
                    gl.glClientActiveTexture(GL.GL_TEXTURE0 + n);
                    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, texCoordsVBO[n]);
                    gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, 0);
                }
            
            if (effect != null) effect.setTextures(textures);         
	    }	    
	    
	    // Drawing the vertices:
	    gl.glEnableClientState(GL.GL_VERTEX_ARRAY);

	    	    
	    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, vertCoordsVBO[0]);
	    
	    // The vertices in the array have 4 components: x, y, z, w. If the user
	    // doesn't explicity specify w, then it is set to 1 by default.
	    gl.glVertexPointer(4, GL.GL_FLOAT, 0, 0);
	    
	    gl.glDrawArrays(vertexMode, first, last - first + 1);
	    
	    if (effect != null) effect.disableVertexAttribs();
	    
	    gl.glBindBuffer(GL.GL_ARRAY_BUFFER_ARB, 0);	    
	    gl.glDisableClientState(GL.GL_VERTEX_ARRAY);
	    	    
	    if (texCoordsVBO != null) 
	    {	
                for (int n = 0; n < numTextures; n++)
                {
                    gl.glClientActiveTexture(GL.GL_TEXTURE0 + n);
                    gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
                }	    		
	    	    gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
	    	gl.glDisable(textures[0].getTextureTarget());
	    }
	    if (normCoordsVBO != null) 
	    {
	    	gl.glBindBufferARB(GL.GL_ARRAY_BUFFER_ARB, 0);
	        gl.glDisableClientState(GL.GL_NORMAL_ARRAY);
	    }
	    
        // Default blending mode in PGraphicsOpenGL.
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);        
        
        if (effect != null) effect.stop();
	}	

    protected void initModelCommon(PApplet parent)
    {
		this.parent = parent;		
        pgl = (PGraphicsOpenGL)parent.g;
        gl = pgl.gl;		
		        
    	tmpVertArray = null;
    	tmpTexCoordsArray = null;
    	tmpAttributesArray = null;    	
    }

	protected PApplet parent;    
    protected GL gl;	
    protected PGraphicsOpenGL pgl;	
	protected int size;
	protected int vertexMode;
	protected int vboUsage;
	protected int[] vertCoordsVBO = { 0 };

	protected float[] tmpVertArray;
	protected float[] tmpTexCoordsArray;
	protected float[] tmpAttributesArray;
	
	protected int[] normCoordsVBO = null;	
	protected int[] texCoordsVBO = null;

	protected int numTextures;
		
	public GLTexture[] textures;
	
	public FloatBuffer vertices;
	public FloatBuffer texCoords;
	public static final int STATIC = 0;
    public static final int DYNAMIC = 1;
    public static final int STREAM = 2;	
}
