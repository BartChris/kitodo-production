package org.kitodo.data.database.beans;

import org.kitodo.data.database.enums.TaskStatus;

public class TaskTableDTO {
    private Integer id;

    private String title;

    private String processTitle;
    private Integer processId;
    private String projectTitle;

    private String processCreationDate;
    private String processingStatusTitle;

    private User processingUser;
    private String processingUserFullName;

    //private Date processingBegin;
    //private Date processingEnd;

    private boolean correction;
    private int correctionCommentStatus;

    private String editTypeTitle;

    private boolean batchStep;
    private boolean batchAvailable;

    private String processingStatus;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProcessCreationDate(String processCreationDate) {
        this.processCreationDate = processCreationDate;
    }

    public String getProcessCreationDate() {
        return processCreationDate;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getProcessTitle() {
        return processTitle;
    }

    public void setProcessTitle(String processTitle) {
        this.processTitle = processTitle;
    }

    public Integer getProcessId() {
        return processId;
    }

    public void setProcessId(Integer processId) {
        this.processId = processId;
    }

    public String getProcessingStatusTitle() {
        return processingStatusTitle;
    }



    public void setProcessingStatusTitle(String processingStatusTitle) {
        this.processingStatusTitle = processingStatusTitle;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }



    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatusTitle;
    }

    public User getProcessingUser() {
        return processingUser;
    }

    public void setProcessingUser(User processingUser) {
        this.processingUser = processingUser;
    }

    public String getProcessingUserFullName() {
        return processingUserFullName;
    }

    public void setProcessingUserFullName(String processingUserFullName) {
        this.processingUserFullName = processingUserFullName;
    }

    public boolean isCorrection() {
        return correction;
    }

    public void setCorrection(boolean correction) {
        this.correction = correction;
    }

    public int getCorrectionCommentStatus() {
        return correctionCommentStatus;
    }

    public void setCorrectionCommentStatus(int correctionCommentStatus) {
        this.correctionCommentStatus = correctionCommentStatus;
    }

    public String getEditTypeTitle() {
        return editTypeTitle;
    }

    public void setEditTypeTitle(String editTypeTitle) {
        this.editTypeTitle = editTypeTitle;
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

}
