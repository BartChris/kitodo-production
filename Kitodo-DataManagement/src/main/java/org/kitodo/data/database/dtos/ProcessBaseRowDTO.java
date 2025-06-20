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

import org.kitodo.data.database.beans.Folder;

import java.io.Serializable;

/**
 * Lightweight projection of a process row used by the process table.
 *
 * <pre>
 * SELECT NEW org.kitodo.data.database.dtos.ProcessBaseRowDTO(
 *            p.id, p.title,
 *            p.template.id,
 *            p.project.id, p.project.title)
 * FROM   Process p …
 * </pre>
 */
public class ProcessBaseRowDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /* ------------------------------------------------------------------ */
    /*  columns                                                           */
    /* ------------------------------------------------------------------ */
    private final int    processId;
    private final String title;
    private final int    templateId;
    private final int    projectId;
    private final String projectTitle;


    /* ------------------------------------------------------------------ */
    /*  constructor used by Hibernate                                     */
    /* ------------------------------------------------------------------ */
    public ProcessBaseRowDTO(int processId, String title, int templateId, int projectId, String projectTitle) {
        this.processId = processId;
        this.title = title;
        this.templateId = templateId;
        this.projectId = projectId;
        this.projectTitle = projectTitle;
    }

    /* ------------------------------------------------------------------ */
    /*  getters (DTO is read-only, no setters needed)                     */
    /* ------------------------------------------------------------------ */
    public int    getProcessId()    { return processId; }
    public String getTitle()        { return title; }
    public int    getTemplateId()   { return templateId; }
    public int    getProjectId()    { return projectId; }
    public String getProjectTitle() { return projectTitle; }

}
