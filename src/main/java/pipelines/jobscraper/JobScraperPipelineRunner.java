package pipelines.jobscraper;

import common.SqlJob;
import database.SqliteClient;

import java.util.List;
import java.util.logging.Logger;

/**
 * Runner for {@link JobScraperPipeline}.
 */
public class JobScraperPipelineRunner {
    private static final Logger LOGGER =
            Logger.getLogger(JobScraperPipelineRunner.class.getName());

    private final JobScraperPipeline jobScraperPipeline;
    private final SqliteClient sqliteClient;

    public JobScraperPipelineRunner(JobScraperPipeline jobScraperPipeline, SqliteClient sqliteClient) {
        this.jobScraperPipeline = jobScraperPipeline;
        this.sqliteClient = sqliteClient;
    }

    public void run() {
        List<SqlJob> jobs = sqliteClient.listJobs();
        LOGGER.info(String.format("Indexing %d companies for internships", jobs.size()));
        int result = jobScraperPipeline.scrapeJobs(jobs);
        LOGGER.info(result + " jobs in DB");
    }
}
