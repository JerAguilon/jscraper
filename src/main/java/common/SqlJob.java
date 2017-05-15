package common;

/**
 * SQL representation of a job.
 */
public class SqlJob {

    private final String eid;
    private final String jobId;
    private final String title;
    private final String applyUrl;

    public SqlJob(String eid, String jobId, String title, String applyUrl) {
        this.eid = eid;
        this.jobId = jobId;
        this.title = title;
        this.applyUrl = applyUrl;
    }

    public String getEid() {
        return eid;
    }

    public String getJobId() {
        return jobId;
    }

    public String getTitle() {
        return title;
    }

    public String getApplyUrl() {
        return applyUrl;
    }

    @Override
    public String toString() {
        return "SqlJob{" +
                "eid='" + eid + '\'' +
                ", jobId='" + jobId + '\'' +
                ", title='" + title + '\'' +
                ", applyUrl='" + applyUrl + '\'' +
                '}';
    }
}
