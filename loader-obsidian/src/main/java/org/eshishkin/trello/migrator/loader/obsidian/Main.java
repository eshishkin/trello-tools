package org.eshishkin.trello.migrator.loader.obsidian;

import org.eshishkin.trello.migrator.model.domain.TrelloBackup;
import org.eshishkin.trello.migrator.model.domain.TrelloCard;
import org.eshishkin.trello.migrator.utils.JsonUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;
import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.BooleanUtils.isTrue;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

public class Main {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String... args) throws IOException {
        var input = args[0];
        var output = args[1];

        if (input == null || output == null) {
            throw new IllegalArgumentException("No input parameters");
        }

        var tasks = JsonUtils.parse(Files.readString(Path.of(input)), TrelloBackup.class)
                .getCards()
                .stream()
                .sorted(comparing(TrelloCard::getCreated, nullsFirst(reverseOrder())))
                .map(Main::toTask)
                .collect(joining("\n"));

        Files.writeString(Path.of(output), tasks, StandardCharsets.UTF_8);
    }

    private static String toTask(TrelloCard card) {
        return new StringBuilder()
                .append(" - [")
                .append(isTrue(card.getDueComplete()) ? "x" : SPACE)
                .append("] ")
                .append(card.getName())
                .append(" ")
                .append(card.getCreated() != null ? " ➕ " + card.getCreated().format(FORMATTER) : EMPTY)
                .append(card.getStart() != null ? " \uD83D\uDEEB " + card.getStart().format(FORMATTER) : EMPTY)
                .append(card.getDue() != null ? " \uD83D\uDCC5 " + card.getDue().format(FORMATTER) : EMPTY)
                .append(card.getDueComplete() == Boolean.TRUE ? " ✅ " + card.getDue().format(FORMATTER) : EMPTY)
                .append("\n")
                .toString();
    }
}
