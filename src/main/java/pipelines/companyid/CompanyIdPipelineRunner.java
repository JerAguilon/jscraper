package pipelines.companyid;

import com.google.common.collect.ImmutableList;
import common.CompanyId;
import database.SqliteClient;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Runs the {@link CompanyIdPipeline}.
 */
public class CompanyIdPipelineRunner {

    private static final String COMPANY_SUFFIX_REGEX = "(\\.com|\\.org|\\.co|\\.edu|\\.gov|\\.co\\.uk|\\.net)";
    private static final Logger LOGGER =
            Logger.getLogger(CompanyIdPipelineRunner.class.getName());

    private final CompanyIdPipeline companyIdPipeline;

    public CompanyIdPipelineRunner(CompanyIdPipeline companyIdPipeline) {
        this.companyIdPipeline = companyIdPipeline;
    }

    public void run(String inputSifteryFile) {
        try {
            List<CompanyConfig> companies = getCompanies(inputSifteryFile);
            List<CompanyId> companyIds = companyIdPipeline.getIdsToQuery(companies);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    private static List<CompanyConfig> getCompanies(String inputFile) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(inputFile));

        ImmutableList.Builder<CompanyConfig> companies = ImmutableList.builder();
        String line;
        line = in.readLine(); // skip the first line since it's a header
        while((line = in.readLine()) != null)
        {
            String[] columns = line.split(",");
            String companyName = columns[1].replaceFirst(COMPANY_SUFFIX_REGEX, "");
            String humanReadableName = columns[0];
            companies.add(new CompanyConfig(humanReadableName, companyName));
        }
        return companies.build();
    }

}
