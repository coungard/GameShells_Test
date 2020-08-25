package com.coungard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

public class Laucher {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        System.out.println("Enter the path to the folder:");
        String imagesPath = scanner.nextLine();
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
            System.out.println("Incorrect path " + imagesPath);
            scanner.nextLine();
            return;
        }
        System.out.println("\nFiles analysed: " + count);
        scanner.nextLine();
    }
}
