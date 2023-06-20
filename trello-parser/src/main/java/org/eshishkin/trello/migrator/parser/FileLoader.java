package org.eshishkin.trello.migrator.parser;

import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.StreamSupport;

public class FileLoader {

    @SneakyThrows
    public static List<String> load(String directory) {
        List<String> data;
        try (var stream = Files.newDirectoryStream(Path.of(directory))) {
            data = StreamSupport.stream(stream.spliterator(), true)
                    .map(FileLoader::readString)
                    .toList();
        }
        return data;
    }

    @SneakyThrows
    private static String readString(Path path) {
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
