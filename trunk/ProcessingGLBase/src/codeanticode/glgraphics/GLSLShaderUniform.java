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

/**
 * This class stores the uniform variable of a GLSL shader.
 */
public class GLSLShaderUniform extends GLSLShaderVariable
{
    /**
     * Creates an instance of GLSLShaderUniform using the specified parameters. The shader is set to null.
     * @param parent PApplet
     * @param name String
     * @param label String 
     * @param type int 
     */	
    public GLSLShaderUniform(PApplet parent, String name, String label, int type) 
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
    public GLSLShaderUniform(PApplet parent, GLSLShader shader, String name, String label, int type) 
    {
    	super(parent, shader, name, label, type);
    }

    public void genID() 
    {
        if (shader != null) glID = shader.getUniformLocation(name);        
    }

    /**
     * Copies the parameter value to the GPU.
     */ 
    public void setParameter()
    {
        if (glID == -1) {
        	System.err.println("Error in texture filter parameter " + name + ": no valid opengl ID.");
            return;
        }
        if (type == TEX_FILTER_PARAM_INT) gl.glUniform1iARB(glID, valueInt);
        else if (type == TEX_FILTER_PARAM_FLOAT) gl.glUniform1fARB(glID, valueFloat);
        else if (type == TEX_FILTER_PARAM_VEC2) gl.glUniform2fvARB(glID, 1, FloatBuffer.wrap(valueArray));
        else if (type == TEX_FILTER_PARAM_VEC3) gl.glUniform3fvARB(glID, 1, FloatBuffer.wrap(valueArray));
        else if (type == TEX_FILTER_PARAM_VEC4) gl.glUniform4fvARB(glID, 1, FloatBuffer.wrap(valueArray));
        else if (type == TEX_FILTER_PARAM_MAT2) gl.glUniformMatrix2fvARB(glID, 1, false, FloatBuffer.wrap(valueArray));
        else if (type == TEX_FILTER_PARAM_MAT3) gl.glUniformMatrix3fvARB(glID, 1, false, FloatBuffer.wrap(valueArray));
        else if (type == TEX_FILTER_PARAM_MAT4) gl.glUniformMatrix4fvARB(glID, 1, false, FloatBuffer.wrap(valueArray));
        else System.err.println("Error in texture filter parameter " + name + ": Unknown type.");
    }
}
