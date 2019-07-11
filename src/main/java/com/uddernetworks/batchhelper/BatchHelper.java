package com.uddernetworks.batchhelper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BatchHelper {

    private static final boolean USE_FILE = false;
    private static final boolean RUN_DEMO = true;

    private static final String SAVE_LOCATION = USE_FILE ? "ascii-colored.txt" : "ascii-cli-colored.txt";

    // Shorthand codes meant for places that may require a lot of colors per line
    private static final Map<String, Color> COLOR_SHORTHANDS = new LinkedHashMap<>() {{
        put("`S", Color.STRONG_BLACK);
        put("`BR", Color.STRONG_YELLOW);
        put("`B", Color.CYAN);
        put("`GR", Color.GREEN);
        put("`O", Color.YELLOW);
        put("`R", Color.RED);
    }};

    public static void main(String[] args) throws IOException {
        var helper = new BatchHelper();
        helper.main();

        if (RUN_DEMO) helper.runDemo();
    }

    private String input;

    public BatchHelper() throws IOException {
        if (USE_FILE) {
            input = new String(Files.readAllBytes(Paths.get("input\\input.txt")));
            return;
        }

        var inputBuilder = new StringBuilder();
        var scanner = new Scanner(System.in);
        System.out.println("Please supply the input string to convert (Input will end when a newline is received):");
        for (String line; (line = scanner.nextLine()) != null;) {
            if (line.isBlank()) break;
            inputBuilder.append(line).append('\n');
        }

        input = inputBuilder.toString();
    }

    public void main() {
        var maxLineLength = Arrays.stream(input.split("\n")).mapToInt(this::getRealLength).max().orElse(0);

        var textOut = new StringBuilder();
        Arrays.stream(input.split("\n")).forEach(line -> {
            if (line.startsWith("^center^")) {
                var length = getRealLength(line);
                var spacesLeft = (maxLineLength - length) / 2D;
                line = String.format("%-" + spacesLeft + "s", "") + line.substring(8);
            }

            textOut.append(Color.RESET).append(line).append(Color.RESET);
        });
        input = textOut.toString();

        // Replacing normal colors
        Arrays.stream(Color.values()).forEach(color -> input = input.replace("^" + color.name() + "^", color.toString()));

        // Replacing shorthands
        COLOR_SHORTHANDS.forEach((friendly, replace) -> input = input.replace(friendly, replace.toString()));

        // Write to output
        saveText(SAVE_LOCATION);
    }

    // Simply output (Via batch) the file
    public void runDemo() {
        try {
            System.out.println("Demo of the generated text:\n");

            Files.write(new File("demo.bat").toPath(), ("@echo off\ntype output\\" + SAVE_LOCATION).getBytes(), StandardOpenOption.CREATE);
            Commandline.runCommand("demo.bat");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Gets length of all data not inside of %'s
    private int getRealLength(String line) {
        var length = line.length();
        Matcher m = Pattern.compile("(\\^)(?:(?=(\\\\?))\\2.)*?\\1").matcher(line);
        while (m.find()) {
            length -= m.group().length();
        }

        return length;
    }

    private void saveText(String name) {
        try {
            var output = new File("output\\" + name);
            output.createNewFile();
            Files.write(output.toPath(), input.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Generated file has been saved to " + name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
