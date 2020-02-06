package de.moviemanager.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.util.StringUtils;

import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public enum  FileUtils {
    ;

    public static List<String> readAllLines(final File file) throws IOException {
        try (final FileReader fileReader = new FileReader(file);
             final BufferedReader reader = new BufferedReader(fileReader)) {
            final List<String> result = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line);
            }
            return result;
        }
    }

    public static void writeLines(final File file, final List<String> lines) throws IOException {
        if (file.exists() && wasDeleteNotSuccessful(file)) {
            throw new IOException("File couldn't be deleted!");
        }
        createDirectory(file);

        try (final FileWriter fileWriter = new FileWriter(file);
             final BufferedWriter writer = new BufferedWriter(fileWriter)) {
            for (final String line : lines) {
                writer.write(line + "\n");
            }
        }
    }

    private static boolean wasDeleteNotSuccessful(final File file) {
        return !file.delete();
    }

    public static File resolve(final File file, final String other) {
        return new File(file, other);
    }

    public static File resolve(final File file, final File other) {
        return new File(file, other.getPath());
    }

    public static void createDirectory(final File file) throws IOException {
        File parentDirectory = file.getAbsoluteFile();
        if (file.getName().contains(".")) {
            parentDirectory = file.getAbsoluteFile().getParentFile();
        }

        if (!parentDirectory.exists() && !parentDirectory.mkdirs()) {
            throw new IOException("Couldn't create directory '" + file.getAbsolutePath() + "'");
        }
    }

    public static void delete(final File file) throws IOException {
        if (file.isFile() && wasDeleteNotSuccessful(file)) {
            throw new IOException("Couldn't delete file '" + file.getAbsolutePath() + "'");
        } else if(file.isDirectory()) {
            list(file).forEach(contentFile -> {
                try {
                    delete(contentFile);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });

            if(wasDeleteNotSuccessful(file)) {
                throw  new IOException("Couldn't delete directory '"
                        + file.getAbsolutePath()
                        + "'");
            }
        }
    }

    public static File relativize(final File file, final File other) {
        String[] leftPath = file.getAbsolutePath().split(Pattern.quote(File.separator));
        String[] rightPath = other.getAbsolutePath().split(Pattern.quote(File.separator));

        int length = Math.min(leftPath.length, rightPath.length);
        int sharedElements = 0;

        for (; sharedElements < length; ++sharedElements) {
            if (!leftPath[sharedElements].equals(rightPath[sharedElements])) {
                break;
            }
        }
        if (sharedElements != length) {
            throw new IllegalArgumentException("Files have different roots!");
        }

        List<String> result;
        if (leftPath.length < rightPath.length) {
            result = Arrays.asList(rightPath).subList(sharedElements, rightPath.length);
        } else {
            result = Arrays.asList(leftPath).subList(sharedElements, leftPath.length);
        }

        return new File(StringUtils.join(File.separator, result));
    }

    public static void copy(final File source,
                            final File destination) throws IOException {
        copy(source, destination, false);
    }

    private static void copy(final File source,
                             final File destination,
                             boolean replaceExisting) throws IOException {
        sourceChecks(source);
        destinationChecks(destination, replaceExisting);
        if (source.isDirectory()) {
            copyEachFile(source, destination, replaceExisting);
            return;
        }

        if (destination.exists()) {
            FileUtils.delete(destination);
        } else {
            createDirectory(destination);
            if (!destination.createNewFile()) {
                throw new IOException("Creation of destination '" +
                        destination.getAbsolutePath() +
                        "' failed!"
                );
            }
        }

        int bufferSize = calculateBufferSize(source.length());

        try (FileOutputStream fos = new FileOutputStream(destination);
             FileInputStream fis = new FileInputStream(source)
        ) {
            transferFromTo(fis, fos, bufferSize);
        }
    }

    private static void sourceChecks(final File source) throws IOException {
        if (!source.exists()) {
            throw new IOException("Source '" + source.getAbsolutePath() + "' doesn't exist!");
        }
    }

    private static void destinationChecks(final File destination,
                                          boolean replaceExisting) throws IOException {
        final String absolutePath = destination.getAbsolutePath();
        if (destination.exists() && !replaceExisting) {
            throw new IOException("Destination '" + absolutePath + "' does exists and " +
                    "replace existing is not allowed!");
        } else if (destination.exists()
                && destination.isDirectory()
                && replaceExisting
                && destination.list().length > 0) {
            throw new IOException("Destination '" + absolutePath + "' can't be replaced, " +
                    "because it is a non-empty directory!");
        }
    }

    private static void copyEachFile(final File source, final File destination, boolean replaceExisting) {
        ofNullable(source.listFiles())
                .map(Arrays::asList)
                .orElse(new ArrayList<>())
                .forEach(sourceFile -> {
                            try {
                                copy(sourceFile,
                                        resolve(destination, sourceFile.getName()),
                                        replaceExisting
                                );
                            } catch (IOException e) {
                                throw new IllegalStateException(e);
                            }
                        }
                );
    }

    private static int calculateBufferSize(long fileLength) {
        int minSize = 1024;
        int maxSize = 1048576;
        return Math.max(minSize, Math.min((int) (fileLength / 10), maxSize));
    }

    public static void transferFromTo(final InputStream input,
                                      final OutputStream out) throws IOException {
        transferFromTo(input, out, 4096);
    }

    private static void transferFromTo(final InputStream input,
                                       final OutputStream out,
                                       int bufferSize) throws IOException {
        byte[] buffer = new byte[bufferSize];
        for (int read; (read = input.read(buffer)) != -1; ) {
            out.write(buffer, 0, read);
        }
    }

    public static boolean exists(final File file) {
        return file.exists();
    }

    public static File get(final String arg, final String... args) {
        List<String> elements = new ArrayList<>();
        elements.add(requireNonNull(arg));
        for (final String element : args) {
            elements.add(requireNonNull(element));
        }

        return new File(StringUtils.join(File.separator, elements));
    }

    public static Stream<File> walk(final File directory) {
        final List<File> queue = new ArrayList<>();
        orderedWalk(queue, directory);
        return queue.stream();
    }

    private static void orderedWalk(final List<File> files, final File root) {
        final Deque<File> toWalk = new ArrayDeque<>();
        toWalk.push(root);

        while (!toWalk.isEmpty()) {
            final File file = toWalk.pop();

            if (!file.exists()) {
                continue;
            }
            files.add(file);

            if (file.isDirectory()) {
                final File[] content = ofNullable(file.listFiles())
                        .orElse(new File[]{});

                stream(content)
                        .sorted(comparing(File::isDirectory)
                                .thenComparing(File::getName)
                        )
                        .forEach(toWalk::push);

            }
        }
    }

    public static Stream<File> list(final File root) {
        return ofNullable(root.listFiles())
                .map(Arrays::asList)
                .orElseGet(Collections::emptyList)
                .stream()
                .sorted(comparing(File::isDirectory).thenComparing(File::getName));
    }
}

