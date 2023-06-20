package org.eshishkin.trello.migrator.loader.openproject;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class CommentOpenProjectTaskRequest implements Serializable {
    private Comment comment;

    @Data
    @Accessors(chain = true)
    public static class Comment implements Serializable {
        private String raw;
    }

}
