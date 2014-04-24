package edu.yale.library.ladybird.engine.cron;


import static org.slf4j.LoggerFactory.getLogger;


import edu.yale.library.ladybird.kernel.cron.DefaultJobsManager;
import edu.yale.library.ladybird.engine.CronSchedulingException;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;

public class ImportScheduler {

    private final Logger logger = getLogger(this.getClass());

    /**
     * Schedules an import cron job. To be called from kernel at start up.
     *
     * @param jobName
     * @param triggerName
     * @param cronExpression
     * @throws Exception
     */
    public void scheduleJob(final String jobName, final String triggerName, final String cronExpression) {
        logger.debug("Scheduling import job");

        JobDetail job;
        try {
            job = getJob(jobName, ImportJobFactory.getInstance().getClass());
            Trigger trigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity("IMG-TRIGER", "IMJ")
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();
            doScheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new CronSchedulingException(e);
        }

        DefaultJobsManager defaultJobsManager = new DefaultJobsManager();
        defaultJobsManager.addJob(job);
    }

    @Deprecated
    public void scheduleJob(final String jobName, final Trigger trigger) {
        logger.debug("Scheduling import job");

        JobDetail job;
        try {
            job = getJob(jobName, ImportJobFactory.getInstance().getClass());
            doScheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw new CronSchedulingException(e);
        }

        DefaultJobsManager defaultJobsManager = new DefaultJobsManager();
        defaultJobsManager.addJob(job);
    }

    private void doScheduleJob(final JobDetail job, final Trigger trigger) throws SchedulerException {
        try {
            final Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    protected JobDetail getJob(String jobName, Class klass) {
        JobDetail job = JobBuilder.newJob(klass)
                .withIdentity(jobName, "IMJ").build();
        return job;
    }

}

