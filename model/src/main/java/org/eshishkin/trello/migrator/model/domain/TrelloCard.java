package org.eshishkin.trello.migrator.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class TrelloCard {
    private String id;
    private String name;
    private String description;
    private String shortLink;
    private String shortUrl;
    private String boardId;
    private String listId;
    private ZonedDateTime created;
    private ZonedDateTime start;
    private ZonedDateTime due;
    private Boolean closed;
    private Boolean dueComplete;

    private List<TrelloChecklist> checklists = new ArrayList<>();
    private List<TrelloAction> actions = new ArrayList<>();

    private List<String> possibleChildren = new ArrayList<>();
}
