package org.eshishkin.trello.migrator.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class TrelloBackup {
    private List<TrelloBoard> boards;
    private List<TrelloList> lists;
    private List<TrelloCard> cards;
}
