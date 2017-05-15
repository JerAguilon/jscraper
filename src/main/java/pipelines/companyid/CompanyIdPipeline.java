package pipelines.companyid;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import common.CompanyId;
import common.PageRequester;
import common.PageRequester.Page;
import database.SqliteClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pipeline that identifies companies by querying the jobvite page and parsing the company eid.
 */
public class CompanyIdPipeline {

    private static final Logger LOGGER =
            Logger.getLogger(CompanyIdPipeline.class.getName());

    @VisibleForTesting
    static final Pattern CANDIDATE_FIELD_PATTERN = Pattern.compile("companyEId:[\\s]*'([^']*)'{1}?");

    private static final String URL_PREFIX = "https://jobs.jobvite.com/";
    private static final String FAILED_COMPANY_QUERY_STRING = "invalid";

    private final SqliteClient sqliteClient;
    private final PageRequester pageRequester;

    public CompanyIdPipeline(SqliteClient sqliteClient, PageRequester pageRequester) {
        this.sqliteClient = sqliteClient;
        this.pageRequester = pageRequester;
    }

    /**
     * Converts a list of companies names to a list of {@link CompanyId} associated with the companies. This method
     * is functional programming as fuck.
     *
     * @param companies a list of companies from human readable form to jobvite-compatible form
     * @return a list of {@link CompanyId} to query
     */
    public List<CompanyId> getIdsToQuery(List<CompanyConfig> companies) {
        List<CompanyId> ids = companies
                .stream()
                .filter(company -> isNotPresent(company))
                .map(this::queryPage)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(CompanyIdPipeline::getId)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(entry -> new CompanyId(
                        entry.getKey().getCompanyName(), entry.getKey().getHumanReadableName(), entry.getValue()))
                .collect(ImmutableList.toImmutableList());
        ids.forEach(sqliteClient::addCompany);
        LOGGER.info(String.format("EIds successfully parsed."));
        return ids;
    }

    private boolean isNotPresent(CompanyConfig company) {
        if (sqliteClient.hasCompany(company.getCompanyName())) {
            LOGGER.fine(String.format("Company %s has already been added. Skipping", company.getCompanyName()));
            return false;
        }
        return true;
    }

    private Optional<Entry<CompanyConfig, String>> queryPage(CompanyConfig companyConfig) {
        LOGGER.info("Starting eid parsing for company: " + companyConfig.getCompanyName());
        try {
            Page page = pageRequester.get(URL_PREFIX + companyConfig.getCompanyName());
            if (!page.getResponseUrl().contains(FAILED_COMPANY_QUERY_STRING)) {
                LOGGER.fine(String.format("Found page for %s", companyConfig.getCompanyName()));
                return Optional.of(Maps.immutableEntry(companyConfig, page.getPage()));
            }
            LOGGER.info(String.format("Could not access company %s", companyConfig.getCompanyName()));
        } catch (IOException e) {
            LOGGER.severe(String.format("Company %s failed due to: %s", companyConfig.getCompanyName(), e.getMessage()));
            // fall through
        }
        return Optional.empty();
    }

    private static Optional<Entry<CompanyConfig, String>> getId(Entry<CompanyConfig, String> companyToPage) {
        Matcher matcher = CANDIDATE_FIELD_PATTERN.matcher(companyToPage.getValue());

        if (matcher.find()) {
            String eid = matcher.group(1);
            return Optional.of(Maps.immutableEntry(companyToPage.getKey(), eid));
        }
        return Optional.empty();
    }
}