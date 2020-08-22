package com.coungard;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Test {
    public static void main(String[] args) throws IOException {

        File file = new File("test.png");
        BufferedImage image = ImageIO.read(file);

        BufferedImage subImage = image.getSubimage(0, 550, image.getWidth(), 160);
        File subFile = new File("sub.png");
        ImageIO.write(subImage, "png", subFile);

        BufferedImage clippedImage = subImage.getSubimage(145, 35, 64, 90);
        File clipped = new File("clipped.png");
        ImageIO.write(clippedImage, "png", clipped);

    }
}
