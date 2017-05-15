package scrapermain;

import common.PageRequester;
import database.SqliteClient;
import pipelines.companyid.CompanyIdPipeline;
import pipelines.companyid.CompanyIdPipelineRunner;
import pipelines.jobfinder.JobFinderPipeline;
import pipelines.jobfinder.JobFinderPipelineRunner;
import pipelines.jobscraper.JobScraperPipeline;
import pipelines.jobscraper.JobScraperPipelineRunner;

/**
 * Manager that completes actions based on the {@link RunningMode} of the scraper.
 */
public class RunningModeManager implements AutoCloseable {

    private static final String DEFAULT_INPUT_SIFTERY_FILE = "config/siftery/sifteryCustomerList.csv";
    private static final String DATABASE_LOCATION = "db/JScraper.db";
    
    private final SqliteClient sqliteClient;
    private final CompanyIdPipelineRunner companyIdPipelineRunner;
    private final JobFinderPipelineRunner jobFinderPipelineRunner;
    private final JobScraperPipelineRunner jobScraperPipelineRunner;

    private RunningModeManager() throws Exception {

        sqliteClient = new SqliteClient(DATABASE_LOCATION);

        PageRequester pageRequester = new PageRequester();
        CompanyIdPipeline companyIdPipeline = new CompanyIdPipeline(sqliteClient, pageRequester);
        companyIdPipelineRunner = new CompanyIdPipelineRunner(companyIdPipeline);

        JobFinderPipeline jobFinderPipeline = new JobFinderPipeline();
        jobFinderPipelineRunner = new JobFinderPipelineRunner(
                jobFinderPipeline, sqliteClient, pageRequester);

        JobScraperPipeline jobScraperPipeline = new JobScraperPipeline(sqliteClient, pageRequester);
        jobScraperPipelineRunner =
                new JobScraperPipelineRunner(jobScraperPipeline, sqliteClient);
    }

    private void setMode(RunningMode runningMode) {
        switch (runningMode) {
            case FULL_RUN:
                sqliteClient.clearCompanies();
                sqliteClient.clearJobs();
                sqliteClient.clearFields();
                break;
            case RETAIN_COMPANIES:
                sqliteClient.clearJobs();
                sqliteClient.clearFields();
                break;
            default:
                throw new IllegalArgumentException("Unsupported running mode: " + runningMode.name());
        }
    }

    public void run(RunningMode runningMode) {
        setMode(runningMode);
        switch(runningMode) {
            case FULL_RUN:
                companyIdPipelineRunner.run(DEFAULT_INPUT_SIFTERY_FILE);
                jobFinderPipelineRunner.run();
                jobScraperPipelineRunner.run();
                break;
            case RETAIN_COMPANIES:
                jobFinderPipelineRunner.run();
                jobScraperPipelineRunner.run();
                break;
        }
    }

    public static RunningModeManager create() throws Exception {
        return new RunningModeManager();
    }

    @Override
    public void close() throws Exception {
        sqliteClient.close();
    }
}
