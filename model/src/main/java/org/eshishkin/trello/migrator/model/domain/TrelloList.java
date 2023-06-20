package org.eshishkin.trello.migrator.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TrelloList {
    private String id;
    private String name;
    private String boardId;
}
