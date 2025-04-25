package org.kitodo.data.database.beans;

import org.apache.commons.lang3.tuple.Pair;
import org.kitodo.data.database.enums.CorrectionComments;
import org.kitodo.data.database.enums.TaskStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TaskTableDTOMapper {

    public TaskTableDTO mapFromEntity(Task task,
                                      Map<Integer, Pair<String, String>> processAndProjectTitlesByTaskIds,
                                      Map<Integer, Integer> correctionStatusByTaskId,
                                      Map<Integer, Pair<Integer, String>> processingUsersByTaskIds,
                                      Map<Integer, String> correctionCommentsByTaskIds) {
        TaskTableDTO dto = new TaskTableDTO();

        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setProcessingBegin(task.getProcessingBegin());
        dto.setProcessingEnd(task.getProcessingEnd());
        dto.setProcessingTime(task.getProcessingTime());
        dto.setBatchStep(task.isBatchStep());
        // Process & Project Titles
        Pair<String, String> titles = processAndProjectTitlesByTaskIds.get(task.getId());
        if (titles != null) {
            dto.setProcessTitle(titles.getLeft());
            dto.setProjectTitle(titles.getRight());
        }

        // Status Info
        TaskStatus status = task.getProcessingStatus();
        dto.setProcessingStatus(status != null ? status.name() : null);
        dto.setProcessingStatusTitle(status != null ? status.getTitle() : null);

        // User Info from preloaded map
        Pair<Integer, String> userInfo = processingUsersByTaskIds.get(task.getId());
        if (userInfo != null) {
            dto.setProcessingUserId(userInfo.getLeft());
            dto.setProcessingUserFullName(userInfo.getRight());
        }
        String comment = correctionCommentsByTaskIds.get(task.getId());
        dto.setCorrectionComment(comment);

        // Flags
        dto.setIsCorrection(task.isCorrection());

        // Correction Comment Status
        int correctionStatus = correctionStatusByTaskId.getOrDefault(
                task.getId(), CorrectionComments.NO_CORRECTION_COMMENTS.getValue());
        dto.setCorrectionCommentStatus(correctionStatus);

        dto.setEditTypeTitle(task.getEditTypeTitle());

        return dto;
    }

    public List<TaskTableDTO> mapFromEntities(List<Task> tasks,
                                              Map<Integer, Pair<String, String>> processAndProjectTitlesByTaskIds,
                                              Map<Integer, Integer> correctionStatusByTaskId,
                                              Map<Integer, Pair<Integer, String>> processingUsersByTaskIds,
                                              Map<Integer, String> correctionCommentsByTaskIds) {
        List<TaskTableDTO> result = new ArrayList<>(tasks.size());
        for (Task task : tasks) {
            result.add(mapFromEntity(task, processAndProjectTitlesByTaskIds,
                    correctionStatusByTaskId, processingUsersByTaskIds, correctionCommentsByTaskIds));
        }
        return result;
    }
}
