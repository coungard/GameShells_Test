package com.coungard;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public class DeckAnalyzer {
    private static final String[] DECK =
            {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    private static final int CARD_WIDTH = 64;
    private static final int WHITE_COLOR_ENTRY = 30;
    private static final int SHIFT_PIXEL_Y = 3;
    private final File[] files;

    BufferedImage subImage;

    public DeckAnalyzer(String directoryPath) {
        File dir = new File(directoryPath);
        // Чтение полного списка файлов каталога
        files = dir.listFiles();
    }

    public void analysis() throws IOException {
        for (int i = 0; i < files.length; i++) {
            saveSubImage(files[i].getPath());
            getCardsCount(files[i].getPath());
        }
    }

    private void saveSubImage(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedImage image = ImageIO.read(file);

        subImage = image.getSubimage(0, 550, image.getWidth(), 160);
        File outputfile = new File("clipped.png");
        ImageIO.write(subImage, "png", outputfile);
    }

    private void getCardsCount(String filePath) {
        if (subImage == null) {
            System.out.println("Sub image still not clipped!");
            return;
        }
        int count = 0;
        boolean found = false;
        boolean firstEntry = false;
        int whiteColors = 0;

        WritableRaster raster = subImage.getRaster();
        int width = raster.getWidth();
        int height = raster.getHeight();

        label:
        for (int y = 0; y < height; y++) {
            if (found)
                break;
            for (int x = 0; x < width; x++) {
                int RGBA = subImage.getRGB(x, y);

                int red = (RGBA >> 16) & 255;
                int green = (RGBA >> 8) & 255;
                int blue = RGBA & 255;

                if (red == 255 && green == 255 && blue == 255) {
                    whiteColors++;
                    if (whiteColors == WHITE_COLOR_ENTRY) {
                        if (!firstEntry) {
                            y = y + SHIFT_PIXEL_Y;
                            firstEntry = true;
                            whiteColors = 0;
                            continue label;
                        }
                        x = x + CARD_WIDTH - WHITE_COLOR_ENTRY;
                        count++;
                        found = true;
                    }
                } else {
                    whiteColors = 0;
                }
            }
        }
        System.out.println("For file " + filePath + "\t Cards count = " + count);
    }
}
