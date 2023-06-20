package org.eshishkin.trello.migrator.loader.openproject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
public class CreateParentOpenProjectRequest implements Serializable {

    private int lockVersion = 0;
    @JsonProperty("_links")
    private Map<String, Link> links;

    @Data
    @AllArgsConstructor
    public static class Link implements Serializable {
        private String href;
    }

}
