package edu.yale.library.ladybird.engine.cron.job;


import edu.yale.library.ladybird.engine.EventBus;
import edu.yale.library.ladybird.engine.JobStatus;
import edu.yale.library.ladybird.engine.cron.ImportJob;
import edu.yale.library.ladybird.engine.cron.ProgressEvent;
import edu.yale.library.ladybird.engine.cron.queue.ImportContextQueue;
import edu.yale.library.ladybird.engine.cron.queue.ImportEngineQueue;
import edu.yale.library.ladybird.engine.exports.ExportRequestEvent;
import edu.yale.library.ladybird.engine.imports.DefaultImportEngine;
import edu.yale.library.ladybird.engine.imports.ImportCompleteEvent;
import edu.yale.library.ladybird.engine.imports.ImportCompleteEventBuilder;
import edu.yale.library.ladybird.engine.imports.ImportEngine;
import edu.yale.library.ladybird.engine.imports.ImportEngineException;
import edu.yale.library.ladybird.engine.imports.Import;
import edu.yale.library.ladybird.engine.imports.ImportReaderValidationException;
import edu.yale.library.ladybird.engine.imports.ImportRequestEvent;
import edu.yale.library.ladybird.engine.imports.ImageFunctionProcessor;
import edu.yale.library.ladybird.engine.imports.ReadMode;
import edu.yale.library.ladybird.engine.imports.Spreadsheet;
import edu.yale.library.ladybird.engine.oai.ImportSourceProcessor;
import edu.yale.library.ladybird.engine.oai.OaiProvider;
import edu.yale.library.ladybird.entity.ImportSource;
import edu.yale.library.ladybird.entity.Settings;
import edu.yale.library.ladybird.entity.User;
import edu.yale.library.ladybird.kernel.ApplicationProperties;
import edu.yale.library.ladybird.kernel.notificaiton.NotificationEventQueue;
import edu.yale.library.ladybird.persistence.dao.ImportSourceDAO;
import edu.yale.library.ladybird.persistence.dao.SettingsDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.ImportSourceHibernateDAO;
import edu.yale.library.ladybird.persistence.dao.hibernate.SettingsHibernateDAO;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;
import static org.slf4j.LoggerFactory.getLogger;

public class DefaultImportJob implements Job, ImportJob {

    private final Logger logger = getLogger(this.getClass());

    /**
     * Execute the full cycle and notify the list of users.
     *
     * @param arg0 JobExecution
     * @throws JobExecutionException
     */
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        final long startTime = System.currentTimeMillis();
        final ImportRequestEvent importReqEvent = ImportEngineQueue.getJob();
        final Spreadsheet spreadsheet = importReqEvent.getSpreadsheet();

        logger.debug("Starting importJobRequestId={} corresponding to file={}",
                importReqEvent.getJobRequest().getId(), spreadsheet);

