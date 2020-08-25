package com.coungard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

public class Laucher {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            throw new RuntimeException("Not found image path for scanning...");
        }
        String imagesPath = args[0];
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
    }
}
