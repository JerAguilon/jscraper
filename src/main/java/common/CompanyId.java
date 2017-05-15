package common;

/**
 * The id of a company, containing a human-friendly name and jobvite-specific ID/name. Used in the
 * {@code CompanyIdPipeline} as an immutable store of data.
 */
public class CompanyId {

    private final String humanReadableName;
    private final String companyName;
    private final String companyId;

    public CompanyId(String companyName, String humanReadableName, String companyId) {
        this.humanReadableName = humanReadableName;
        this.companyName = companyName;
        this.companyId = companyId;
    }

    public String getHumanReadableName() {
        return humanReadableName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyId() {
        return companyId;
    }
}
