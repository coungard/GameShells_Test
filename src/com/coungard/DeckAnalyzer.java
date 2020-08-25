package com.coungard;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.coungard.Settings.*;

public class DeckAnalyzer {
    private final File file;
    private Map<Integer, Point> cards = new HashMap<>();
    private Map<Integer, Boolean> grayCards = new HashMap<>();

    private BufferedImage tableArea;
    private BufferedImage cardArea;

    public DeckAnalyzer(File file) {
        this.file = file;
    }

    public void analysis() throws IOException {
        saveSubImage();
        checkCardsCount();
        analys();
    }

    private void saveSubImage() throws IOException {
        BufferedImage image = ImageIO.read(file);
        tableArea = image.getSubimage(0, 550, image.getWidth(), 160);
    }

    private void checkCardsCount() {
        int count = 0;
        boolean found = false;
        boolean firstEntry = false;
        int pixelsEntry = 0;

        WritableRaster raster = tableArea.getRaster();
        label:
        for (int y = 30; y < raster.getHeight(); y++) {
            if (found) break;
            for (int x = 130; x < raster.getWidth(); x++) {
                Color c = new Color(tableArea.getRGB(x, y));

                if (Color.WHITE.equals(c) || GRAY_BACKGROUND.equals(c)) {
                    pixelsEntry++;
                    if (pixelsEntry == COLOR_ENTRY) {
                        if (!firstEntry) {
                            y = y + SHIFT_PIXEL_Y;
                            firstEntry = true;
                            pixelsEntry = 0;
                            continue label;
                        } else {
                            count++;
                            grayCards.put(count, GRAY_BACKGROUND.equals(c));
                            cards.put(count, new Point(x - COLOR_ENTRY, y - SHIFT_PIXEL_Y));
                            x = x - COLOR_ENTRY + CARD_WIDTH;
                            found = true;
                        }
                    }
                } else
                    pixelsEntry = 0;
            }
        }
    }

    private void analys() {
        StringBuilder builder = new StringBuilder();
        builder.append("File ").append(file.getName()).append(" - ");

        for (Map.Entry<Integer, Point> entry : cards.entrySet()) {
            Point point = entry.getValue();
            cardArea = tableArea.getSubimage(point.x, point.y, CARD_WIDTH, CARD_HEIGHT);
            WritableRaster raster = cardArea.getRaster();

            int reds = 0;
            label:
            for (int x = 0; x < raster.getWidth(); x++) {
                for (int y = 0; y < raster.getHeight(); y++) {
                    Color color = new Color(cardArea.getRGB(x, y));
                    if (color.getRed() > 60
                            && color.getRed() > color.getBlue() * 2 && color.getRed() > color.getGreen() * 2)
                        reds++;
                    if (reds > 20)
                        break label;
                }
            }
            Suit suit = reds > 20 ? checkHeartsOrDiamonds() : checkClumbsOrSpides(entry.getKey());
            Deck deck = findDeck(entry.getKey());

            builder.append(deck != null ? deck.getValue() : "").append(suit.getValue());
        }
        System.out.println(builder.toString());
    }

    private Suit checkClumbsOrSpides(int key) {
        WritableRaster raster = cardArea.getRaster();
        boolean gray = grayCards.get(key);

        int whiteSpace = 0;
        int grow = 0;
        boolean repeat = false;

        label:
        for (int x = raster.getWidth() - 10; x > raster.getWidth() / 2; x--) {
            for (int y = raster.getHeight() / 2; y < raster.getHeight() - 10; y++) {
                Color color = new Color(cardArea.getRGB(x, y));
                if (gray ? !GRAY_BACKGROUND.equals(color) : !Color.WHITE.equals(color)) {
                    if (whiteSpace != 0) {
                        if (y <= whiteSpace) {
                            if (y == whiteSpace && repeat) {
                                break label;
                            }
                            if (y == whiteSpace)
                                repeat = true;
                            grow++;
                        } else
                            break label;
                    }
                    whiteSpace = y;
                    continue label;
                }
            }
        }
        return grow > 8 ? Suit.Spades : Suit.Clubs;
    }

