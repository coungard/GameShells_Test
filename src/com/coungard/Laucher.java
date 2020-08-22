package com.coungard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Laucher {
    private static final String imagesPath = "images";

    public static void main(String[] args) throws IOException {
        if (Files.isDirectory(Paths.get(imagesPath))) {
            File dir = new File(imagesPath);
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    DeckAnalyzer analyzer = new DeckAnalyzer(file);
                    analyzer.analysis();
                }
            }
        } else {
            throw new RuntimeException("Directory " + imagesPath + " does not exists");
        }


    }
}
