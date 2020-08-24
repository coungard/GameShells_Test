package com.coungard;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static com.coungard.Settings.*;

public class DeckAnalyzer {
    private static final Map<Integer, Point> cards = new HashMap<>();
    private List<Suit> suitList = new ArrayList<>();
    private List<Deck> deckList = new ArrayList<>();
    private final File file;

    private BufferedImage tableArea;
    private BufferedImage cardArea;

    private StringBuilder cardSequenceString;
    public static int errors;

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
        tableArea = image.getSubimage(0, 550, image.getWidth(), 160);

//        try {
//            ImageIO.write(tableArea, "png", new File("sub/" + file.getPath()));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void checkCardsCount() {
        if (tableArea == null) {
            System.out.println("Sub image still not clipped!");
            return;
        }

        int count = 0;
        boolean found = false;
        boolean firstEntry = false;
        int whiteColors = 0;

        WritableRaster raster = tableArea.getRaster();
        int width = raster.getWidth();
        int height = raster.getHeight();

        label:
        for (int y = 0; y < height; y++) {
            if (found) break;
            for (int x = 0; x < width; x++) {
                Color c = new Color(tableArea.getRGB(x, y));

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
//        for (Map.Entry<Integer, Point> entry : DeckAnalyzer.cards.entrySet()) {
//            Point point = entry.getValue();
//            BufferedImage clipped = tableArea.getSubimage(point.x, point.y, CARD_WIDTH, CARD_HEIGHT);
//            try {
//                String name = file.getName();
//                String[] parts = name.split("\\.");
//                ImageIO.write(clipped, "png",
//                        new File("sub/images/" + parts[0] + "_" + entry.getKey() + "." + parts[1]));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
        System.out.print("For file " + file + "\t Cards count = " + count + "\t");
    }

    private void checkSuit() throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("Cards suit: ");

        for (Map.Entry<Integer, Point> entry : DeckAnalyzer.cards.entrySet()) {
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
            boolean redSuit = reds > 20;

            Deck deck = findDeck(entry.getKey());
            Suit suit;
            if (redSuit) {
                suit = checkHeartsOrDiamonds(cardArea);
            } else {
                suit = checkClumbsOrSpides(cardArea);
            }

            builder.append(entry.getKey())
                    .append("=[")
                    .append(deck != null ? deck : "")
                    .append(" ")
                    .append(suit)
                    .append("] ");
        }

        cardSequenceString = builder;
        boolean valid = checkValid();
        if (!valid) {
            errors++;
        }

        builder.append("\t").append(valid ? " NICE" : "ERROR!");
        System.out.println(builder.toString());
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

    private Deck findDeck(int key) throws IOException {
        BufferedImage cardValue = cardArea.getSubimage(4, 4, DIMENSION.width, DIMENSION.height);
        String name = file.getName();
        String[] parts = name.split("\\.");
        ImageIO.write(cardValue, "png",
                new File("sub/images/" + parts[0] + "_" + key + "_val." + parts[1]));

        WritableRaster raster = cardValue.getRaster();
        int width = raster.getWidth();
        int height = raster.getHeight();

        boolean tenOrQueen = false;
        for (int y = 0; y < height; y++) {
            Color color = new Color(cardValue.getRGB(width - 1, y));
            if (!Color.WHITE.equals(color)) {
                tenOrQueen = true;
            }
        }

        boolean entry = false;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        label:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = new Color(cardValue.getRGB(x, y));

                if (Color.WHITE.equals(color)) {
                    if (x == width - 1 && entry)
                        break label;
                } else {
                    entry = true;
                    baos.write(x);
                    break;
                }
            }
        }
        System.out.println(Arrays.toString(baos.toByteArray()));

        if (tenOrQueen) {
            if (baos.toByteArray()[1] > 8) {
                deckList.add(Deck.QUEEN);
                return Deck.QUEEN;
            } else {
                deckList.add(Deck.TEN);
                return Deck.TEN;
            }
        }
        return getResultFromBytes(baos.toByteArray());
    }

    // check sequence: King, Jack, Five
    private Deck getResultFromBytes(byte[] buffer) {
        byte first = buffer[0];
        byte second = buffer[1];
        byte third = buffer[2];
        byte last = buffer[buffer.length - 1];

        // king
        boolean figure = true;
        int index;
        for (index = 1; index < buffer.length; index++) {
            if (first != buffer[index] || first == 0) {
                figure = false;
                break;
            }
        }
        if (index > 15 && figure) {
            deckList.add(Deck.KING);
            return Deck.KING;
        }

        //jack, five
        figure = true;
        for (index = 0; index < buffer.length / 2; index++) {
            if (first == 0 || buffer[index] != first) {
                figure = false;
                break;
            }
        }
        int oldValue = buffer.length - 1;
        for (index = buffer.length - 2; index > buffer.length - 4; index--) {
            if (oldValue < buffer[index]) {
                figure = false;
                break;
            }
        }
        if (figure) {
            if (first - buffer[index] > 5) {
                deckList.add(Deck.JACK);
                return Deck.JACK;
            }
            if (first - buffer[index] > 0) {
                deckList.add(Deck.FIVE);
                return Deck.FIVE;
            }
        }
        return null;
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
        for (char c : content) {
            switch (c) {
                case 'K':
                    if (deckList.size() <= iter) {
                        return false;
                    }
                    boolean valid = deckList.get(iter).equals(Deck.KING);
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
            }
        }
        return true;
    }
}
















