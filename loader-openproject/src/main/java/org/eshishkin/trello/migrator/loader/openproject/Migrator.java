package org.eshishkin.trello.migrator.loader.openproject;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.eshishkin.trello.migrator.model.domain.TrelloAction;
import org.eshishkin.trello.migrator.model.domain.TrelloBackup;
import org.eshishkin.trello.migrator.model.domain.TrelloBoard;
import org.eshishkin.trello.migrator.model.domain.TrelloCard;
import org.eshishkin.trello.migrator.model.domain.TrelloChecklist;
import org.eshishkin.trello.migrator.model.domain.TrelloChecklist.Item;
import org.eshishkin.trello.migrator.model.domain.TrelloList;
import org.eshishkin.trello.migrator.utils.JsonUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.eshishkin.trello.migrator.utils.TrelloLinkUtils.hasTrelloLink;

@RequiredArgsConstructor
public class Migrator {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final TrelloBackup context;
    private final Settings settings;

    public void migrate() {
        var packageByLink = new HashMap<String, String>();

        var total = context.getCards().size();
        var processed = 0;

        for (var trelloCard : context.getCards()) {
            var packageId = createOpenProjectTask(trelloCard);
            packageByLink.put(trelloCard.getShortLink(), packageId);
            trelloCard.getActions().forEach(a -> createOpenProjectComment(packageId, a, trelloCard));
            processed++;
            System.out.println("Processed cards " + processed + " of " + total);
        }

        context.getCards().forEach(card -> {
            var parentPackageId = packageByLink.get(card.getShortLink());
            card.getPossibleChildren().forEach(id -> {
                createOpenProjectRelationship(packageByLink.get(id), parentPackageId);
            });
        });
    }

