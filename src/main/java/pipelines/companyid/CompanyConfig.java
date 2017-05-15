package pipelines.companyid;


/**
 * A storage class representing a human-friendly name and a jobvite name, parsed from the input csv file in the
 * {@code CompanyIdPipeline}.
 */
public class CompanyConfig {
    private final String humanReadableName;
    private final String companyName;

    public CompanyConfig(String humanReadableName, String companyName) {
        this.humanReadableName = humanReadableName;
        this.companyName = companyName;
    }

    public String getHumanReadableName() {
        return humanReadableName;
    }

    public String getCompanyName() {
        return companyName;
    }
}
