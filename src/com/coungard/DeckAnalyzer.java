package com.coungard;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.coungard.Settings.*;

public class DeckAnalyzer {
    private final File file;
    BufferedImage subImage;

    public DeckAnalyzer(File file) {
        this.file = file;
    }

    public void analysis() throws IOException {
        saveSubImage();
        getCardsCount();
    }

    private void saveSubImage() throws IOException {
        BufferedImage image = ImageIO.read(file);
        subImage = image.getSubimage(0, 550, image.getWidth(), 160);
    }

    private void getCardsCount() throws IOException {
        if (subImage == null) {
            System.out.println("Sub image still not clipped!");
            return;
        }
        Map<Integer, Point> cards = new HashMap<>();

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

                if (red == 255 && green == 255 && blue == 255 || red == 120 && green == 120 && blue == 120) {
                    whiteColors++;
                    if (whiteColors == WHITE_COLOR_ENTRY) {
                        if (!firstEntry) {
                            y = y + SHIFT_PIXEL_Y;
                            firstEntry = true;
                            whiteColors = 0;
                            continue label;
                        }
                        count++;
                        cards.put(count, new Point(x - WHITE_COLOR_ENTRY, y));
                        x = x + CARD_WIDTH - WHITE_COLOR_ENTRY;
                        found = true;
                    }
                } else {
                    whiteColors = 0;
                }
            }
        }
        System.out.print("For file " + file + "\t Cards count = " + count + "\t");
        checkSuit(cards);
    }

    private void checkSuit(Map<Integer, Point> coordinates) {
        StringBuilder builder = new StringBuilder();
        builder.append("Cards suit: ");

        for (Map.Entry<Integer, Point> entry : coordinates.entrySet()) {
            Point point = entry.getValue();
            BufferedImage clipped = subImage.getSubimage(point.x, point.y, CARD_WIDTH, CARD_HEIGHT);

            WritableRaster raster = clipped.getRaster();
            int width = raster.getWidth();
            int height = raster.getHeight();

            int reds = 0;

            label:
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int RGBA = clipped.getRGB(x, y);

                    int red = (RGBA >> 16) & 255;
                    int green = (RGBA >> 8) & 255;
                    int blue = RGBA & 255;

                    if (red > 60 && red > green * 2 && red > blue * 2)
                        reds++;
                    if (reds > 20)
                        break label;
                }
            }
            String suit = reds > 20 ? "red" : "black";
            builder.append(entry.getKey())
                    .append("=")
                    .append(suit)
                    .append(" ");
        }
        System.out.println(builder.toString());
    }
}
