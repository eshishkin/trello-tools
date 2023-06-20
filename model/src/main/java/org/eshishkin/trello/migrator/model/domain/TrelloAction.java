package org.eshishkin.trello.migrator.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.ZonedDateTime;

@Data
@Accessors(chain = true)
public class TrelloAction {
    private String id;
    private ActionType type;
    private ZonedDateTime date;
    private String listName;
    private String boardName;
    private String source;

    private Changes changes;

    private String checklistId;
    private String checklistName;

    private String checklistItemName;
    private String checklistItemState;

    private String createdBy;
    private Author createdByOnBehalf;

    @Data
    @Accessors(chain = true)
    public static class Author {
        private String username;
        private String fullName;
    }

    @Data
    @Accessors(chain = true)
    public static class Changes {
        private String text;

        private String cardNameBefore;
        private String listBeforeId;
        private String listAfterId;
        private ZonedDateTime due;
        private Boolean dueComplete;
        private Boolean cardClosed;
        private ZonedDateTime dueBefore;
        private Boolean dueCompleteBefore;
        private Boolean cardClosedBefore;
        private String attachment;
    }

    public enum ActionType {
        createCard, commentCard, moveCardToBoard, updateCard, addChecklistToCard, removeChecklistFromCard,
        updateCheckItemStateOnCard, addMemberToCard, removeMemberFromCard, addAttachmentToCard, copyCard, deleteCard,
        moveCardFromBoard, unknown;

        public static ActionType parse(String raw) {
            try {
                return ActionType.valueOf(raw);
            } catch (Exception ex) {
                System.out.println("Unknown type " + raw );
                return unknown;
            }
        }
    }
}
