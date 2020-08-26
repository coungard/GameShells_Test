#### Step 1: Load image as a Buffer. (x, y - pixels)
Simple way with a **BufferedImage**
#### Step 2 - Cut 10 % of picture with cards content. (They are located in the center of the picture)
How to: 
```java
subImage = image.getSubimage(0, 550, image.getWidth(), 160);
```
where 550 - y coordinate, located higher than card content. 160 - cards content height.
#### Step 3 - Check cards count. 
Logic: We iterate for x - coordinate on cards content, and trying to find entry 
WHITE(gray) color points. If we find such an entry, we are sure that there is a card in front of us.

Now we move the x coordinate to the width of this entry and add the map width to it. Then we repeat this procedure.
Example:
```java
    
        int count = 0;
        int whiteColors = 0;

        for (int y = 0; y < raster.getHeight(); y++) {
            for (int x = 0; x < raster.getWidth(); x++) {
                int RGBA = subImage.getRGB(x, y);

                int red = (RGBA >> 16) & 255;
                int green = (RGBA >> 8) & 255;
                int blue = RGBA & 255;

                if (red == WHITE_COLOR && green == WHITE_COLOR && blue == WHITE_COLOR
                    whiteColors++;
                    if (whiteColors == WHITE_COLOR_ENTRY) { // (entry = 30)
                        x = x + CARD_WIDTH - WHITE_COLOR_ENTRY; (card width = 64)
                        count++;
                    }
            }
        }
```

#### Step 4 - Check suit color
Okey, now we got cards count, those range and coordinates. <br>
We know that there are only 4 suits - and 2 of them (Hearts and Diamonds) are *red*. <br>
Solution for check a red suits is simple:
```java
    private void checkSuit() {
        for (Map.Entry<Integer, Point> entry : DeckAnalyzer.cards.entrySet()) {
            Point point = entry.getValue();
            BufferedImage clipped = subImage.getSubimage(point.x, point.y, CARD_WIDTH, CARD_HEIGHT);

            int reds = 0;

            label:
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int RGBA = clipped.getRGB(x, y);
                    if (red > 60 && red > green * 2 && red > blue * 2)
                        reds++;
                    if (reds > 20)
                        break label;
                }
            }
        }
        if (reds > 0) 
            System.out.println(entry.getKey() + " is red!");
    }
```
where Point(x, y) - a card coordinates on subImage.

#### Step 5 - Find the difference between hearts and diamonds.

The best explanation is visualization:<br>
![](res/hearts_or_diamonds.png)

We know that diamonds is ***higher*** than hearts.<br>
We start off from the bottom right point and simply measure the height of the shape's entry.

#### Step 6 - Find the difference between spades and clubs.

Example:<br>
![](res/spades_or_clubs.png)

Again we starting from the bottom right point and looking for entries on the diagonal.<br>
But now, unlike the previous algorithm, we only count the grow increment.

### Step 7 - Find the Deck.
Before we start parsing cards, we transform the points of the horizontal entries into a byte array, from the start of the first point to the last.<br>
![](res/entries.png)

#### Step 7.1 - Find the Ten and Queen.

Okay, now we got all suites, let's start looking for a deck. <br>
Before I start explaining the map search approach, I have to show this shit: <br>
![](res/cards_clipped.png)

The card cut algorithm  looks awkward.<br>
It can be seen that for the __Queen__ and __Ten__, the value of the card goes beyond the bounds.<br>
![](res/ten_or_queen.png)

For the solution, we take any initial byte of the entry. If it is less, then it is __Ten__, if it is more then a __Queen__.

#### Step 7.2 - Find the King.

Above in the picture, we noticed that the King has a __solid side__. Therefore, we check for frequent repetition in the array.
```java
int i;
for(i = 0; i < buffer.length; i++) {
    if (buffer[i] != buffer[i + 1])
        break;
}
if (i == buffer.length)
    return Deck.King;
```

#### Step 7.3 - Find Jack and Five.

![](res/jack_or_5.png)

Both of these cards have the same point entries up to the middle. <br>
To see their difference, it is enough to determine the __growth increment__ at the bottom of the figure,
and compare the final grow index with the initial one.

#### Step 7.4 - Find Ace, Seven and Two.

![](res/ace_seven_two.png)

We see continuous growth in these cards if we start from the end of the array of entry points.<br> 
For an __Ace__, this is one hundred percent growth to the beginning of the figure. 
__Seven__ has a little less, and __Two__ has the smallest.<br> As a result, we just compare:

```java
if (index < buffer.length / 2) {
    if (index < 2) {
        return Deck.ACE;
    } else if (index < 5) {
        return Deck.SEVEN;
    } else {
        return Deck.TWO;
    }
}
```

#### Step 7.5 - Find Four and Sex.

![](res/sex_or_four.png)

Similar to the previous algorithm - we calculate the grow entries, but now we do it from the beginning of the figure.<br>
Again we compare the last index with the first index:
```java
if (index > buffer.length / 2)
    return buffer[1] - buffer[index] > 7 ? Deck.FOUR : Deck.SEX;
```
