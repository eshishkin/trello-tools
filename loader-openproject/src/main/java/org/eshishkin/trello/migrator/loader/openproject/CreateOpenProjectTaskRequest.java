package org.eshishkin.trello.migrator.loader.openproject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
public class CreateOpenProjectTaskRequest implements Serializable {

    @JsonProperty("_type")
    private String type = "WorkPackage";
    private String subject;
    private String startDate;
    private String dueDate;
    private boolean readonly = false;
    private boolean scheduleManually = false;
    private Description description;
    @JsonProperty("customField1")
    private String trelloLink;

    @JsonProperty("customField2")
    private String trelloList;

    @JsonProperty("_links")
    private Map<String, Link> links;

    @Data
    @Accessors(chain = true)
    public static class Description implements Serializable {
        private String format = "markdown";
        private String raw;
    }

    @Data
    @AllArgsConstructor
    public static class Link implements Serializable {
        private String href;
    }
}
