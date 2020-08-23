package com.coungard;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.coungard.Settings.*;

public class DeckAnalyzer {
    private final File file;
    BufferedImage subImage;

    private static final Map<Integer, Point> cards = new HashMap<>();

    public DeckAnalyzer(File file) {
        this.file = file;
        cards.clear();
    }

    public void analysis() throws IOException {
        saveSubImage();
        checkCardsCount();
        checkSuit();
    }

    private void saveSubImage() throws IOException {
        BufferedImage image = ImageIO.read(file);
        subImage = image.getSubimage(0, 550, image.getWidth(), 160);
    }

    private void checkCardsCount() {
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
            if (found) break;

            for (int x = 0; x < width; x++) {
                Color c = new Color(subImage.getRGB(x, y));

                if (c.getRed() == 255 && c.getGreen() == 255 && c.getBlue() == 255
                        || c.getRed() == 120 && c.getGreen() == 120 && c.getBlue() == 120) {
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
                        x = x - WHITE_COLOR_ENTRY + CARD_WIDTH;
                        found = true;
                    }
                } else {
                    whiteColors = 0;
                }
            }
        }
        System.out.print("For file " + file + "\t Cards count = " + count + "\t");
    }

    private void checkSuit() {
        StringBuilder builder = new StringBuilder();
        builder.append("Cards suit: ");

        for (Map.Entry<Integer, Point> entry : DeckAnalyzer.cards.entrySet()) {
            Point point = entry.getValue();
            BufferedImage clipped = subImage.getSubimage(point.x, point.y, CARD_WIDTH, CARD_HEIGHT);

            WritableRaster raster = clipped.getRaster();

            int reds = 0;
            label:
            for (int x = 0; x < raster.getWidth(); x++) {
                for (int y = 0; y < raster.getHeight(); y++) {
                    Color color = new Color(clipped.getRGB(x, y));
                    if (color.getRed() > 60
                            && color.getRed() > color.getBlue() * 2 && color.getRed() > color.getGreen() * 2)
                        reds++;
                    if (reds > 20)
                        break label;
                }
            }
            boolean redSuit = reds > 20;

            if (redSuit) {
                checkHeartsOrDiamonds(raster);
            }

            String suit = reds > 20 ? "red" : "black";
            builder.append(entry.getKey())
                    .append("=")
                    .append(suit)
                    .append(" ");
        }
        System.out.println(builder.toString());
    }

    private Suit checkHeartsOrDiamonds(Raster raster) {
        // TODO...

        return Suit.Hearts;
    }
}
