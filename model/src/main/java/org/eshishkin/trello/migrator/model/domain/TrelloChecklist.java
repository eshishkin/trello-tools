package org.eshishkin.trello.migrator.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class TrelloChecklist {
    private String id;
    private int pos;
    private String name;
    private List<Item> items;

    @Data
    @Accessors(chain = true)
    public static class Item {
        private String id;
        private String name;
        private ItemState state;
        private int pos;
    }

    public enum ItemState {
        complete, incomplete
    }
}
