package com.coungard;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DeckAnalyzer {
    private static final String[] DECK =
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    BufferedImage image;
    BufferedImage subImage;

    public DeckAnalyzer(String path) throws IOException {
        File file = new File(path);
        image = ImageIO.read(file);
    }

    public void saveSubImage() throws IOException {
        subImage = image.getSubimage(0, 550, image.getWidth(), 160);
        File outputfile = new File("clipped.png");
        ImageIO.write(subImage, "png", outputfile);
    }

    @SuppressWarnings("unused")
    public int getCardsCount() {
        if (subImage == null) {
            System.out.println("Sub image still not clipped!");
            return 0;
        }
        // todo...
        return 0;
    }
}
