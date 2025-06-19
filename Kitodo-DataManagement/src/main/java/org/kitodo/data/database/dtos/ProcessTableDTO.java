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

package org.kitodo.data.database.dtos;

import java.io.Serializable;
import java.util.List;

/**
 * DTO used by the process list view.  All data are filled once on the server
 * side; no further database look-ups should be required while rendering.
 */
public class ProcessTableDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /* ------------------------------------------------------------- */
    /* core identifiers                                               */
    /* ------------------------------------------------------------- */
    private int id;                 // process id (primary key)
    private int templateId;
    private int projectId;

    /* ------------------------------------------------------------- */
    /* descriptive data                                               */
    /* ------------------------------------------------------------- */
    private String title;
    private String projectTitle;
    private String lastEditingUser;
    private String processingBeginLastTask;
    private String processingEndLastTask;

    /* ------------------------------------------------------------- */
    /* hierarchy / relations                                          */
    /* ------------------------------------------------------------- */
    private boolean hasChildren;
    private int     numberOfChildren;
    private List<ParentProcessInfo> parentProcesses;

    /* ------------------------------------------------------------- */
    /* comments                                                       */
    /* ------------------------------------------------------------- */
    private boolean  hasComments;
    private Integer  correctionCommentStatus;
    private List<CommentDTO> comments;
    private String   lastComment;

    /* ------------------------------------------------------------- */
    /* progress                                                       */
    /* ------------------------------------------------------------- */
    private Double  progressClosed;
    private Double  progressInProcessing;
    private Double  progressOpen;
    private String  progressCombined;
    private boolean hasTasks;
    private String  currentTaskTitles;
    private List<CurrentTaskInfo> tasks;

    /* ------------------------------------------------------------- */
    /* permissions / actions                                          */
    /* ------------------------------------------------------------- */
    private boolean canCreateChildProcess;
    private boolean canCreateProcessWithCalendar;
    private boolean canBeExported;

    /** Pre-built navigation string for the “create child process” action. */
    private String  createChildUrl;

    /* ------------------------------------------------------------- */
    /* getters & setters                                              */
    /* ------------------------------------------------------------- */

    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }

    public int getTemplateId()                { return templateId; }
    public void setTemplateId(int templateId) { this.templateId = templateId; }

    public int getProjectId()                 { return projectId; }
    public void setProjectId(int projectId)   { this.projectId = projectId; }

    public String getTitle()                  { return title; }
    public void setTitle(String title)        { this.title = title; }

    public String getProjectTitle()           { return projectTitle; }
    public void setProjectTitle(String t)     { this.projectTitle = t; }

    public String getLastEditingUser()        { return lastEditingUser; }
    public void setLastEditingUser(String u)  { this.lastEditingUser = u; }

    public String getProcessingBeginLastTask(){ return processingBeginLastTask; }
    public void setProcessingBeginLastTask(String d){ this.processingBeginLastTask = d; }

    public String getProcessingEndLastTask()  { return processingEndLastTask; }
    public void setProcessingEndLastTask(String d){ this.processingEndLastTask = d; }

    public boolean isHasChildren()            { return hasChildren; }
    public void setHasChildren(boolean b)     { this.hasChildren = b; }

    public int getNumberOfChildren()          { return numberOfChildren; }
    public void setNumberOfChildren(int n)    { this.numberOfChildren = n; }

    public List<ParentProcessInfo> getParentProcesses()                 { return parentProcesses; }
    public void setParentProcesses(List<ParentProcessInfo> parents)     { this.parentProcesses = parents; }

    public boolean isHasComments()            { return hasComments; }
    public void setHasComments(boolean b)     { this.hasComments = b; }

    public Integer getCorrectionCommentStatus(){ return correctionCommentStatus; }
    public void setCorrectionCommentStatus(Integer s){ this.correctionCommentStatus = s; }

    public List<CommentDTO> getComments()     { return comments; }
    public void setComments(List<CommentDTO> c){ this.comments = c; }

    public String getLastComment()            { return lastComment; }
    public void setLastComment(String c)      { this.lastComment = c; }

    public Double getProgressClosed()         { return progressClosed; }
    public void setProgressClosed(Double d)   { this.progressClosed = d; }

    public Double getProgressInProcessing()   { return progressInProcessing; }
    public void setProgressInProcessing(Double d){ this.progressInProcessing = d; }

    public Double getProgressOpen()           { return progressOpen; }
    public void setProgressOpen(Double d)     { this.progressOpen = d; }

    public String getProgressCombined()       { return progressCombined; }
    public void setProgressCombined(String s) { this.progressCombined = s; }

    public boolean isHasTasks()               { return hasTasks; }
    public void setHasTasks(boolean b)        { this.hasTasks = b; }

    public String getCurrentTaskTitles()      { return currentTaskTitles; }
    public void setCurrentTaskTitles(String s){ this.currentTaskTitles = s; }

    public List<CurrentTaskInfo> getTasks()   { return tasks; }
    public void setTasks(List<CurrentTaskInfo> t){ this.tasks = t; }

    public boolean isCanCreateChildProcess()  { return canCreateChildProcess; }
    public void setCanCreateChildProcess(boolean b){ this.canCreateChildProcess = b; }

    public boolean isCanCreateProcessWithCalendar(){ return canCreateProcessWithCalendar; }
    public void setCanCreateProcessWithCalendar(boolean b){ this.canCreateProcessWithCalendar = b; }

    public boolean isCanBeExported()          { return canBeExported; }
    public void setCanBeExported(boolean b)   { this.canBeExported = b; }

    public String getCreateChildUrl()         { return createChildUrl; }
    public void setCreateChildUrl(String url) { this.createChildUrl = url; }

    /* ------------------------------------------------------------- */
    /* nested helper classes                                          */
    /* ------------------------------------------------------------- */

    public static class ParentProcessInfo {
        private int     id;
        private String  title;
        private boolean inAssignedProject;
        private boolean locked;

        public int getId()               { return id; }
        public void setId(int id)        { this.id = id; }

        public String getTitle()         { return title; }
        public void setTitle(String t)   { this.title = t; }

        public boolean isInAssignedProject()   { return inAssignedProject; }
        public void setInAssignedProject(boolean b){ this.inAssignedProject = b; }

        public boolean isLocked()        { return locked; }
        public void setLocked(boolean b) { this.locked = b; }
    }

    public static class CurrentTaskInfo {
        private int     id;
        private String  title;
        private String  status;
        private boolean batchStep;
        private boolean batchAvailable;
        private Integer processingUserId;
        private String  processingUserFullName;

        public int getId()               { return id; }
        public void setId(int id)        { this.id = id; }

        public String getTitle()         { return title; }
        public void setTitle(String t)   { this.title = t; }

        public String getStatus()        { return status; }
        public void setStatus(String s)  { this.status = s; }

        public boolean isBatchStep()     { return batchStep; }
        public void setBatchStep(boolean b){ this.batchStep = b; }

        public boolean isBatchAvailable(){ return batchAvailable; }
        public void setBatchAvailable(boolean b){ this.batchAvailable = b; }

        public Integer getProcessingUserId()          { return processingUserId; }
        public void     setProcessingUserId(Integer i){ this.processingUserId = i; }

        public String getProcessingUserFullName()     { return processingUserFullName; }
        public void setProcessingUserFullName(String s){ this.processingUserFullName = s; }
    }

    public static class CommentDTO {
        private String  message;
        private String  authorFullName;
        private String  creationDate;   // rendered already formatted
        private boolean corrected;
        private String  type;           // "ERROR", "INFO", …

        public String getMessage()      { return message; }
        public void setMessage(String m){ this.message = m; }

        public String getAuthorFullName(){ return authorFullName; }
        public void setAuthorFullName(String s){ this.authorFullName = s; }

        public String getCreationDate() { return creationDate; }
        public void setCreationDate(String d){ this.creationDate = d; }

        public boolean isCorrected()    { return corrected; }
        public void setCorrected(boolean b){ this.corrected = b; }

        public String getType()         { return type; }
        public void setType(String t)   { this.type = t; }
    }
}
