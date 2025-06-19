/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.data.database.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.dtos.ProcessTableDTO;
import org.kitodo.data.database.dtos.TaskRowDTO;
import org.kitodo.data.database.enums.TaskStatus;

public class ProcessTableDTOConverter {

    private static final Map<Integer, Collection<String>> RULESET_CACHE_FOR_CREATE_CHILD_FROM_PARENT = new HashMap<>();

    public ProcessTableDTO mapFromEntity(Process process, Map<Integer, Boolean> canCreateChildProcessesMap,
                                         Map<Integer, Boolean> inAssignedProjectMap,
                                         Map<Integer, List<Process>> parentProcessMap, Map<Integer, Map<TaskStatus, Double>> progressMap,
                                         Map<Integer, String> lastEditingUserMap, Map<Integer, Boolean> exportableStatus,
                                         Map<Integer, List<Comment>> commentsMap, Map<Integer, List<TaskRowDTO>> processTasksMap,
                                         Map<Integer, Integer> childrenNumberMap) {

        ProcessTableDTO dto = new ProcessTableDTO();
        dto.setId(process.getId());
        dto.setTitle(process.getTitle());
        dto.setLastEditingUser(
                lastEditingUserMap.getOrDefault(process.getId(), null)
        );

        dto.setHasChildren(childrenNumberMap.getOrDefault(process.getId(), 0) > 0);
        dto.setNumberOfChildren(childrenNumberMap.getOrDefault(process.getId(), 0));
        dto.setCreateChildUrl(
                "processCreate.xhtml?templateId=" + dto.getTemplateId()
                        + "&projectId="  + dto.getProjectId()
                        + "&parentId="   + dto.getId());
        //dto.setHasTasks(!process.getTasks().isEmpty());
        dto.setHasTasks(true);
        dto.setTemplateId(process.getTemplate().getId());
        dto.setProjectId(process.getProject().getId());
        dto.setCanBeExported(exportableStatus.getOrDefault(process.getId(), false));
        dto.setCanCreateChildProcess(canCreateChildProcessesMap.get(process.getId()));

        List<Comment> commentList = commentsMap.getOrDefault(process.getId(), Collections.emptyList());
        String lastComment = "";
        if (!commentList.isEmpty()) {
            Comment last = commentList.get(commentList.size() - 1);
            lastComment = last.getMessage();
        }

        int correctionStatus = 0;
        List<ProcessTableDTO.CommentDTO> commentDTOs = new ArrayList<>();

        for (Comment comment : commentList) {
            ProcessTableDTO.CommentDTO dtoComment = new ProcessTableDTO.CommentDTO();
            dtoComment.setMessage(comment.getMessage());
            dtoComment.setAuthorFullName(comment.getAuthor() != null ? comment.getAuthor().getFullName() : "-");
            dtoComment.setCreationDate(comment.getCreationDate().toString()); // Or format as needed
            dtoComment.setCorrected(comment.isCorrected());
            dtoComment.setType("ERROR"); // If you ever support other types, adjust here

            commentDTOs.add(dtoComment);

            // Determine correction status
            if ("ERROR".equals(dtoComment.getType())) {
                if (dtoComment.isCorrected()) {
                    correctionStatus = Math.max(correctionStatus, 1);
                } else {
                    correctionStatus = 2;
                    break;
                }
            }
        }
        dto.setCorrectionCommentStatus(correctionStatus);

        dto.setComments(commentDTOs);
        dto.setLastComment(lastComment);
        dto.setHasComments(!commentDTOs.isEmpty());
        dto.setProjectTitle(process.getProject().getTitle());


        Map<TaskStatus, Double> progress = progressMap.getOrDefault(process.getId(), Collections.emptyMap());
        dto.setProgressClosed(progress.getOrDefault(TaskStatus.DONE, 0.0));
        dto.setProgressOpen(progress.getOrDefault(TaskStatus.OPEN, 0.0));
        dto.setProgressInProcessing(progress.getOrDefault(TaskStatus.INWORK, 0.0));
        dto.setProgressCombined(
                ProcessConverter.getCombinedProgressFromTaskPercentages(progress)
        );
        dto.setCurrentTaskTitles(createProgressTooltip(process));
        List<ProcessTableDTO.CurrentTaskInfo> taskInfoList = new ArrayList<>();
        List<TaskRowDTO> tasks = processTasksMap.getOrDefault(process.getId(), Collections.emptyList());

        for (TaskRowDTO task : tasks) {
            ProcessTableDTO.CurrentTaskInfo info = new ProcessTableDTO.CurrentTaskInfo();
            info.setId(task.getTaskId());
            info.setTitle(task.getTaskTitle());
            info.setStatus(task.getTaskStatus().getTitle());
            //info.setBatchStep(task.getBatchStep());
            if (task.getUserId() != null) {
                info.setProcessingUserId(task.getUserId());
                info.setProcessingUserFullName(task.getUserFullName());
            }
            taskInfoList.add(info);
        }
        // grab the parent list safely (may be empty)
        List<ProcessTableDTO.ParentProcessInfo> parentProcessInfos = new ArrayList<>();
        List<Process> parents =
                parentProcessMap.getOrDefault(process.getId(), Collections.emptyList());

        for (Process parent : parents) {
            ProcessTableDTO.ParentProcessInfo info = new ProcessTableDTO.ParentProcessInfo();
            info.setId(parent.getId());
            info.setTitle(parent.getTitle());
            info.setInAssignedProject(
                    inAssignedProjectMap.getOrDefault(parent.getId(), false));

            parentProcessInfos.add(info);
        }
        dto.setParentProcesses(parentProcessInfos);
        dto.setTasks(taskInfoList);
        return dto;
    }

