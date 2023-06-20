package org.eshishkin.trello.migrator.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class TrelloBoard {
    private String id;
    private String name;
}
