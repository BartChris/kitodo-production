package org.kitodo.data.database.beans;

import org.kitodo.data.database.enums.TaskStatus;

import java.util.Date;

public class TaskTableDTO {
    private Integer id;
    private String title;

    private String processTitle;
    private Integer processId;
    private String projectTitle;

    private String processCreationDate;

    private String processingStatus;
    private String processingStatusTitle;

    private String processingUserFullName;

    private Date processingBegin;
    private Date processingEnd;
    private Date processingTime;

    private boolean correction;
    private int correctionCommentStatus;

    private String editTypeTitle;

    private boolean batchStep;
    private boolean batchAvailable;

    private Integer processingUserId;



    private String correctionComment;

    public String getCorrectionComment() {
        return correctionComment;
    }
    public void setCorrectionComment(String correctionComment) {
        this.correctionComment = correctionComment;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProcessingUserId() {
        return processingUserId;
    }

    public void setProcessingUserId(Integer processingUserId) {
        this.processingUserId = processingUserId;
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

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getProcessCreationDate() {
        return processCreationDate;
    }

    public void setProcessCreationDate(String processCreationDate) {
        this.processCreationDate = processCreationDate;
    }

    public String getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
    }

    public String getProcessingStatusTitle() {
        return processingStatusTitle;
    }

    public void setProcessingStatusTitle(String processingStatusTitle) {
        this.processingStatusTitle = processingStatusTitle;
    }

    public String getProcessingUserFullName() {
        return processingUserFullName;
    }

    public void setProcessingUserFullName(String processingUserFullName) {
        this.processingUserFullName = processingUserFullName;
    }

    public Date getProcessingBegin() {
        return processingBegin;
    }

    public void setProcessingBegin(Date processingBegin) {
        this.processingBegin = processingBegin;
    }

    public Date getProcessingEnd() {
        return processingEnd;
    }

    public void setProcessingEnd(Date processingEnd) {
        this.processingEnd = processingEnd;
    }

    public Date getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(Date processingTime) {
        this.processingTime = processingTime;
    }

    public boolean getIsCorrection() {
        return correction;
    }

    public void setIsCorrection(boolean correction) {
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

    public boolean getBatchStep() {
        return batchStep;
    }

    public void setBatchStep(boolean batchStep) {
        this.batchStep = batchStep;
    }

    public boolean getBatchAvailable(boolean batchAvailable) {
       return batchAvailable;
    }

    public void setBatchAvailable(boolean batchAvailable) {
        this.batchAvailable = batchAvailable;
    }
}