    @SneakyThrows
    private String createOpenProjectTask(TrelloCard card) {
        var closed = card.getDueComplete();
        var status = Boolean.TRUE.equals(closed) ? settings.getClosedStatusId() : settings.getOpenStatusId();

        var request = new CreateOpenProjectTaskRequest()
                .setSubject(card.getName())
                .setType(settings.getDefaultTypeId())
                .setStartDate(card.getStart() != null ? card.getStart().toLocalDate().toString() : null)
                .setDueDate(card.getDue() != null ? card.getDue().toLocalDate().toString() : null)
                .setLinks(Map.of(
                        "assignee", new CreateOpenProjectTaskRequest.Link("/api/v3/users/" + settings.getAssigneeId()),
                        "priority", new CreateOpenProjectTaskRequest.Link("/api/v3/priorities/" + settings.getDefaultPriorityId()),
                        "project", new CreateOpenProjectTaskRequest.Link("/api/v3/projects/" + settings.getProjectId()),
                        "type", new CreateOpenProjectTaskRequest.Link("/api/v3/types/" + settings.getDefaultTypeId()),
                        "status", new CreateOpenProjectTaskRequest.Link("/api/v3/statuses/" +  status)
                ))
                .setTrelloLink(card.getShortUrl())
                .setTrelloList(getBoardName(card.getBoardId()) + " - " + getListName(card.getListId()))
                .setDescription(new CreateOpenProjectTaskRequest.Description().setRaw(issueDescription(card)));


        var response = HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .uri(URI.create(settings.getUrl() + "/work_packages"))
                        .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.serialize(request)))
                        .header("Content-Type", "application/json")
                        .header("Authorization", auth(settings))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 201) {
            System.out.println(response.body());
            throw new RuntimeException("Unable to create a task:" + response.body());
        }

        return JsonUtils.parse(response.body(), JsonNode.class).get("id").asText();
    }

    @SneakyThrows
    private void createOpenProjectComment(String packageId, TrelloAction action, TrelloCard card) {
        var request = new CommentOpenProjectTaskRequest()
                .setComment(new CommentOpenProjectTaskRequest.Comment().setRaw(
                        comment(action, card, context)
                ));


        var response = HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .uri(URI.create(String.format("%s/work_packages/%s/activities", settings.getUrl(), packageId)))
                        .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.serialize(request)))
                        .header("Content-Type", "application/json")
                        .header("Authorization", auth(settings))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 201) {
            System.out.println(response.body());
            throw new RuntimeException("Unable to create a comment:" + response.body());
        }
    }

    @SneakyThrows
    private void createOpenProjectRelationship(String packageId, String parentPackageId) {
        var request = new CreateParentOpenProjectRequest()
                .setLockVersion(getLockVersion(packageId))
                .setLinks(Map.of(
                        "parent", new CreateParentOpenProjectRequest.Link("/api/v3/work_packages/" + parentPackageId)
                ));


        var response = HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .uri(URI.create(String.format("%s/work_packages/%s", settings.getUrl(), packageId)))
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(JsonUtils.serialize(request)))
                        .header("Content-Type", "application/json")
                        .header("Authorization", auth(settings))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            System.out.println(response.body());
            throw new RuntimeException("Unable to create relationship:" + response.body());
        }
    }

    @SneakyThrows
    private int getLockVersion(String packageId) {
        var response = HTTP_CLIENT.send(HttpRequest.newBuilder()
                        .uri(URI.create(String.format("%s/work_packages/%s", settings.getUrl(), packageId)))
                        .GET()
                        .header("Authorization", auth(settings))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            System.out.println(response.body());
            throw new RuntimeException(String.format("Unable to get work package %s: %s", packageId, response.body()));
        }

        return JsonUtils.parse(response.body(), JsonNode.class).get("lockVersion").asInt();
    }

    private String auth(Settings settings) {
        return "Basic " + Base64.getEncoder().encodeToString(String.format("apikey:%s", settings.getToken()).getBytes(UTF_8));
    }

    private String issueDescription(TrelloCard card) {
        var closed = card.getDueComplete();

        var createdAt = card.getActions()
                .stream()
                .map(TrelloAction::getDate)
                .min(Comparator.naturalOrder())
                .orElse(null);

        var updatedAt = card.getActions()
                .stream()
                .map(TrelloAction::getDate)
                .max(Comparator.naturalOrder())
                .orElse(null);

        var description = new StringBuilder()
                .append("[")
                .append(closed ? "Y" : "N").append(",")
                .append(createdAt != null ? FORMATTER.format(createdAt) : "NULL").append(",")
                .append(updatedAt != null ? FORMATTER.format(updatedAt) : "NULL")
                .append("]")
                .append("\n")
                .append(card.getDescription())
                .append("\n");

        if (card.getChecklists().size() > 0) {
            description.append("___");
        }

        card.getChecklists().stream().sorted(comparing(TrelloChecklist::getPos)).forEach(checklist -> {
            description.append("\n").append("**").append(checklist.getName().trim()).append("**").append(":").append("\n");
            checklist.getItems().stream().sorted(comparing(Item::getPos)).forEach(item -> {
                var name = item.getName();
                var link = hasTrelloLink(name);
                var resolved = link
                        ? String.format("[%s](%s)", getCardNameByShortLink(name), name)
                        : name;
                description
                        .append(item.getState() == TrelloChecklist.ItemState.complete  ? "- [X]" : "- [ ]")
                        .append(" ")
                        .append(resolved.trim())
                        .append("\n");
            });

        });

        return description.toString();
    }

    private String comment(TrelloAction action, TrelloCard card, TrelloBackup context) {
        var creator  = action.getCreatedByOnBehalf().getFullName() + " (@eshishkin)";
        var data = action.getChanges();
        var note = String.format("[%s] - \n", FORMATTER.format(action.getDate()));

        switch (action.getType()) {
            case createCard -> note = note + String.format("%s added this card (\"%s\") to **%s**",
                    creator,
                    data.getCardNameBefore(),
                    action.getListName()
            );
            case commentCard -> note = note + data.getText();
            case moveCardToBoard -> note = note + String.format("%s transferred this card from **%s**",
                    creator, action.getBoardName()
            );
            case updateCard -> {
                var moved = !Objects.equals(data.getListAfterId(), data.getListBeforeId());
                var dueChanged = !Objects.equals(data.getDue(), data.getDueBefore());
                if (moved) {
                    note = note + String.format("%s moved this card from **%s** to **%s**",
                            creator, getListName(data.getListBeforeId()), getListName(data.getListAfterId())
                    );
                }
                if (dueChanged) {
                    note = note + String.format("%s changed due date for this card from **%s** to **%s**",
                            creator, data.getDueBefore(), data.getDue()
                    );
                }
                if (Boolean.TRUE.equals(data.getDueComplete()) && !Boolean.TRUE.equals(data.getDueCompleteBefore())) {
                    note = note + String.format("%s marked the due date complete", creator);
                }
                if (Boolean.TRUE.equals(data.getCardClosed()) && !Boolean.TRUE.equals(data.getCardClosedBefore())) {
                    note = note + String.format("%s archived this card", creator);
                }
                if (!Boolean.TRUE.equals(data.getCardClosed()) && Boolean.TRUE.equals(data.getCardClosedBefore())) {
                    note = note + String.format("%s returned this card from archive", creator);
                }
            }

            case addChecklistToCard -> {
                var checklist = getChecklist(card.getId(), action.getChecklistId())
                        .map(TrelloChecklist::getName)
                        .orElse(action.getChecklistName());
                note = note + String.format("%s added checklist **%s** on this card",
                        creator, checklist
                );
            }
            case removeChecklistFromCard -> note = note + String.format("%s removed checklist **%s** from this card",
                    creator, action.getChecklistName()
            );
            case updateCheckItemStateOnCard -> {
                var completed = Objects.equals("complete", action.getChecklistItemState());
                var name = action.getChecklistItemName();
                var link = hasTrelloLink(name);
                var resolved = link
                        ? String.format("[%s](%s)", getCardNameByShortLink(name), name)
                        : name;
                note = note + String.format("%s %s **%s** on this card",
                        creator, completed ? "completed" : "unchecked",
                        resolved
                );
            }
            case addMemberToCard -> note = note + String.format("%s joined this card", action.getCreatedByOnBehalf().getFullName());
            case removeMemberFromCard -> note = note + String.format("%s left this card", action.getCreatedByOnBehalf().getFullName());
            case addAttachmentToCard -> note = note + String.format("%s attached to this card - %s",
                    creator, data.getAttachment()
            );
            case copyCard -> note = note + String.format("%s copied this card from **\"%s\"** in list **\"%s\"**",
                    creator, action.getSource(), action.getListName()
            );
        }

        return note;
    }

    private String getCardNameByShortLink(String link) {
        return context.getCards()
                .stream()
                .filter(c -> Objects.equals(c.getShortUrl(), link))
                .findFirst()
                .map(TrelloCard::getName)
                .orElse(link);
    }

    private String getListName(String id) {
        return context.getLists()
                .stream()
                .filter(c -> Objects.equals(c.getId(), id))
                .findFirst()
                .map(TrelloList::getName)
                .orElse(id);
    }

    private String getBoardName(String id) {
        return context.getBoards()
                .stream()
                .filter(c -> Objects.equals(c.getId(), id))
                .findFirst()
                .map(TrelloBoard::getName)
                .orElse(id);
    }

    private Optional<TrelloChecklist> getChecklist(String cardId, String checklistId) {
        return context.getCards()
                .stream()
                .filter(c -> Objects.equals(c.getId(), cardId))
                .flatMap(x -> x.getChecklists().stream())
                .filter(c -> Objects.equals(c.getId(), checklistId))
                .findFirst();
    }

}
