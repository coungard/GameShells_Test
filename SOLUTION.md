#### Step 1: Load image as a Buffer. (x, y - pixels)
Simple way with a **BufferedImage**
#### Step 2 - Cut 10 % of picture with cards content. (They are located in the center of the picture)
How to: 
```java
subImage = image.getSubimage(0, 550, image.getWidth(), 160);
```
550 - y coordinate, located higher than card content. 160 - cards content height.
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
