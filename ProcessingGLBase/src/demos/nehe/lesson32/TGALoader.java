package demos.nehe.lesson32;

import demos.common.ResourceRetriever;

import javax.media.opengl.GL;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Channels;
import java.nio.ByteBuffer;

import com.sun.opengl.util.BufferUtil;

public class TGALoader {
    private static final byte[] TGA_MAGIC_NUMBER = new byte[]{0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    public static TextureImage loadTGA(GL gl, String filename) throws IOException {
        ReadableByteChannel file = Channels.newChannel(ResourceRetriever.getResourceAsStream("resources/" + filename));
        try {
            return loadTGA(gl, file);
        } finally {
            try {
                file.close();
            } catch (IOException n) {
                // Ignored
            }
        }
    }

    private static TextureImage loadTGA(GL gl, ReadableByteChannel file) throws IOException {
        // Loads A TGA File Into Memory
        ByteBuffer magicNumber = BufferUtil.newByteBuffer(12); // Used To Compare TGA Header
        ByteBuffer header = BufferUtil.newByteBuffer(6); // First 6 Useful Bytes From The Header
        int type = GL.GL_RGBA; // Set The Default GL Mode To RBGA (32 BPP)

        readBuffer(file, magicNumber);
        readBuffer(file, header);

        for (int i = 0; i < magicNumber.capacity(); i++) {
            // Does The Header Match What We Want?
            if (magicNumber.get(i) != TGA_MAGIC_NUMBER[i]) {
                throw new IOException("Invalid TGA header");
            }
        }
        int width = unsignedByteToInt(header.get(1)) * 256 + unsignedByteToInt(header.get(0)); // Determine The TGA Width(highbyte*256+lowbyte)
        int height = unsignedByteToInt(header.get(3)) * 256 + unsignedByteToInt(header.get(2)); // Determine The TGA Height(highbyte*256+lowbyte)
        if (width <= 0) {
            // Is The Width Less Than Or Equal To Zero
            throw new IOException("Image has negative width");
        }
        if (height <= 0) {
            // Is The Height Less Than Or Equal To Zero
            throw new IOException("Image has negative height");
        }
        if (header.get(4) != 24 && header.get(4) != 32) {
            // Is The TGA 24 or 32 Bit?
            throw new IOException("Image is not 24 or 32-bit");
        }

        int bpp = header.get(4); // Grab The TGA's Bits Per Pixel (24 or 32)
        int bytesPerPixel = bpp / 8; // Divide By 8 To Get The Bytes Per Pixel
        int imageSize = width * height * bytesPerPixel; // Calculate The Memory Required For The TGA Data
        ByteBuffer imageData = BufferUtil.newByteBuffer(imageSize); // Reserve Memory To Hold The TGA Data
        readBuffer(file, imageData);

        for (int i = 0; i < imageSize; i += bytesPerPixel) {
            // Loop Through The Image Data
            // Swaps The 1st And 3rd Bytes ('R'ed and 'B'lue)
            byte temp = imageData.get(i); // Temporarily Store The Value At Image Data 'i'
            imageData.put(i, imageData.get(i + 2)); // Set The 1st Byte To The Value Of The 3rd Byte
            imageData.put(i + 2, temp); // Set The 3rd Byte To The Value In 'temp' (1st Byte Value)
        }

        // Build A Texture From The Data
        int[] texID = new int[1];
        gl.glGenTextures(1, texID, 0); // Generate OpenGL texture IDs
        int textureID = texID[0];

        gl.glBindTexture(GL.GL_TEXTURE_2D, textureID); // Bind Our Texture
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR); // Linear Filtered
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR); // Linear Filtered
        if (bpp == 24) {
            // Was The TGA 24 Bits
            type = GL.GL_RGB; // If So Set The 'type' To GL_RGB
        }
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, type, width, height, 0, type, GL.GL_UNSIGNED_BYTE, imageData);

        return new TextureImage(
                textureID, width, height, bytesPerPixel, imageData
        );
    }

    private static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    private static void readBuffer(ReadableByteChannel in, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            in.read(buffer);
        }
        buffer.flip();
    }
}
