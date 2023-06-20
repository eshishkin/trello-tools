package org.eshishkin.trello.migrator.model.raw;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class RawTrelloCard {

    private String id;
    private String name;
    private String desc;

    private String shortLink;
    private String shortUrl;
    private String idBoard;
    private String idList;
    private List<String> idChecklists;

    private ZonedDateTime start;
    private ZonedDateTime due;
    private Boolean closed;
    private Boolean dueComplete;
}
