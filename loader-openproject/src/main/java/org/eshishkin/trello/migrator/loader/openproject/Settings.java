package org.eshishkin.trello.migrator.loader.openproject;

import lombok.Data;

@Data
public class Settings {
    private String url;
    private String token;
    private String assigneeId;
    private String projectId;
    private String openStatusId;
    private String closedStatusId;

    private String defaultTypeId;
    private String defaultPriorityId;
}
