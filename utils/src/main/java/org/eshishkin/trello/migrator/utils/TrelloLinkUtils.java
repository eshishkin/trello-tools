package org.eshishkin.trello.migrator.utils;

public class TrelloLinkUtils {

    public static final String TRELLO_LINK = "https://trello.com/c/";

    public static String getTrelloShortUrl(String link) {
        return link
                .substring(TRELLO_LINK.length())
                .substring(0, 8);
    }

    public static boolean hasTrelloLink(String name) {
        return name.contains(TRELLO_LINK);
    }
}
