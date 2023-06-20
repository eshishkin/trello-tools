package org.eshishkin.trello.migrator.parser;

import org.eshishkin.trello.migrator.utils.JsonUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String... args) throws IOException {
        var input = args[0];
        var output = args[1];

        if (input == null || output == null) {
            throw new IllegalArgumentException("No input parameters");
        }

        var boards = FileLoader.load(input);
        var backup = Parser.parse(boards);
        Files.writeString(Path.of(output), JsonUtils.serialize(backup), StandardCharsets.UTF_8);
    }
}
