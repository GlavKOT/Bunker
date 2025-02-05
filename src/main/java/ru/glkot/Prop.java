package ru.glkot;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Prop {
    public static void createFiles() {
        Path path = Path.of(Path.of("").toAbsolutePath().toString(),"properties");
        path.toFile().mkdirs();
        if (path.toFile().listFiles() != null) {
            List<String> out = new ArrayList<>();
            for (File file : path.toFile().listFiles()) {
                out.add(file.getName().replaceAll(".prop",""));
            }


            File file = Path.of(Path.of("").toAbsolutePath().toString(),"order.txt").toFile();


            try {

                if (!file.exists()) file.createNewFile();


                FileInputStream zov = new FileInputStream(file);
                String string = new String(zov.readAllBytes());
                List<String> stringArray;
                if (string.contains("\n")) {
                    stringArray = new ArrayList<>(Arrays.stream(string.split("\n")).toList());
                } else {
                    stringArray = new ArrayList<>(List.of(string));
                }
                System.out.println(out);
                System.out.println("\n");
                System.out.println(stringArray);

                out.removeAll(stringArray);
                if (!out.isEmpty() && !stringArray.isEmpty()) stringArray.addAll(out);
                else if (stringArray.isEmpty()) stringArray = out;



                PrintWriter writer = new PrintWriter(file);

                for (String s : stringArray) {
                    writer.println(s);
                }

                writer.flush();
                writer.close();


            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }

    }
    public static int propCount() {
        Path path = Path.of(Path.of("").toAbsolutePath().toString(),"properties");
        if (path.toFile().listFiles() != null) {
            return path.toFile().listFiles().length;
        }
        return 0;
    }
    public static List<String> getAllProperties() {
        Path path = Path.of(Path.of("").toAbsolutePath().toString(),"properties");
        if (path.toFile().listFiles() != null) {

            List<String> out = new ArrayList<>();

            for (File file : path.toFile().listFiles()) {
                try {
                    FileInputStream zov = new FileInputStream(file);
                    out.add(new String(zov.readAllBytes()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return out;

        }
        return List.of();
    }
    public static List<String> getAllNames() {
        File file = Path.of(Path.of("").toAbsolutePath().toString(),"order.txt").toFile();


        try {

            if (!file.exists()) return List.of();


            FileInputStream zov = new FileInputStream(file);
            String string = new String(zov.readAllBytes());
            List<String> stringArray;
            if (string.contains("\n")) {
                return Arrays.stream(string.split("\n")).toList();
            } else {
                return List.of(string);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String random(String prop) {
        File file = Path.of(Path.of("").toAbsolutePath().toString(),"properties",prop+".prop").toFile();

        if (file.exists()) {
            try {
                FileInputStream zov = new FileInputStream(file);
                String string = new String(zov.readAllBytes());
                String[] stringArray;
                if (string.contains("\n")) {
                    stringArray = string.split("\n");
                } else {
                    stringArray = (String[]) List.of(string).toArray();
                }

                if (stringArray.length > 1) {
                    return stringArray[new Random().nextInt(0, stringArray.length)];
                } else if (!string.isBlank()) {
                    return string;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return "-";
    }

    public static int position(String prop) {
        File file = Path.of(Path.of("").toAbsolutePath().toString(),"order.txt").toFile();
        System.out.println(file.getPath());

        try {

            if (!file.exists()) return -1;


            FileInputStream zov = new FileInputStream(file);
            String string = new String(zov.readAllBytes());
            List<String> stringArray;
            if (string.contains("\n")) {
                stringArray = Arrays.stream(string.split("\n")).toList();
            } else {
                stringArray = List.of(string);
            }


            return stringArray.indexOf(prop) + 1;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
