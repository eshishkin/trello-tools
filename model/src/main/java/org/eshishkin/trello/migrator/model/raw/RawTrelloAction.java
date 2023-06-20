package org.eshishkin.trello.migrator.model.raw;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class RawTrelloAction {
    private String id;
    private ZonedDateTime date;

    private Content data;
    private MemberCreator memberCreator;
    private AppCreator appCreator;
    private String type;

    @Data
    public static class MemberCreator {
        private String username;
        private String fullName;
    }

    @Data
    public static class AppCreator {
        private String name;
    }

    @Data
    public static class Content {
        private String name;
        private String text;

        private List listAfter;
        private List listBefore;
        private Old old;
        private Card card;
        private Checklist checklist;
        private CheckItem checkItem;
        private List list;
        private Board board;
        private Member member;
        private Attachment attachment;
        private CardSource cardSource;
        private BoardSource boardSource;
    }

    @Data
    public static class Card {
        private String id;
        private String name;
        private ZonedDateTime due;
        private Boolean dueComplete;
        private Boolean closed;
    }

    @Data
    public static class Checklist {
        private String id;
        private String name;
        private ZonedDateTime due;
    }

    @Data
    public static class CheckItem {
        private String name;
        private String state;
    }

    @Data
    public static class List {
        private String id;
        private String name;
        private Boolean dueComplete;
        private Boolean closed;
    }

    @Data
    public static class Board {
        private String id;
        private String name;
    }

    @Data
    public static class Old {
        private Boolean closed;
        private Boolean dueComplete;
        private ZonedDateTime due;
    }

    @Data
    public static class Member {
        private String fullName;
    }

    @Data
    public static class Attachment {
        private String url;
    }

    @Data
    public static class CardSource {
        private String id;
    }

    @Data
    public static class BoardSource {
        private String id;
    }
}
