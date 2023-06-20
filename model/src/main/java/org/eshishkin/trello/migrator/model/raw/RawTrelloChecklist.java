package org.eshishkin.trello.migrator.model.raw;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RawTrelloChecklist {

    private String id;
    private String name;
    private int pos;

    private String idCard;
    private String idBoard;

    private List<Item> checkItems = new ArrayList<>();

    @Data
    public static class Item {
        private String id;
        private String name;
        private String state;
        private int pos;
    }

    public enum ItemState {
        complete, incomplete
    }
}
