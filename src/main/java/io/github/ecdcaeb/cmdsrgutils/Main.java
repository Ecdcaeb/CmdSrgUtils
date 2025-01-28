package io.github.ecdcaeb.cmdsrgutils;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.neoforged.srgutils.IMappingFile;

import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        OptionSpec<String> from  = parser.accepts("from",  "The source format").withRequiredArg().ofType(String.class).required();
        OptionSpec<String> to  = parser.accepts("to",  "The result format").withRequiredArg().ofType(String.class).required();
        OptionSet options;
        try {
            options = parser.parse(expandArgs(args));
        } catch (OptionException ex) {
            System.err.println("Error: " + ex.getMessage());
            System.err.println();
            parser.printHelpOn(System.err);
            System.exit(1);
            return;
        }
        IMappingFile.Format eFrom = IMappingFile.Format.get(options.valueOf(from));
        IMappingFile.Format eTo = IMappingFile.Format.get(options.valueOf(to));

        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.println("\nEnter the path of the From file or type \"exit\" to exit...");
            String input = scanner.nextLine();
            if (input.equals("exit"))
                break;
            File fromFile = new File(input);
            if (!fromFile.exists() || !fromFile.isFile()) {
                System.out.println("No such file, please try again.");
                continue;
            }
            System.out.println("Loading " + input);
            IMappingFile mappingFile;
            try {
                mappingFile = IMappingFile.load(fromFile);
            } catch (IOException e) {
                System.out.println("Error while loading file");
                e.printStackTrace();
                System.out.println();
                continue;
            }
            File outputFile = new File(input + '.' + eTo.name());
            System.out.println("Writing " + outputFile.getPath());
            try {
                outputFile.createNewFile();
                mappingFile.write(outputFile.toPath(), eTo, false);
            } catch (IOException e) {
                System.out.println("Error while writing file");
                e.printStackTrace();
                System.out.println();
                continue;
            }
            System.out.println("\nComplete!");
        }

        scanner.close();
    }

    private static String[] expandArgs(String[] args) throws IOException {
        List<String> ret = new ArrayList<>();
        for (int x = 0; x < args.length; x++) {
            if (args[x].equals("--cfg")) {
                if (x + 1 == args.length)
                    throw new IllegalArgumentException("No value specified for '--cfg'");

                try (Stream<String> lines = Files.lines(Paths.get(args[++x]))) {
                    lines.forEach(ret::add);
                }
            } else if (args[x].startsWith("--cfg=")) {
                try (Stream<String> lines = Files.lines(Paths.get(args[x].substring(6)))) {
                    lines.forEach(ret::add);
                }
            } else {
                ret.add(args[x]);
            }
        }

        return ret.toArray(new String[ret.size()]);
    }
}

