package org.kitodo.data.database.beans;

import org.kitodo.data.database.enums.TaskStatus;

import java.util.ArrayList;
import java.util.List;

public class TaskTableDTOMapper {

    public TaskTableDTO mapFromEntity(Task task) {
        TaskTableDTO dto = new TaskTableDTO();

        dto.setId(task.getId());
        dto.setTitle(task.getTitle());

        Process process = task.getProcess();
        if (process != null) {
            dto.setProcessId(process.getId());
            dto.setProcessTitle(process.getTitle());

            Project project = process.getProject();
            dto.setProjectTitle(project != null ? project.getTitle() : null);

            // Ideally you'd fetch this properly too
            dto.setProcessCreationDate("DATE");
        }

        TaskStatus status = task.getProcessingStatus();
        dto.setProcessingStatus(status != null ? status.name() : null);
        dto.setProcessingStatusTitle(status != null ? status.getTitle() : null);

        User user = task.getProcessingUser();
        if (user != null) {
            dto.setProcessingUser(user); // If needed — otherwise remove
            dto.setProcessingUserFullName(user.getFullName());
        }

        dto.setCorrection(task.isCorrection());
        dto.setCorrectionCommentStatus(getCorrectionCommentStatus(task));

        dto.setEditTypeTitle(task.getEditTypeTitle());

        return dto;
    }


    public List<TaskTableDTO> mapFromEntities(List<Task> tasks) {
        List<TaskTableDTO> result = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            result.add(mapFromEntity(task));
        }
        return result;
    }

    /**
     * Dummy method for computing comment status — adjust this if you have actual logic.
     */
    private int getCorrectionCommentStatus(Task task) {
        Process process = task.getProcess();
        if (process != null && process.getComments() != null && !process.getComments().isEmpty()) {
            return 1; // Simplified: return 1 if comments exist
        }
        return 0;
    }
}