        try {
            final int userId = importReqEvent.getJobRequest().getUser().getUserId();
            final int projectId = importReqEvent.getJobRequest().getCurrentProject().getProjectId();

            final ImportEngine importEngine = new DefaultImportEngine(userId, projectId);

            //Post init
            ProgressEvent progressEvent = new ProgressEvent(importReqEvent.getJobRequest().getId(), importReqEvent,
                    JobStatus.INIT);
            EventBus.post(progressEvent);

            final List<Import.Row> rowList = importEngine.read(spreadsheet, ReadMode.FULL);
            logger.trace("Read rows size={}", rowList.size());

            //TODO
            final OaiProvider provider = getCtxOaiProvider();
            importEngine.setOaiProvider(provider);

            //passes relative path for each import job.
            //This is provided by the user on each run. The root path is set application wide.
            final ImageFunctionProcessor imageFunctionProcessor =
                    getCtxMediaFunctionProcessor(importReqEvent.getJobRequest().getExportPath());
            importEngine.setImageFunctionProcessor(imageFunctionProcessor);
            importEngine.setImportSourceProcessor(new ImportSourceProcessor());

            logger.debug("Writing to import table(s) for job={}", importReqEvent.getImportId());

            final int imid = importEngine.write(rowList, spreadsheet, importReqEvent.getJobRequest().getId());

            long elapsedImport = System.currentTimeMillis() - startTime;
            logger.debug("Completed import job={} in={}", imid, formatDuration(elapsedImport, "HH:mm:ss:SS"));

            final ImportCompleteEvent importCompEvent = new ImportCompleteEventBuilder().setTime(elapsedImport)
                    .setRowsProcessed(rowList.size()).createImportDoneEvent();
            importCompEvent.setImportId(imid);

            //Post progress
            EventBus.post(new ProgressEvent(importReqEvent.getJobRequest().getId(), importCompEvent,
                    JobStatus.DONE));

            sendNotification(importCompEvent, Collections.singletonList(importReqEvent.getJobRequest().getUser()));

            logger.debug("Added import event for importId={} to notification queue", imid);

            //This drives both the export and object metadata writing. import context job adds an importcontext
            //object to objWriteQ and exportQ:

            //FIXME. This adds posts the ExportRequestEvent, which is not accurate
            // it may not be the case that export is desired automatically (and only obj_ writing is desired)
            // it should also post some ImportContext related event, indicating that an oai feed will be read
            final ExportRequestEvent exportEvent = new ExportRequestEvent(imid, importReqEvent.getJobRequest());
            ImportContextQueue.addJob(exportEvent);

            logger.trace("Added event to ExportEngineQueue=" + exportEvent.toString());
        } catch (ImportReaderValidationException e) {
            logger.error("Validation exception", e);
            throw new ImportEngineException(e);
        } catch (IOException e) {
            logger.error("Error executing job", e.getMessage());
            throw new ImportEngineException(e);
        } catch (final ImportEngineException cre) {
            logger.error("Exception in import job number={}.", importReqEvent.getJobRequest().getId(), cre);
            throw cre;
        } catch (Exception e) {
            logger.error("Exception in import job number={}.", importReqEvent.getJobRequest().getId(), e);
            throw new ImportEngineException(e);
        }
    }

    private void sendNotification(final ImportCompleteEvent importEvent, final List<User> userList) {
        String subject = "Job #" + importEvent.getImportId() + " spreadsheet import complete";
        String message = "Rows imported from file: " + importEvent.getRowsProcessed();
        message += ", Time: " + DurationFormatUtils.formatDurationWords(importEvent.getTime(), true, true);
        NotificationEventQueue.addEvent(new NotificationEventQueue().new NotificationItem(importEvent, userList, message, subject));
    }

    /**
     * Returns OAI provider. Subject to removal (when oai provider is avaialable via some sort of context).
     * Note: returns the 1st provider with status active.
     *
     * @return
     */
    private OaiProvider getCtxOaiProvider() {
        final ImportSourceDAO importSourceDAO = new ImportSourceHibernateDAO();
        final List<ImportSource> importSourceList = importSourceDAO.findAll();

        for (ImportSource importSource : importSourceList) {
            if (importSource.isActive()) {
                return new OaiProvider("id", importSource.getUrl(), importSource.getGetPrefix());
            }
        }
        return null;
    }

    /**
     * Returns a MediaFunctionProcessor if db state is found
     */
    private ImageFunctionProcessor getCtxMediaFunctionProcessor(final String path) {
        SettingsDAO settingsDAO = new SettingsHibernateDAO();
        final Settings settings = settingsDAO.findByProperty(ApplicationProperties.IMPORT_ROOT_PATH_ID);

        if (settings == null) {
            logger.debug("No db configured property={}", ApplicationProperties.IMPORT_ROOT_PATH_ID);
            return new ImageFunctionProcessor(ApplicationProperties.CONFIG_STATE.IMPORT_ROOT_PATH, path);
        }
        final String rootPath = settings.getValue();
        return new ImageFunctionProcessor(rootPath, path);
    }

}
