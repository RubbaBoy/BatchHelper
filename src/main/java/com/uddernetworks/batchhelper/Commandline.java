package com.uddernetworks.batchhelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

/**
 * Adapted from MS Paint IDE
 */
public class Commandline {
    public static int runCommand(String... command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Process process = processBuilder.start();

            inheritIO(process.getInputStream());
            inheritIO(process.getErrorStream());

            var exitCode = 1;
            Runtime.getRuntime().addShutdownHook(new Thread(process::destroyForcibly));

            try {
                exitCode = process.waitFor();
            } catch (InterruptedException ignored) { // This is probably from manually stopping the process; nothing bad to report
                process.destroyForcibly();
            }

            System.out.println("Process terminated with " + exitCode);
            return exitCode;
        } catch (IOException e) {
            System.err.println("An error occurred while running command with arguments " + command);
            e.printStackTrace();
            return -1;
        }
    }

    private static void inheritIO(InputStream inputStream) {
        CompletableFuture.runAsync(() -> {
            Scanner sc = new Scanner(inputStream);
            while (sc.hasNextLine()) {
                System.out.println(sc.nextLine());
            }
        });
    }
}
