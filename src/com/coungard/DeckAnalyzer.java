package com.coungard;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coungard.Settings.*;

public class DeckAnalyzer {
    private final File file;
    BufferedImage subImage;

    private static final Map<Integer, Point> cards = new HashMap<>();

    private List<Suit> suitList = new ArrayList<>();

    public DeckAnalyzer(File file) {
        this.file = file;
        cards.clear();
        suitList.clear();
    }

    public void analysis() throws IOException {
        saveSubImage();
        checkCardsCount();
        checkSuit();
    }

    private void saveSubImage() throws IOException {
        BufferedImage image = ImageIO.read(file);
        subImage = image.getSubimage(0, 550, image.getWidth(), 160);

        try {
            ImageIO.write(subImage, "png", new File("sub/" + file.getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                        } else {
                            count++;
                            cards.put(count, new Point(x - WHITE_COLOR_ENTRY, y - SHIFT_PIXEL_Y));
                            x = x - WHITE_COLOR_ENTRY + CARD_WIDTH;
                            found = true;
                        }
                    }
                } else {
                    whiteColors = 0;
                }
            }
        }
        for (Map.Entry<Integer, Point> entry : DeckAnalyzer.cards.entrySet()) {
            Point point = entry.getValue();
            BufferedImage clipped = subImage.getSubimage(point.x, point.y, CARD_WIDTH, CARD_HEIGHT);
            try {
                String name = file.getName();
                String[] parts = name.split("\\.");
                ImageIO.write(clipped, "png",
                        new File("sub/images/" + parts[0] + "_" + entry.getKey() + "." + parts[1]));
            } catch (IOException e) {
                e.printStackTrace();
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

            String suitValue = "";
            if (redSuit) {
                Suit suit = checkHeartsOrDiamonds(clipped);
                suitValue = "red [" + suit + "]";
            } else {
                Suit suit = checkClumbsOrSpides(clipped);
                suitValue = "black [" + suit + "]";
            }

            builder.append(entry.getKey())
                    .append("=")
                    .append(suitValue)
                    .append(" ");
        }
        boolean valid = checkValid();

        builder.append("\t").append(valid ? " NICE" : "ERROR!");

        System.out.println(builder.toString());
    }

    private boolean checkValid() {
        String fileName = file.getName(); // 2cQc5s_3c.png
        char[] content = fileName.split("_")[0].toCharArray();

        int iter = 0;
        for (char c : content) {
            switch (c) {
                case 'c':
                    boolean valid = suitList.get(iter).equals(Suit.Clubs);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                case 's':
                    valid = suitList.get(iter).equals(Suit.Spades);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                case 'h':
                    valid = suitList.get(iter).equals(Suit.Hearts);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                case 'd':
                    valid = suitList.get(iter).equals(Suit.Diamonds);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private Suit checkClumbsOrSpides(BufferedImage clipped) {
        WritableRaster raster = clipped.getRaster();

        int whiteSpace = 0;
        int grow = 0;
        boolean repeat = false;

        label:
        for (int x = raster.getWidth() - 10; x > raster.getWidth() / 2; x--) {
            for (int y = raster.getHeight() / 2; y < raster.getHeight() - 10; y++) {
                Color color = new Color(clipped.getRGB(x, y));
                if (!Color.WHITE.equals(color)) {
                    if (whiteSpace != 0) {
                        if (y <= whiteSpace) {
                            if (y == whiteSpace && repeat) {
                                break label;
                            }
                            if (y == whiteSpace)
                                repeat = true;
                            grow++;
                        } else {
                            break label;
                        }
                    }
                    whiteSpace = y;
                    continue label;
                }
            }
        }
        if (grow > 8) {
            suitList.add(Suit.Spades);
            return Suit.Spades;
        } else {
            suitList.add(Suit.Clubs);
            return Suit.Clubs;
        }
    }

    private Suit checkHeartsOrDiamonds(BufferedImage clipped) {
        WritableRaster raster = clipped.getRaster();

        boolean empty = false;
        boolean entry = false;
        int entries = 0;

        for (int y = raster.getHeight() - 1; y > 0; y--) {
            if (entry && empty)
                break;
            empty = true;
            for (int x = raster.getWidth() - 1; x > 0; x--) {
                Color color = new Color(clipped.getRGB(x, y));
                if (color.getRed() > 60
                        && color.getRed() > color.getBlue() * 2 && color.getRed() > color.getGreen() * 2) {
                    entries++;
                    entry = true;
                    empty = false;
                    break;
                }
            }
        }
        if (entries > 30) {
            suitList.add(Suit.Diamonds);
            return Suit.Diamonds;
        } else {
            suitList.add(Suit.Hearts);
            return Suit.Hearts;
        }
    }
}
















