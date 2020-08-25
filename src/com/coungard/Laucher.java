package com.coungard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

public class Laucher {
    private static final String imagesPath = "images";

    public static void main(String[] args) throws IOException {
        if (!Files.exists(Paths.get("sub/"))) {
            Files.createDirectory(Paths.get("sub/"));
        }
        if (!Files.exists(Paths.get("sub/images/"))) {
            Files.createDirectory(Paths.get("sub/images/"));
        }

        DeckAnalyzer.errors = 0;
        int count = 0;
        if (Files.isDirectory(Paths.get(imagesPath))) {
            File dir = new File(imagesPath);
            File[] files = dir.listFiles();

            if (files != null) {
                Arrays.sort(files, Comparator.comparing(File::getName));
                for (File file : files) {
                    if (file.getName().split("\\.").length < 3) {
                        DeckAnalyzer analyzer = new DeckAnalyzer(file);
                        analyzer.analysis();
                        count++;
                    }
                }
            }
        } else {
            throw new RuntimeException("Directory " + imagesPath + " does not exists");
        }
        System.out.println("\nFiles analysed: " + count);
        System.out.println("Errors: " + DeckAnalyzer.errors);
        System.out.println("Total percentage of errors: " + (DeckAnalyzer.errors * 100 / count) + "%");
    }
}
