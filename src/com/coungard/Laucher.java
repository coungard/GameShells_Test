package com.coungard;

import java.io.IOException;

public class Laucher {

    public static void main(String[] args) throws IOException {
        DeckAnalyzer deckAnalyzer = new DeckAnalyzer("imgs");
        deckAnalyzer.analysis();
    }
}
