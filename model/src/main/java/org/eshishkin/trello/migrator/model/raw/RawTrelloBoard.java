package org.eshishkin.trello.migrator.model.raw;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RawTrelloBoard {
    private String id;
    private String name;
    private String shortLink;
    private String shortUrl;

    private List<RawTrelloCard> cards = new ArrayList<>();
    private List<RawTrelloList> lists = new ArrayList<>();
    private List<RawTrelloAction> actions = new ArrayList<>();
    private List<RawTrelloChecklist> checkLists = new ArrayList<>();
}
