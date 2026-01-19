package helpers.testrail.models;

import lombok.Data;

import java.util.List;

/**
 * Модель для хранения списка вложений тестового прогона.
 */
@Data
public class RunAttachments {
    private List<Attachment> attachments;

    /**
     * Модель данных для отдельного вложения.
     */
    @Data
    public static class Attachment {
        private long id;
        private String name;
        private long size;
        private long created_on;
        private long project_id;
        private long case_id;
        private long user_id;
        private long entity_attachments_id;
        private String icon_name;
        private int result_id;
    }
}
