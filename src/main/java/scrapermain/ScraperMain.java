package scrapermain;

import common.PageRequester;
import database.SqliteClient;
import pipelines.companyid.CompanyIdPipeline;
import pipelines.companyid.CompanyIdPipelineRunner;
import pipelines.jobfinder.JobFinderPipeline;
import pipelines.jobfinder.JobFinderPipelineRunner;
import pipelines.jobscraper.JobScraperPipeline;
import pipelines.jobscraper.JobScraperPipelineRunner;

import java.util.logging.Logger;

public class ScraperMain {
    private static final String INPUT_SIFTERY_FILE = "config/siftery/sifteryCustomerList.csv";

    private static final Logger LOGGER =
            Logger.getLogger(ScraperMain.class.getName());
    private static final String DATABASE_LOCATION = "db/JScraper.db";

    public static void main(String[] args) {
        try {
            SqliteClient sqliteClient = new SqliteClient(DATABASE_LOCATION);
            PageRequester pageRequester = new PageRequester();

            RunningModeManager runningModeManager = new RunningModeManager(sqliteClient);

            CompanyIdPipeline companyIdPipeline = new CompanyIdPipeline(sqliteClient, pageRequester);
            CompanyIdPipelineRunner companyIdPipelineRunner = new CompanyIdPipelineRunner(companyIdPipeline);

            JobFinderPipeline jobFinderPipeline = new JobFinderPipeline();
            JobFinderPipelineRunner jobFinderPipelineRunner = new JobFinderPipelineRunner(
                    jobFinderPipeline, sqliteClient, pageRequester);

            JobScraperPipeline jobScraperPipeline = new JobScraperPipeline(sqliteClient, pageRequester);
            JobScraperPipelineRunner jobScraperPipelineRunner =
                    new JobScraperPipelineRunner(jobScraperPipeline, sqliteClient);

            runningModeManager.setMode(RunningMode.RETAIN_COMPANIES);
            //companyIdPipelineRunner.run(INPUT_SIFTERY_FILE);
            jobFinderPipelineRunner.run();
            jobScraperPipelineRunner.run();

            sqliteClient.close();
        } catch (Exception e) {
            // swallow exceptions and log them to not crash the program
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }

    }

}
