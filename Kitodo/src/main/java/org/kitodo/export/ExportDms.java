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

package org.kitodo.export;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ConfigurationException;
import org.kitodo.exceptions.ExportException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.exceptions.MetadataException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.VariableReplacer;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyDocStructHelperInterface;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetadataHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyMetsModsDigitalDocumentHelper;
import org.kitodo.production.helper.metadata.legacytypeimplementations.LegacyPrefsHelper;
import org.kitodo.production.helper.tasks.ExportDmsTask;
import org.kitodo.production.helper.tasks.TaskManager;
import org.kitodo.production.helper.tasks.TaskSitter;
import org.kitodo.production.metadata.copier.CopierData;
import org.kitodo.production.metadata.copier.DataCopier;
import org.kitodo.production.model.Subfolder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.file.FileService;
import org.kitodo.production.services.workflow.WorkflowControllerService;
import org.xml.sax.SAXException;

public class ExportDms {
    private static final Logger logger = LogManager.getLogger(ExportDms.class);
    private static final String EXPORT_DIR_DELETE = "errorDirectoryDeleting";
    private static final String ERROR_EXPORT = "errorExport";

    private final FileService fileService = ServiceManager.getFileService();
    private final ProcessService processService = ServiceManager.getProcessService();
    private final ExportMets exportMets = new ExportMets();

    private boolean exportWithImages = true;
    private Task workFlowTask;
    private ExportBatchState exportBatchState = null;

    public ExportDms() {
    }

    public ExportDms(Task workFlowTask) {
        this.workFlowTask = workFlowTask;
    }

    public void setExportWithImages(boolean exportWithImages) {
        this.exportWithImages = exportWithImages;
    }


    /**
     * Single-process entry point, no batch / hierarchy orchestration.
     */
    public static void exportProcess(Process process, Task taskOrNull) throws DAOException {
        exportProcess(process, taskOrNull, true);
    }

    /**
     * Single-process entry point, no batch / hierarchy orchestration.
     */
    public static void exportProcess(Process process, Task taskOrNull, boolean withImages) throws DAOException {
        ExportDms export = new ExportDms(taskOrNull);
        export.setExportWithImages(withImages);
        export.scheduleProcess(process);
    }

    public static void exportProcesses(List<Process> processes, Task taskOrNull) throws DAOException {
        exportProcesses(processes, taskOrNull, true);
    }


    public static void exportProcesses(List<Process> processes, Task taskOrNull, boolean withImages)
            throws DAOException {

        ExportHierarchyPlanner planner = new ExportHierarchyPlanner();
        List<Process> planned = planner.plan(processes);

        ExportBatchState batch = new ExportBatchState();
        planned.forEach(batch::register);

        ExportDms export = new ExportDms(taskOrNull);
        export.setExportWithImages(withImages);
        export.setBatchState(batch);

        for (Process process : planned) {
            if (batch.isReady(process) && batch.markStarted(process)) {
                export.scheduleProcess(process);
            }
        }
    }

