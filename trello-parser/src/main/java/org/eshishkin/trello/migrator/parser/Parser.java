package org.eshishkin.trello.migrator.parser;

import org.eshishkin.trello.migrator.model.domain.TrelloAction;
import org.eshishkin.trello.migrator.model.domain.TrelloBackup;
import org.eshishkin.trello.migrator.model.domain.TrelloBoard;
import org.eshishkin.trello.migrator.model.domain.TrelloCard;
import org.eshishkin.trello.migrator.model.domain.TrelloChecklist;
import org.eshishkin.trello.migrator.model.domain.TrelloList;
import org.eshishkin.trello.migrator.model.raw.RawTrelloAction;
import org.eshishkin.trello.migrator.model.raw.RawTrelloBoard;
import org.eshishkin.trello.migrator.model.raw.RawTrelloCard;
import org.eshishkin.trello.migrator.model.raw.RawTrelloChecklist;
import org.eshishkin.trello.migrator.model.raw.RawTrelloList;
import org.eshishkin.trello.migrator.utils.JsonUtils;
import org.eshishkin.trello.migrator.utils.TrelloLinkUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

public class Parser {

    public static TrelloBackup parse(List<String> raw) {
        var boards = boards(raw);
        Map<String, List<TrelloChecklist>> checklists = new ConcurrentHashMap<>();
        Map<String, List<TrelloAction>> actions = new ConcurrentHashMap<>();

        var cards = boards.stream()
                .flatMap(board -> {

                    groupChecklists(board).forEach((id, data) -> checklists.merge(id, data, (v1, v2) -> {
                        v1.addAll(v2);
                        return v1;
                    }));

                    groupActions(board).forEach((id, data) -> actions.merge(id, data, (v1, v2) -> {
                        v1.addAll(v2);
                        return v1;
                    }));

                    return board.getCards().stream().map(Parser::toDomain);
                })
                .toList()
                .stream()
                .map(card -> {
                    card.setActions(actions.getOrDefault(card.getId(), new ArrayList<>()));
                    card.setChecklists(checklists.getOrDefault(card.getId(), new ArrayList<>()));
                    card.setPossibleChildren(card.getChecklists()
                            .stream()
                            .flatMap(x -> x.getItems().stream())
                            .map(TrelloChecklist.Item::getName)
                            .filter(TrelloLinkUtils::hasTrelloLink)
                            .map(TrelloLinkUtils::getTrelloShortUrl)
                            .toList()
                    );
                    card.setCreated(card.getActions().stream()
                            .map(TrelloAction::getDate)
                            .min(naturalOrder()).orElse(null)
                    );
                    return card;
                })
                .toList();

        return new TrelloBackup()
                .setBoards(boards.stream().map(Parser::toDomain).toList())
                .setLists(boards.stream().flatMap(x -> x.getLists().stream()).map(Parser::toDomain).toList())
                .setCards(cards);
    }

    private static List<RawTrelloBoard> boards(List<String> boards) {
        return Stream.ofNullable(boards)
                .flatMap(Collection::stream)
                .map(data -> JsonUtils.parse(data, RawTrelloBoard.class))
                .toList();
    }

    private static Map<String, List<TrelloChecklist>> groupChecklists(RawTrelloBoard board) {
        return board.getCheckLists().stream().collect(groupingBy(
                RawTrelloChecklist::getIdCard, mapping(Parser::toDomain, toList())
        ));
    }

    private static Map<String, List<TrelloAction>> groupActions(RawTrelloBoard board) {
        return board.getActions().stream()
                .filter(action -> action.getData() != null)
                .filter(action -> action.getData().getCard() != null)
                .filter(action -> action.getData().getCard().getId() != null)
                .collect(groupingBy(
                        action -> action.getData().getCard().getId(),
                        mapping(Parser::toDomain, toList())
                ));
    }


    private static TrelloBoard toDomain(RawTrelloBoard raw) {
        return new TrelloBoard().setId(raw.getId()).setName(raw.getName());
    }

    private static TrelloList toDomain(RawTrelloList raw) {
        return new TrelloList().setId(raw.getId()).setName(raw.getName()).setBoardId(raw.getIdBoard());
    }

    private static TrelloChecklist toDomain(RawTrelloChecklist raw) {
        return new TrelloChecklist()
                .setId(raw.getId())
                .setName(raw.getName())
                .setPos(raw.getPos())
                .setItems(raw.getCheckItems().stream()
                        .map(item -> new TrelloChecklist.Item()
                                .setId(item.getId())
                                .setName(item.getName())
                                .setPos(item.getPos())
                                .setState(TrelloChecklist.ItemState.valueOf(item.getState()))
                        )
                        .toList()
                );
    }

    private static TrelloAction toDomain(RawTrelloAction raw) {
        return new TrelloAction()
                .setId(raw.getId())
                .setType(TrelloAction.ActionType.parse(raw.getType()))
                .setDate(raw.getDate())
                .setListName(raw.getData().getList() != null ? raw.getData().getList().getName() : null)
                .setBoardName(raw.getData().getBoard() != null ? raw.getData().getBoard().getName() : null)
                .setSource(raw.getData().getBoardSource() != null ? raw.getData().getBoardSource().getId() : null)
                .setChecklistId(raw.getData().getChecklist() != null ? raw.getData().getChecklist().getId() : null)
                .setChecklistName(raw.getData().getChecklist() != null ? raw.getData().getChecklist().getName() : null)
                .setChecklistItemName(raw.getData().getCheckItem() != null ? raw.getData().getCheckItem().getName() : null)
                .setChecklistItemState(raw.getData().getCheckItem() != null ? raw.getData().getCheckItem().getState() : null)
                .setCreatedBy(raw.getAppCreator() != null ? raw.getAppCreator().getName() : null)
                .setCreatedByOnBehalf(new TrelloAction.Author()
                        .setFullName(raw.getMemberCreator().getFullName())
                        .setUsername(raw.getMemberCreator().getUsername())
                )
                .setChanges(new TrelloAction.Changes()
                        .setText(raw.getData().getText())
                        .setCardNameBefore(raw.getData().getCard().getName())
                        .setDueBefore(raw.getData().getOld() != null ? raw.getData().getOld().getDue() : null)
                        .setDue(raw.getData().getCard().getDue())
                        .setDueComplete(raw.getData().getCard().getDueComplete())
                        .setDueCompleteBefore(raw.getData().getOld() != null ? raw.getData().getOld().getDueComplete() : null)
                        .setCardClosed(raw.getData().getCard().getClosed())
                        .setCardClosedBefore(raw.getData().getOld() != null ? raw.getData().getOld().getClosed() : null)
                        .setListAfterId(raw.getData().getListAfter() != null ? raw.getData().getListAfter().getId() : null)
                        .setListBeforeId(raw.getData().getListBefore() != null ? raw.getData().getListBefore().getId() : null)
                        .setAttachment(raw.getData().getAttachment() != null ? raw.getData().getAttachment().getUrl() : null)
                );
    }

    private static TrelloCard toDomain(RawTrelloCard raw) {
        return new TrelloCard()
                .setId(raw.getId())
                .setName(raw.getName())
                .setDescription(raw.getDesc())
                .setClosed(raw.getClosed())
                .setBoardId(raw.getIdBoard())
                .setDue(raw.getDue())
                .setDueComplete(raw.getDueComplete())
                .setListId(raw.getIdList())
                .setShortLink(raw.getShortLink())
                .setShortUrl(raw.getShortUrl())
                .setStart(raw.getStart());
    }

}
