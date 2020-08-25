package com.coungard;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.coungard.Settings.*;

public class DeckAnalyzer {
    private Map<Integer, Point> cards = new HashMap<>();
    private List<Suit> suitList = new ArrayList<>();
    private List<Deck> deckList = new ArrayList<>();
    private final File file;

    private BufferedImage tableArea;
    private BufferedImage cardArea;

    public static int errors;
    private Map<Integer, Boolean> grayCards = new HashMap<>();

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
        int whiteColors = 0;

        WritableRaster raster = tableArea.getRaster();
        label:
        for (int y = 30; y < raster.getHeight(); y++) {
            if (found) break;
            for (int x = 130; x < raster.getWidth(); x++) {
                Color c = new Color(tableArea.getRGB(x, y));

                if (Color.WHITE.equals(c) || GRAY_BACKGROUND.equals(c)) {
                    whiteColors++;
                    if (whiteColors == COLOR_ENTRY) {
                        if (!firstEntry) {
                            y = y + SHIFT_PIXEL_Y;
                            firstEntry = true;
                            whiteColors = 0;
                            continue label;
                        } else {
                            count++;
                            grayCards.put(count, GRAY_BACKGROUND.equals(c));
                            cards.put(count, new Point(x - COLOR_ENTRY, y - SHIFT_PIXEL_Y));
                            x = x - COLOR_ENTRY + CARD_WIDTH;
                            found = true;
                        }
                    }
                } else {
                    whiteColors = 0;
                }
            }
        }
    }

    private void analys() {
        StringBuilder builder = new StringBuilder();
        builder.append("File ").append(file.getName()).append(" Deck : ");

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
            boolean isRed = reds > 20;

            Suit suit = isRed ? checkHeartsOrDiamonds() : checkClumbsOrSpides(entry.getKey());
            Deck deck = findDeck(entry.getKey());
            suitList.add(suit);
            deckList.add(deck);

            builder.append(entry.getKey()).append("=[")
                    .append(deck != null ? deck : "").append(" ").append(suit).append("] ");
        }

        boolean valid = checkValid();
        if (!valid)
            errors++;

        builder.append("\t").append(valid ? " NICE" : "ERROR!");
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

        return getResultFromBytes(baos.toByteArray());
    }

    private Deck getResultFromBytes(byte[] buffer) {
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

        iter = 0;
        boolean valid;
        for (char c : content) {
            switch (c) {
                case '2':
                    if (deckList.size() <= iter) {
                        return false;
                    }
                    valid = deckList.get(iter).equals(Deck.TWO);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                case '3':
                    if (deckList.size() <= iter) {
                        return false;
                    }
                    valid = deckList.get(iter).equals(Deck.THREE);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                case '4':
                    if (deckList.size() <= iter) {
                        return false;
                    }
                    valid = deckList.get(iter).equals(Deck.FOUR);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                case '5':
                    if (deckList.size() <= iter) {
                        return false;
                    }
                    valid = deckList.get(iter).equals(Deck.FIVE);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                case '6':
                    if (deckList.size() <= iter) {
                        return false;
                    }
                    valid = deckList.get(iter).equals(Deck.SEX);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                case '7':
                    if (deckList.size() <= iter) {
                        return false;
                    }
                    valid = deckList.get(iter).equals(Deck.SEVEN);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                case '8':
                    if (deckList.size() <= iter) {
                        return false;
                    }
                    valid = deckList.get(iter).equals(Deck.EIGHT);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                case '9':
                    if (deckList.size() <= iter) {
                        return false;
                    }
                    valid = deckList.get(iter).equals(Deck.NINE);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                case '1':
                    if (deckList.size() <= iter) {
                        return false;
                    }
                    valid = deckList.get(iter).equals(Deck.TEN);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                case 'J':
                    if (deckList.size() <= iter) {
                        return false;
                    }
                    valid = deckList.get(iter).equals(Deck.JACK);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                case 'Q':
                    if (deckList.size() <= iter) {
                        return false;
                    }
                    valid = deckList.get(iter).equals(Deck.QUEEN);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                case 'K':
                    if (deckList.size() <= iter) {
                        return false;
                    }
                    valid = deckList.get(iter).equals(Deck.KING);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
                case 'A':
                    if (deckList.size() <= iter) {
                        return false;
                    }
                    valid = deckList.get(iter).equals(Deck.ACE);
                    iter++;
                    if (!valid) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }
}