    private void scheduleProcess(Process process) throws DAOException {
        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.ASYNCHRONOUS_AUTOMATIC_EXPORT)) {
            TaskManager.addTask(new ExportDmsTask(this, process));
            Helper.setMessage(TaskSitter.isAutoRunningThreads() ? "DMSExportByThread" : "DMSExportThreadCreated",
                    process.getTitle());
        } else {
            boolean success = false;
            try {
                success = performExportForProcess(process, null);
            } catch (IOException | SAXException | FileStructureValidationException | DAOException e) {
                Helper.setErrorMessage(ERROR_EXPORT, new Object[] {process.getTitle() }, logger, e);
            } finally {
                onSingleProcessFinished(process, success, null);
            }
        }
    }


    /**
     * Pure execution of one process in the current thread.
     * No task scheduling. No parent triggering.
     */
    public boolean performExportForProcess(Process process, ExportDmsTask taskOrNull)
            throws DAOException, IOException, SAXException, FileStructureValidationException {

           boolean wasNotAlreadyExported = !process.isExported();
        if (wasNotAlreadyExported) {
            process.setExported(true);
            processService.save(process);
        }
        boolean exportSuccessful = false;
        try {
            exportSuccessful = startExportInternal(process, processService.readMetadataFile(process)
                    .getDigitalDocument(), taskOrNull);
            return exportSuccessful;
        } catch (IOException | DAOException | SAXException | FileStructureValidationException e) {
            Helper.setErrorMessage(ERROR_EXPORT, new Object[] {process.getTitle() }, logger, e);
            throw e;
        } finally {
            if (wasNotAlreadyExported) {
                process.setExported(exportSuccessful);
                processService.save(process);
            }
        }
    }

    /**
     * Completion hook for both sync and async execution.
     */
    public void onSingleProcessFinished(Process process, boolean success, ExportDmsTask taskOrNull) {
        if (!success) {
            return;
        }

        Task workflowTask = getWorkflowTask();
        if (Objects.nonNull(workflowTask)
                && workflowTask.getProcess().getId().equals(process.getId())) {
            try {
                if (Objects.nonNull(taskOrNull)) {
                    taskOrNull.setProgress(100);
                }
                new WorkflowControllerService().close(workflowTask);
            } catch (IOException | SAXException | FileStructureValidationException | DAOException e) {
                logger.error("Failed to close workflow task for process " + process.getId(), e);
                if (Objects.nonNull(taskOrNull)) {
                    taskOrNull.setException(e);
                }
            }
        }

        ExportBatchState batch = getBatchState();
        if (Objects.nonNull(batch)) {
            batch.finished(process);
            Process parent = process.getParent();
            if (Objects.nonNull(parent) && batch.isReady(parent) && batch.markStarted(parent)) {
                try {
                    scheduleProcess(parent);
                } catch (DAOException e) {
                    logger.error("Failed to launch parent export for process " + parent.getId(), e);
                }
            }
        }
    }

    /**
     * Actual export logic for a single process.
     */
    private boolean startExportInternal(Process process, LegacyMetsModsDigitalDocumentHelper newFile, ExportDmsTask taskOrNull)
            throws IOException, DAOException, SAXException, FileStructureValidationException {

        LegacyPrefsHelper prefs = ServiceManager.getRulesetService().getPreferences(process.getRuleset());

        LegacyMetsModsDigitalDocumentHelper gdzfile = readDocument(process, newFile, prefs, taskOrNull);
        if (Objects.isNull(gdzfile)) {
            return false;
        }

        boolean dataCopierResult = executeDataCopierProcess(gdzfile, process, taskOrNull);
        if (!dataCopierResult) {
            return false;
        }

        trimAllMetadata(gdzfile.getDigitalDocument().getLogicalDocStruct());

        if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.USE_META_DATA_VALIDATION)
                && !ServiceManager.getMetadataValidationService().validate(gdzfile, prefs)) {
            if (Objects.nonNull(taskOrNull)) {
                taskOrNull.setException(new MetadataException("metadata validation failed", null));
            }
            return false;
        }

        return prepareExportLocation(process, gdzfile, taskOrNull);
    }


    /*private boolean exportCompletedChildren(List<Process> children) throws DAOException {
        for (Process child:children) {
            if (ProcessConverter.getCombinedProgressAsString(child, false).equals(ProcessState.COMPLETED.getValue())
                    && !child.isExported()) {
                if (!startExport(child)) {
                    return false;
                }
            }
        }
        return true;
    }*/

    private boolean prepareExportLocation(Process process,
            LegacyMetsModsDigitalDocumentHelper gdzfile, ExportDmsTask taskOrNull) throws IOException, DAOException, SAXException,
            FileStructureValidationException {

        URI hotfolder = new File(process.getProject().getDmsImportRootPath()).toURI();
        String processTitle = Helper.getNormalizedTitle(process.getTitle());
        URI exportFolder = new File(hotfolder.getPath(), processTitle).toURI();
        ReentrantLock lock = ExportDirectoryGuard.lock(exportFolder);
        // delete old export folder
        try {
            if (!fileService.delete(exportFolder)) {
                String message = Helper.getTranslation(ERROR_EXPORT, processTitle);
                String description = Helper.getTranslation(EXPORT_DIR_DELETE, exportFolder.getPath());
                Helper.setErrorMessage(message, description);
                if (Objects.nonNull(taskOrNull)) {
                    taskOrNull.setException(new ExportException(message + ": " + description));
                }
                return false;
            }
            fileService.createDirectory(hotfolder, processTitle);
            if (Objects.nonNull(taskOrNull)) {
                taskOrNull.setProgress(1);
            }
            return exportImagesAndMetsToDestinationUri(process, gdzfile, exportFolder, taskOrNull);
        } finally {
            ExportDirectoryGuard.unlock(lock);
        }
    }

    private boolean exportImagesAndMetsToDestinationUri(Process process, LegacyMetsModsDigitalDocumentHelper gdzfile,
            URI destination, ExportDmsTask taskOrNull) throws IOException, DAOException, SAXException, FileStructureValidationException {

        if (exportWithImages) {
            try {
                directoryDownload(process, destination, taskOrNull);
            } catch (IOException | InterruptedException | RuntimeException | URISyntaxException e) {
                if (Objects.nonNull(taskOrNull)) {
                    taskOrNull.setException(e);
                } else {
                    Helper.setErrorMessage(ERROR_EXPORT, new Object[] {process.getTitle() }, logger, e);
                }
                return false;
            }
        }

        // export the file to the import folder
        return asyncExportWithImport(process, gdzfile, destination, taskOrNull);
    }

    private boolean executeDataCopierProcess(LegacyMetsModsDigitalDocumentHelper gdzfile, Process process, ExportDmsTask taskOrNull) {
        try {
            String rules = ConfigCore.getParameter(ParameterCore.COPY_DATA_ON_EXPORT);
            if (Objects.nonNull(rules) && !executeDataCopierProcess(gdzfile, process, rules, taskOrNull)) {
                return false;
            }
        } catch (NoSuchElementException e) {
            logger.catching(Level.TRACE, e);
            // no configuration simply means here is nothing to do
        }
        return true;
    }

    private boolean executeDataCopierProcess(LegacyMetsModsDigitalDocumentHelper gdzfile, Process process,
            String rules, ExportDmsTask taskOrNull) {
        try {
            new DataCopier(rules).process(new CopierData(gdzfile, process));
        } catch (ConfigurationException e) {
            if (Objects.nonNull(taskOrNull)) {
                taskOrNull.setException(e);
            } else {
                Helper.setErrorMessage("dataCopier.syntaxError", e.getMessage(), logger, e);
                return false;
            }
        }
        return true;
    }

    private LegacyMetsModsDigitalDocumentHelper readDocument(Process process, LegacyMetsModsDigitalDocumentHelper newFile,
                                                             LegacyPrefsHelper prefs, ExportDmsTask taskOrNull) {
        LegacyMetsModsDigitalDocumentHelper gdzfile;
        try {
            gdzfile = new LegacyMetsModsDigitalDocumentHelper(prefs.getRuleset());
            gdzfile.setDigitalDocument(newFile);
            return gdzfile;
        } catch (RuntimeException e) {
            if (Objects.nonNull(taskOrNull)) {
                taskOrNull.setException(e);
                logger.error(Helper.getTranslation(ERROR_EXPORT, process.getTitle()), e);
            } else {
                Helper.setErrorMessage(ERROR_EXPORT, new Object[] {process.getTitle() }, logger, e);
            }
            return null;
        }
    }

    private boolean asyncExportWithImport(Process process, LegacyMetsModsDigitalDocumentHelper gdzfile, URI userHome, ExportDmsTask taskOrNull)
            throws IOException, DAOException, SAXException, FileStructureValidationException {

        String atsPpnBand = Helper.getNormalizedTitle(process.getTitle());
        if (Objects.nonNull(taskOrNull)) {
            taskOrNull.setWorkDetail(atsPpnBand + ".xml");
        }
        boolean metsFileWrittenSuccesful = exportMets.writeMetsFile(process, fileService.createResource(userHome,
                File.separator + atsPpnBand + ".xml"), gdzfile);

        if (Objects.nonNull(taskOrNull)) {
            taskOrNull.setProgress(100);
        }
        return metsFileWrittenSuccesful;
    }


    /**
     * Get workflowTask.
     *
     * @return value of workFlowTask
     */
    public Task getWorkflowTask() {
        return workFlowTask;
    }

    public void setBatchState(ExportBatchState exportBatchState) {
        this.exportBatchState = exportBatchState;
    }

    public ExportBatchState getBatchState() {
        return exportBatchState;
    }

    /**
     * Run through all metadata and children of given docstruct to trim the strings
     * calls itself recursively.
     */
    private void trimAllMetadata(LegacyDocStructHelperInterface inStruct) {
        // trim all metadata values
        for (LegacyMetadataHelper md : inStruct.getAllMetadata()) {
            if (Objects.nonNull(md.getValue())) {
                md.setStringValue(md.getValue().trim());
            }
        }

        // run through all children of docstruct
        for (LegacyDocStructHelperInterface child : inStruct.getAllChildren()) {
            trimAllMetadata(child);
        }
    }

    /**
     * Download image.
     *
     * @param process
     *            object
     * @param userHome
     *            File
     * @param atsPpnBand
     *            String
     * @param ordnerEndung
     *            String
     */
    public void imageDownload(Process process, URI userHome, String atsPpnBand, final String ordnerEndung, ExportDmsTask taskOrNull)
            throws IOException {
        // determine the source folder
        URI tifOrdner = processService.getImagesTifDirectory(true, process.getId(),
            process.getTitle(), process.getProcessBaseUri());

        // copy the source folder to the destination folder
        if (fileService.fileExist(tifOrdner) && !fileService.getSubUris(tifOrdner).isEmpty()) {
            URI zielTif = userHome.resolve(atsPpnBand + ordnerEndung + "/");

            // with Agora import simply create the folder
            if (!fileService.fileExist(zielTif)) {
                fileService.createDirectory(userHome, atsPpnBand + ordnerEndung);
            }

            if (Objects.nonNull(taskOrNull)) {
                taskOrNull.setWorkDetail(null);
            }
        }
    }

    /**
     * Starts copying all directories configured as export folder.
     *
     * @param process
     *            object
     * @param destination
     *            the destination directory
     * @throws InterruptedException
     *             if the user clicked stop on the thread running the export DMS
     *             task
     *
     */
    private void directoryDownload(Process process, URI destination, ExportDmsTask taskOrNull) throws IOException, InterruptedException, URISyntaxException {
        Collection<Subfolder> processDirs = process.getProject().getFolders().parallelStream()
                .filter(Folder::isCopyFolder).map(folder -> new Subfolder(process, folder)).toList();
        VariableReplacer variableReplacer = new VariableReplacer(null, process, null);

        String uriToDestination = destination.toString();
        if (!uriToDestination.endsWith("/")) {
            uriToDestination = uriToDestination.concat("/");
        }
        for (Subfolder processDir : processDirs) {
            URI dstDir = new URI(uriToDestination
                    + variableReplacer.replace(processDir.getFolder().getRelativePath()));
            fileService.createDirectories(dstDir);

            Collection<URI> srcs = processDir.listContents().values();
            int progress = 0;
            for (URI src : srcs) {
                if (Objects.nonNull(taskOrNull)) {
                    taskOrNull.setWorkDetail(fileService.getFileName(src));
                }
                fileService.copyFileToDirectory(src, dstDir);
                if (Objects.nonNull(taskOrNull)) {
                    taskOrNull.setProgress((int) ((progress++ + 1) * 98d / processDirs.size() / srcs.size() + 1));
                    if (taskOrNull.isInterrupted()) {
                        throw new InterruptedException();
                    }
                }
            }
        }
    }
}
