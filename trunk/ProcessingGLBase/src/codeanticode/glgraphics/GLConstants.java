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

/**
 * This interface class defines constants used in the glgraphics package.
 */
public interface GLConstants 
{
    /**
     * This constant identifies the GLGraphics renderer.
     */
    static final String GLGRAPHICS = "codeanticode.glgraphics.GLGraphics";

    /**
     * This constant identifies the texture target GL_TEXTURE_2D, that is, textures with normalized coordinates.
     */
    public static final int TEX_NORM = 0;
    /**
     * This constant identifies the texture target GL_TEXTURE_RECTANGLE, that is, textures with non-normalized coordinates
     */
    public static final int TEX_RECT = 1;
    /**
     * This constant identifies the texture target GL_TEXTURE_1D, that is, one-dimensional textures.
     */
    public static final int TEX_ONEDIM = 2;	

    /**
     * This constant identifies the texture internal format GL_RGBA: 4 color components of 8 bits each, identical as ARGB.
     */
    public static final int COLOR = 2;  
    /**
     * This constant identifies the texture internal format GL_RGBA16F_ARB: 4 float compontents of 16 bits each.
     */
    public static final int FLOAT = 6;
    /**
     * This constant identifies the texture internal format GL_RGBA32F_ARB: 4 float compontents of 32 bits each.
     */	
    public static final int DOUBLE = 7;  


    /**
     * This constant identifies an image buffer that contains only RED channel info.
     */       
    //public static final int RED = 0;
    
    /**
     * This constant identifies an image buffer that contains only GREEN channel info.
     */		
    //public static final int GREEN = 0;

    /**
     * This constant identifies an image buffer that contains only BLUE channel info.
     */		
    //public static final int BLUE = 0;
    /**
     * This constant identifies an image buffer that contains only ALPHA channel info.
     */		
    //public static final int ALPHA = 0; Already defined in Processing with value = 4
    
    /**
     * This constant identifies an integer texture buffer.
     */
    public static final int TEX_INT = 0;
    /**
     * This constant identifies an unsigned byte texture buffer.
     */
    public static final int TEX_BYTE = 1;

    /**
     * This constant identifies the nearest texture filter .
     */
    public static final int NEAREST = 0;
    /**
     * This constant identifies the linear texture filter .
     */
    public static final int LINEAR = 1;
    /**
     * This constant identifies the nearest/nearest function to build mipmaps .
     */
    public static final int NEAREST_MIPMAP_NEAREST = 2;
    /**
     * This constant identifies the linear/nearest function to build mipmaps .
     */
    public static final int LINEAR_MIPMAP_NEAREST = 3;
    /**
     * This constant identifies the nearest/linear function to build mipmaps .
     */
    public static final int NEAREST_MIPMAP_LINEAR = 4;
    /**
     * This constant identifies the linear/linear function to build mipmaps .
     */
    public static final int LINEAR_MIPMAP_LINEAR = 5;
    
    public static final int LINE_STRIP = 0;
    public static final int LINE_LOOP = 1;   
    public static final int POINT_SPRITES = 3;
        
    /**
     * These constants identifies the texture parameter types.
     */
    public static final int TEX_FILTER_PARAM_INT = 0;
    public static final int TEX_FILTER_PARAM_FLOAT = 1;
    public static final int TEX_FILTER_PARAM_VEC2 = 2;	
    public static final int TEX_FILTER_PARAM_VEC3 = 3;
    public static final int TEX_FILTER_PARAM_VEC4 = 4;
    public static final int TEX_FILTER_PARAM_MAT2 = 5;
    public static final int TEX_FILTER_PARAM_MAT3 = 6;
    public static final int TEX_FILTER_PARAM_MAT4 = 7;

    public static final int GL_DEPTH_STENCIL = 0x84F9;
    public static final int GL_UNSIGNED_INT_24_8 = 0x84FA;
    public static final int GL_DEPTH24_STENCIL8 = 0x88F0;
    
    public static final int BACKGROUND_ALPHA = 16384;
    
    /**
     * 
     * @author pierre
     * 
     * 
     *
     */
    public enum KeyboardAction { DRAW_AXIS, DRAW_GRID, DISPLAY_FPS, ENABLE_TEXT, EXIT_VIEWER,
		SAVE_SCREENSHOT, CAMERA_MODE, FULL_SCREEN, STEREO, ANIMATION, HELP, EDIT_CAMERA,
		MOVE_CAMERA_LEFT, MOVE_CAMERA_RIGHT, MOVE_CAMERA_UP, MOVE_CAMERA_DOWN,
		INCREASE_FLYSPEED, DECREASE_FLYSPEED, SNAPSHOT_TO_CLIPBOARD
	};
	
	public enum MouseHandler { CAMERA, FRAME };
	
	public enum ClickAction { NO_CLICK_ACTION, ZOOM_ON_PIXEL, ZOOM_TO_FIT, SELECT, RAP_FROM_PIXEL, RAP_IS_CENTER,
		CENTER_FRAME, CENTER_SCENE, SHOW_ENTIRE_SCENE, ALIGN_FRAME, ALIGN_CAMERA };
		
	enum MouseAction { NO_MOUSE_ACTION,
			ROTATE, ZOOM, TRANSLATE,
			MOVE_FORWARD, LOOK_AROUND, MOVE_BACKWARD,
			SCREEN_ROTATE, ROLL, DRIVE,
			SCREEN_TRANSLATE, ZOOM_ON_REGION };
}
