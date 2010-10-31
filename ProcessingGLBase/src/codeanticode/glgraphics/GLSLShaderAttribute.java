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

import processing.core.*;
import java.nio.*;

import javax.media.opengl.GL;

/**
 * This class stores the attribute variable of a GLSL shader.
 */
public class GLSLShaderAttribute extends GLSLShaderVariable
{
    /**
     * Creates an instance of GLSLShaderAttribute using the specified parameters. The shader is set to null.
     * @param parent PApplet
     * @param name String
     * @param label String 
     * @param type int 
     */	
    public GLSLShaderAttribute(PApplet parent, String name, String label, int type) 
    {
        super(parent, null, name, label, type);
    }

    /**
     * Creates an instance of GLSLShaderUniform using the specified parameters.
     * @param parent PApplet
     * @param shader GLSLShader
     * @param name String
     * @param label String 
     * @param type int 
     */	
    public GLSLShaderAttribute(PApplet parent, GLSLShader shader, String name, String label, int type) 
    {
    	super(parent, shader, name, label, type);
    }

    public void genID() 
    {
        if (shader != null) glID = shader.getAttribLocation(name);        
    }
    
    public void enableVertexAttribArray()
    {
    	gl.glEnableVertexAttribArray(glID);
    }

    public void disableVertexAttribArray()
    {
    	gl.glDisableVertexAttribArray(glID);
    }    
    
    public void setAttribArrayPointer(int size, boolean normalized, int fist)
    {
        gl.glVertexAttribPointerARB(glID, size, GL.GL_FLOAT, normalized, 0, fist);    
    }
    
    /**
     * Copies the parameter value to the GPU.
     */ 
    public void setAttribute()
    {
        if (glID == -1) {
        	System.err.println("Error in shader attribute " + name + ": no valid opengl ID.");
            return;
        }
        else if (type == TEX_FILTER_PARAM_FLOAT) gl.glVertexAttrib1fARB(glID, valueFloat);
        else if (type == TEX_FILTER_PARAM_VEC2) gl.glVertexAttrib2fvARB(glID, FloatBuffer.wrap(valueArray));
        else if (type == TEX_FILTER_PARAM_VEC3) gl.glVertexAttrib3fvARB(glID, FloatBuffer.wrap(valueArray));
        else if (type == TEX_FILTER_PARAM_VEC4) gl.glVertexAttrib4fvARB(glID, FloatBuffer.wrap(valueArray));
        else System.err.println("Error in shader attribute " + name + ": Unknown type.");
    }
}
