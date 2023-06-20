import com.julienvey.trello.Trello;
import com.julienvey.trello.domain.Action;
import com.julienvey.trello.domain.Argument;
import com.julienvey.trello.domain.Board;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.CheckList;
import com.julienvey.trello.domain.TList;
import com.julienvey.trello.impl.TrelloImpl;
import com.julienvey.trello.impl.http.ApacheHttpClient;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eshishkin.trello.migrator.utils.JsonUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class Downloader {

    public static String download(String trelloKey, String token, String boardId) {
        var trello = getClient(trelloKey, token);

        var board = trello.getBoard(boardId);
        var lists = trello.getBoardLists(boardId);
        var cards = trello.getBoardCards(boardId);
        var checklists = trello.getBoardChecklists(boardId);
        var actions = getCardActions(cards.stream().map(Card::getId).toList(), trello);

        return JsonUtils.serialize(new Result().setId(board.getId())
                .setName(board.getName())
                .setShortLink(board.getShortLink())
                .setShortUrl(board.getShortUrl())
                .setCards(cards)
                .setActions(actions)
                .setCheckLists(checklists)
                .setLists(lists)
        );
    }

    private static Trello getClient(String trelloKey, String token) {
        RequestConfig.Builder requestBuilder = RequestConfig.custom();
        requestBuilder.setConnectTimeout(60000);
        requestBuilder.setConnectionRequestTimeout(180000);

        return new TrelloImpl(
                trelloKey, token,
                new ApacheHttpClient(HttpClientBuilder.create().setDefaultRequestConfig(requestBuilder.build()).build())
        );
    }

    private static Board getBoard(String board, Trello trello) {
        return trello.getBoard(board);
    }

    private static List<Card> getCardsFromBoard(String board, Trello trello) {
        return trello.getBoardCards(board);
    }

    private static List<CheckList> getChecklistsFromBoards(Set<String> boards, Trello trello) {
        return boards.stream()
                .map(trello::getBoardChecklists)
                .flatMap(Collection::stream)
                .toList();
    }

    private static List<TList> getListsFromBoards(Set<String> boards, Trello trello) {
        return boards.stream()
                .map(trello::getBoardLists)
                .flatMap(Collection::stream)
                .toList();
    }


    private static List<Action> getCardActions(List<String> cards, Trello trello) {
        return cards.stream()
                .flatMap(id -> {
                    waitFor(100);
                    return trello.getCardActions(
                                    id,
                                    new Argument("filter", "addAttachmentToCard,addChecklistToCard,addMemberToCard,commentCard,copyCommentCard,convertToCardFromCheckItem,createCard,copyCard,deleteAttachmentFromCard,emailCard,moveCardFromBoard,moveCardToBoard,removeChecklistFromCard,removeMemberFromCard,updateCard:idList,updateCard:closed,updateCard:due,updateCard:dueComplete,updateCheckItemStateOnCard,updateCustomFieldItem"),
                                    new Argument("page", "0")
                            )
                            .stream();
                })
                .toList();
    }

    @SneakyThrows
    private static void waitFor(int millis) {
        Thread.sleep(millis);
    }

    @Data
    @Accessors(chain = true)
    private static class Result {
        private String id;
        private String name;
        private String desc;
        private String shortUrl;
        private String shortLink;

        private List<TList> lists  = new ArrayList<>();
        private List<Card> cards = new ArrayList<>();
        private List<Action> actions = new ArrayList<>();
        private List<CheckList> checkLists = new ArrayList<>();
    }
}