    public List<ProcessTableDTO> mapFromEntities(List<Process> processes, Map<Integer, Boolean> canCreateChildProcessesMap,
                                                 Map<Integer, Boolean> setInAssignedProject,
                                                 Map<Integer, List<Process>> parentProcesses, Map<Integer, Map<TaskStatus, Double>> progressMap,
                                                 Map<Integer, String> lastEditingUserMap, Map<Integer, Boolean> exportableStatus,
                                                 Map<Integer, List<Comment>> commentsMap, Map<Integer, List<TaskRowDTO>> processTasksMap,
                                                 Map<Integer, Integer> childrenNumberMap) {
        List<ProcessTableDTO> result = new ArrayList<>(processes.size());
        for (Process process : processes) {
            result.add(mapFromEntity(process, canCreateChildProcessesMap,setInAssignedProject, parentProcesses, progressMap,
                    lastEditingUserMap, exportableStatus, commentsMap, processTasksMap, childrenNumberMap));
        }
        return result;
    }

    /**
     * Create and return String used as progress tooltip for a given process. Tooltip contains OPEN tasks and tasks
     * INWORK.
     *
     * @param process
     *          process for which the tooltop is created
     * @return String containing the progress tooltip for the given process
     */
    public String createProgressTooltip(Process process) {
        return "TODO Tooltip";
    }
        /*String openTasks = getOpenTasks(process).stream()
                .map(t -> " - " + Helper.getTranslation(t.getTitle())).collect(Collectors.joining(NEW_LINE_ENTITY));
        if (!openTasks.isEmpty()) {
            openTasks = Helper.getTranslation(TaskStatus.OPEN.getTitle()) + ":" + NEW_LINE_ENTITY + openTasks;
        }
        String tasksInWork = getTasksInWork(process).stream()
                .map(t -> " - " + Helper.getTranslation(t.getTitle())).collect(Collectors.joining(NEW_LINE_ENTITY));
        if (!tasksInWork.isEmpty()) {
            tasksInWork = Helper.getTranslation(TaskStatus.INWORK.getTitle()) + ":" + NEW_LINE_ENTITY + tasksInWork;
        }
        if (openTasks.isEmpty() && tasksInWork.isEmpty()) {
            return "";
        } else if (openTasks.isEmpty()) {
            return tasksInWork;
        } else if (tasksInWork.isEmpty()) {
            return openTasks;
        } else {
            return openTasks + NEW_LINE_ENTITY + tasksInWork;
        }*/
    }
