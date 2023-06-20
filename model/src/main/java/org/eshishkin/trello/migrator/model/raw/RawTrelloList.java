package org.eshishkin.trello.migrator.model.raw;

import lombok.Data;

@Data
public class RawTrelloList {

    private String id;
    private String name;
    private String idBoard;
    private boolean closed;
}
