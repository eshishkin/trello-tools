package org.eshishkin.trello.migrator.loader.openproject;

import lombok.SneakyThrows;
import org.eshishkin.trello.migrator.model.domain.TrelloBackup;
import org.eshishkin.trello.migrator.utils.JsonUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    @SneakyThrows
    public static void main(String... args) {
        var input = args[0];

        if (args[0] == null || args[1] == null) {
            throw new IllegalArgumentException("No input parameters");
        }

        var data = JsonUtils.parse(Files.readString(Path.of(args[0]), StandardCharsets.UTF_8), TrelloBackup.class);
        var settings = JsonUtils.parse(Files.readString(Path.of(args[1]), StandardCharsets.UTF_8), Settings.class);

        new Migrator(data, settings).migrate();
    }
}
