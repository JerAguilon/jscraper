package pipelines.jobfinder;

import common.CompanyId;
import common.PageRequester;
import database.SqliteClient;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Runner class for the {@link JobFinderPipeline}.
 */
public class JobFinderPipelineRunner {

    private static final Logger LOGGER =
            Logger.getLogger(JobFinderPipelineRunner.class.getName());

    private static final String XML_URL_PREFIX = "https://app.jobvite.com/CompanyJobs/xml.aspx?c=";

    private final JobFinderPipeline jobFinderPipeline;
    private final SqliteClient sqliteClient;
    private final PageRequester pageRequester;

    public JobFinderPipelineRunner(JobFinderPipeline jobfinderPipeline, SqliteClient sqliteClient, PageRequester pageRequester) {
        this.jobFinderPipeline = jobfinderPipeline;
        this.sqliteClient = sqliteClient;
        this.pageRequester = pageRequester;
    }

    public void run() {
        List<CompanyId> companyIds = sqliteClient.listCompanies();
        LOGGER.info(String.format("Indexing %d companies for internships", companyIds.size()));
        companyIds.forEach(c -> {
            try {
                String xml = pageRequester.get(XML_URL_PREFIX + c.getCompanyId()).getPage();
                sqliteClient.addJobs(jobFinderPipeline.identifyInternships(xml, c));
            } catch (IOException e) {
                LOGGER.severe(String.format(
                        "Could not parse company %s, caused by: %s", c.getHumanReadableName(), e.getMessage()));
            }

        });
    }

}
