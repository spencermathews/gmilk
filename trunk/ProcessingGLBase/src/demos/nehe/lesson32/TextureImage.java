package demos.nehe.lesson32;

import java.nio.ByteBuffer;

class TextureImage {
    // Create A Structure
    private ByteBuffer imageData; // Image Data (Up To 32 Bits)

    private int bpp; // Image Color Depth In Bits Per Pixel.

    private int width; // Image Width

    private int height; // Image Height

    private int texID; // Texture ID Used To Select A Texture

    TextureImage(int texID, int width, int height, int bpp, ByteBuffer imageData) {
        this.texID = texID;
        this.width = width;
        this.height = height;
        this.bpp = bpp;
        this.imageData = imageData;
    }

    public int getBpp() {
        return bpp;
    }

    public int getHeight() {
        return height;
    }

    public ByteBuffer getImageData() {
        return imageData;
    }

    public int getTexID() {
        return texID;
    }

    public int getWidth() {
        return width;
    }

    public void printInfo() {
        System.out.println("Byte per Pixel: " + bpp + "\n" + "Image Width: " + width + "\n" + "Image Height:" + height + "\n");
    }
}
