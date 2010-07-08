package demos.nehe.lesson31;

import demos.common.TextureReader;

import javax.media.opengl.GL;
import java.io.IOException;
import java.io.File;

/**
 * This class renders a Milkshape 3D model via JOGL.
 *
 * @author Nikolaj Ougaard
 */
class MS3DJOGLRenderer {
    /**
     * This method renders the given Milshape 3D model on the given GL.
     *
     * @param gl
     * @param ms3dModel
     */
    public void renderModel(GL gl, MS3DModel ms3dModel) {
        //Take one Group ( Mesh ) at a time 
        for (int gc = 0; gc < ms3dModel.groups.length; gc++) {
            //Setup material and texture 
            int materialIndex = ms3dModel.groups[gc].materialIndex;
            if (materialIndex >= 0) {
                gl.glMaterialfv(GL.GL_FRONT, GL.GL_AMBIENT, ms3dModel.materials[materialIndex].ambient, 0);
                gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, ms3dModel.materials[materialIndex].diffuse, 0);
                gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, ms3dModel.materials[materialIndex].specular, 0);
                gl.glMaterialfv(GL.GL_FRONT, GL.GL_EMISSION, ms3dModel.materials[materialIndex].emissive, 0);
                gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, ms3dModel.materials[materialIndex].shininess);

                if (!"".equalsIgnoreCase(ms3dModel.materials[materialIndex].textureName)) {
                    gl.glBindTexture(GL.GL_TEXTURE_2D, ms3dModel.materials[materialIndex].textureId);
                    gl.glEnable(GL.GL_TEXTURE_2D);
                } else {
                    gl.glDisable(GL.GL_TEXTURE_2D);
                }
            } else {
                gl.glDisable(GL.GL_TEXTURE_2D);
            }

            //Render the group by drawing all the triangles and setting normals and texture coordinates
            gl.glBegin(GL.GL_TRIANGLES);
            {
                for (int tc = 0; tc < ms3dModel.groups[gc].numTriangles; tc++) {
                    int triangleIndex = ms3dModel.groups[gc].triangleIndices[tc];
                    MS3DTriangle t = ms3dModel.triangles[triangleIndex];

                    for (int vc = 0; vc < 3; vc++) {
                        int index = t.vertexIndices[vc];

                        gl.glNormal3fv(t.vertexNormals[vc], 0);
                        gl.glTexCoord2f(t.s[vc], t.t[vc]);
                        gl.glVertex3fv(ms3dModel.vertices[index].location, 0);
                    }
                }
            }
            gl.glEnd();
        }
    }

    /**
     * This method loads and assign the textures
     *
     * @param gl
     * @param ms3dModel
     */
    public void loadTextures(GL gl, MS3DModel ms3dModel, String baseDirectory) {
        int noTextures = ms3dModel.materials.length;
        int[] textures = new int[noTextures];
        gl.glGenTextures(noTextures, textures, 0);

        for (int m = 0; m < ms3dModel.materials.length; m++) {
            String textureName = ms3dModel.materials[m].textureName;
            if (textureName.length() > 0) {
                gl.glBindTexture(GL.GL_TEXTURE_2D, textures[m]);
                TextureReader.Texture texture;
                try {
                    texture = TextureReader.readTexture(baseDirectory + textureName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                gl.glTexImage2D(GL.GL_TEXTURE_2D,
                                0,
                                3,
                                texture.getWidth(),
                                texture.getHeight(),
                                0,
                                GL.GL_RGB,
                                GL.GL_UNSIGNED_BYTE,
                                texture.getPixels());

                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
                ms3dModel.materials[m].textureId = textures[m];
            }
        }
    }
}