    private Suit checkHeartsOrDiamonds() {
        WritableRaster raster = cardArea.getRaster();

        boolean empty = false;
        boolean entry = false;
        int entries = 0;

        for (int y = raster.getHeight() - 1; y > 0; y--) {
            if (entry && empty)
                break;
            empty = true;
            for (int x = raster.getWidth() - 1; x > 0; x--) {
                Color color = new Color(cardArea.getRGB(x, y));
                if (color.getRed() > 60
                        && color.getRed() > color.getBlue() * 2 && color.getRed() > color.getGreen() * 2) {
                    entries++;
                    entry = true;
                    empty = false;
                    break;
                }
            }
        }
        return entries > 30 ? Suit.Diamonds : Suit.Hearts;
    }

    private Deck findDeck(int key) {
        BufferedImage cardValue = cardArea.getSubimage(4, 4, DIMENSION.width, DIMENSION.height);
        WritableRaster raster = cardValue.getRaster();

        boolean gray = grayCards.get(key);
        boolean tenOrQueen;
        int count = 0;
        for (int y = 0; y < raster.getHeight(); y++) {
            Color color = new Color(cardValue.getRGB(raster.getWidth() - 1, y));

            if (gray ? !GRAY_BACKGROUND.equals(color) : !Color.WHITE.equals(color))
                count++;
        }
        tenOrQueen = count > 5;

        boolean entry = false;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        label:
        for (int y = 0; y < raster.getHeight(); y++) {
            for (int x = 0; x < raster.getWidth(); x++) {
                Color color = new Color(cardValue.getRGB(x, y));

                if (gray ? GRAY_BACKGROUND.equals(color) : Color.WHITE.equals(color)) {
                    if (x == raster.getWidth() - 1 && entry)
                        break label;
                } else {
                    entry = true;
                    baos.write(x);
                    break;
                }
            }
        }
        if (tenOrQueen)
            return baos.toByteArray()[2] > 3 ? Deck.QUEEN : Deck.TEN;

        return getDeckFromBuffer(baos.toByteArray());
    }

    private Deck getDeckFromBuffer(byte[] buffer) {
        byte first = buffer[0];
        boolean figure = true;
        int index;
        for (index = 1; index < buffer.length; index++) {
            if (first != buffer[index] || first == 0) {
                figure = false;
                break;
            }
        }
        if (index > 15 && figure)
            return Deck.KING;

        figure = true;
        for (index = 0; index < buffer.length / 4; index++) {
            if (first == 0 || buffer[index] != first) {
                figure = false;
                break;
            }
        }
        int previous = buffer.length - 1;
        for (index = buffer.length - 2; index > buffer.length - 4; index--) {
            if (previous < buffer[index]) {
                figure = false;
                break;
            }
        }
        if (figure)
            return first - buffer[index] > 5 ? Deck.JACK : Deck.FIVE;

        previous = buffer[buffer.length - 2];
        for (index = buffer.length - 2; index >= 0; index--) {
            if (previous > buffer[index])
                break;

            previous = buffer[index];
        }
        if (index < buffer.length / 2) {
            if (index < 2) {
                return Deck.ACE;
            } else if (index < 5) {
                return Deck.SEVEN;
            } else {
                return Deck.TWO;
            }
        }
        for (index = 0; index < buffer.length - 1; index++) {
            if (buffer[index + 1] > buffer[index])
                break;
        }
        if (index > buffer.length / 2)
            return buffer[1] - buffer[index] > 7 ? Deck.FOUR : Deck.SEX;

        for (index = 0; index < 4; index++) {
            if (buffer[index] != buffer[index + 1])
                break;
        }
        if (index == 3)
            return Deck.THREE;

        for (index = 1; index < buffer.length - 2; index++) {
            if (buffer[index] - buffer[index + 1] > 5)
                return Deck.NINE;
        }
        return Deck.EIGHT;
    }
}
