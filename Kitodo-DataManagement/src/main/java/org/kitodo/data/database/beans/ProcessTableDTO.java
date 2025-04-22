package org.kitodo.data.database.beans;

import org.kitodo.data.database.enums.TaskStatus;

import java.util.List;

public class ProcessTableDTO {
    private int id;
    private String title;
    private String projectTitle;
    private String lastEditingUser;
    private String processingBeginLastTask;
    private String processingEndLastTask;
    private boolean hasChildren;
    private boolean hasComments;
    private int numberOfChildren;
    private Integer correctionCommentStatus;
    private String progressCombined;
    private List<String> commentMessages; // Tooltip text
    private String lastComment;
    private String currentTaskTitles;
    private boolean hasTasks;
    private List<CurrentTaskInfo> tasks;
    private List<ParentProcessInfo> parentProcesses;
    private boolean canCreateChildProcess;
    private boolean canBeExported;
    private Double progressClosed;
    private Double progressInProcessing;
    private Double progressOpen;
    private boolean canCreateProcessWithCalendar;
    private int templateId;
    private int projectId;
    private List<CommentDTO> comments;

    public List<CommentDTO> getComments() {
        return comments;
    }

    public void setComments(List<CommentDTO> comments) {
        this.comments = comments;
    }

    public boolean getCanBeExported() {
        return canBeExported;
    }

    public void setCanBeExported(boolean canBeExported) {
        this.canBeExported = canBeExported;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public int getProjectId() {
        return projectId;
    }

    public boolean getHasTasks() {
        return hasTasks;
    }

    public void setHasTasks(boolean hasTasks) {
        this.hasTasks = hasTasks;
    }

    public String getLastComment() {
        return lastComment;
    }

    public void setLastComment(String lastComment) {
        this.lastComment = lastComment;
    }

    public void setProjectId(int templateId) {
        this.projectId = templateId;
    }

    public Double getProgressInProcessing() {
        return progressInProcessing;
    }

    public boolean getCanCreateChildProcess() {
        return canCreateChildProcess;
    }

    public void setCanCreateChildProcess(boolean canCreateChildProcess) {
        this.canCreateChildProcess = canCreateChildProcess;
    }

    public void setCanCreateProcessWithCalendar(boolean canCreateProcessWithCalendar) {
        this.canCreateProcessWithCalendar = canCreateProcessWithCalendar;
    }

    public boolean getCanCreateProcessWithCalendar() {
        return canCreateProcessWithCalendar;
    }


    public void setProgressInProcessing(Double progressInProcessing) {
        this.progressInProcessing = progressInProcessing;
    }

    public Double getProgressOpen() {
        return progressOpen;
    }

    public String getCurrentTaskTitles() {
        return currentTaskTitles;
    }

    public void setCurrentTaskTitles(String currentTaskTitles) {
        this.currentTaskTitles = currentTaskTitles;
    }


    public void setProgressOpen(Double progressOpen) {
        this.progressOpen = progressOpen;
    }

    public Double getProgressClosed() {
        return progressClosed;
    }

    public void setProgressClosed(Double progressClosed) {
        this.progressClosed = progressClosed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getLastEditingUser() {
        return lastEditingUser;
    }

    public void setLastEditingUser(String lastEditingUser) {
        this.lastEditingUser = lastEditingUser;
    }

    public boolean getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public int getNumberOfChildren(){
        return numberOfChildren;
    }

    public void setNumberOfChildren(int numberOfChildren){
        this.numberOfChildren = numberOfChildren;
    }

    public boolean getHasComments() {
        return hasComments;
    }

    public void setHasComments(boolean hasComments) {
        this.hasComments = hasComments;
    }

    public int getParentID(){
        return 0;
    }

    public Integer getCorrectionCommentStatus() {
        return correctionCommentStatus;
    }

    public void setCorrectionCommentStatus(Integer correctionCommentStatus) {
        this.correctionCommentStatus = correctionCommentStatus;
    }

    public List<String> getCommentMessages() {
        return commentMessages;
    }

    public List<ParentProcessInfo> getParentProcesses() {
        return parentProcesses;
    }

    public void setParentProcesses(List<ParentProcessInfo> parentProcessInfos) {
        this.parentProcesses = parentProcessInfos;
    }

    public List<CurrentTaskInfo> getTasks() {
        return tasks;
    }

    public void setTasks(List<CurrentTaskInfo> currentTasks) {
        this.tasks = currentTasks;
    }

    public void setCommentMessages(List<String> commentMessages) {
        this.commentMessages = commentMessages;
    }

    public String getProgressCombined() {
        return progressCombined;
    }

    public void setProgressCombined(String progressCombined) {
        this.progressCombined = progressCombined;
    }

    public static class ParentProcessInfo {
        private int id;
        private String title;
        private boolean inAssignedProject;
        private boolean locked;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public boolean getInAssignedProject() {
            return inAssignedProject;
        }

        public void setInAssignedProject(boolean inAssignedProject) {
            this.inAssignedProject = inAssignedProject;
        }

        public boolean getLocked() {
            return locked;
        }

        public void setLocked(boolean locked) {
            this.locked = locked;
        }
    }




    public static class CurrentTaskInfo {
        private int id;
        private String title;
        private String status;
        private boolean batchStep;
        private boolean batchAvailable;
        private int processingUserId;
        private String processingUserFullName;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public boolean isBatchStep() {
            return batchStep;
        }

        public void setBatchStep(boolean batchStep) {
            this.batchStep = batchStep;
        }

        public boolean isBatchAvailable() {
            return batchAvailable;
        }

        public void setBatchAvailable(boolean batchAvailable) {
            this.batchAvailable = batchAvailable;
        }

        public int getProcessingUserId() {
            return processingUserId;
        }

        public void setProcessingUserId(int processingUserId) {
            this.processingUserId = processingUserId;
        }

        public String getProcessingUserFullName() {
            return processingUserFullName;
        }

        public void setProcessingUserFullName(String processingUserFullName) {
            this.processingUserFullName = processingUserFullName;
        }
    }

    public static class CommentDTO {
        private String message;
        private String authorFullName;
        private String creationDate; // Could use LocalDateTime or Date too
        private boolean corrected;
        private String type; // e.g., "ERROR", "INFO"

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getAuthorFullName() {
            return authorFullName;
        }

        public void setAuthorFullName(String authorFullName) {
            this.authorFullName = authorFullName;
        }

        public String getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(String creationDate) {
            this.creationDate = creationDate;
        }

        public boolean isCorrected() {
            return corrected;
        }

        public void setCorrected(boolean corrected) {
            this.corrected = corrected;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }




}


