/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 * Licensed under GNU GPL v3 or later – see GPL3-License.txt.
 */
package org.kitodo.data.database.dtos;

import java.io.Serializable;
import org.kitodo.data.database.enums.TaskStatus;

/**
 * Lightweight projection of a task row used by the process table.
 *
 * <pre>
 * SELECT NEW org.kitodo.data.database.dtos.TaskRowDTO(
 *            t.id, p.id,
 *            u.id,
 *            CONCAT(u.name,' ',u.surname),
 *            t.title,
 *            t.processingStatus,
 *            t.batchStep,
 *            t.batchAvailable)
 * FROM   Task t …
 * </pre>
 */
public class TaskRowDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /* ------------------------------------------------------------------ */
    /*  columns                                                           */
    /* ------------------------------------------------------------------ */
    private final int         taskId;
    private final int         processId;
    private final Integer     userId;          // may be null
    private final String      userFullName;    // may be null
    private final String      taskTitle;
    private final TaskStatus  taskStatus;

    /* ------------------------------------------------------------------ */
    /*  constructor used by Hibernate                                     */
    /* ------------------------------------------------------------------ */
    public TaskRowDTO(int         taskId,
                      int         processId,
                      Integer     userId,
                      String      userFullName,
                      String      taskTitle,
                      TaskStatus  taskStatus){

        this.taskId         = taskId;
        this.processId      = processId;
        this.userId         = userId;
        this.userFullName   = userFullName;
        this.taskTitle      = taskTitle;
        this.taskStatus     = taskStatus;
    }

    /* ------------------------------------------------------------------ */
    /*  getters (DTO is read-only, no setters needed)                     */
    /* ------------------------------------------------------------------ */
    public int        getTaskId()        { return taskId; }
    public int        getProcessId()     { return processId; }
    public Integer    getUserId()        { return userId; }
    public String     getUserFullName()  { return userFullName; }
    public String     getTaskTitle()     { return taskTitle; }
    public TaskStatus getTaskStatus()    { return taskStatus; }


    /* convenience                                                      */
    public boolean hasProcessor() { return userId != null; }
}
