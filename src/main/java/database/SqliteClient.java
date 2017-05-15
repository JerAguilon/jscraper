package database;
import com.google.common.collect.ImmutableList;
import common.CompanyData;
import common.CompanyData.Job;
import common.CompanyId;
import common.Field;
import common.SqlJob;
import org.json.JSONObject;

import java.sql.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Client for the sqlite server. Should be injected into the pipelines to add companies/jobs/fields
 *
 * TODO: verify whether the client is thread safe or not
 */
public class SqliteClient {

    private static final Logger LOGGER =
            Logger.getLogger(SqliteClient.class.getName());
    private static final String DRIVER = "org.sqlite.JDBC";
    private static final String DATABASE_URL_PREFIX =
            "jdbc:sqlite:";

    private static final String EMPTY_JSON_STRING = "{}";

    private static final String INSERT_COMPANY_QUERY =
            "INSERT into companies VALUES ('%s', '%s', '%s')";
    private static final String LOOKUP_COMPANY_QUERY =
            "SELECT 1 FROM companies WHERE name='%s'";
    private static final String LIST_COMPANIES_QUERY =
            "SELECT * FROM companies";

    private static final String CLEAR_JOBS_QUERY = "DELETE FROM jobs";
    private static final String INSERT_JOB_QUERY =
            "INSERT into jobs VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String LOOKUP_JOB_QUERY =
            "SELECT 1 FROM jobs WHERE job_id='%s' AND eid='%s'";
    private static final String LIST_JOBS_QUERY =
            "SELECT * FROM jobs";
    private static final String UPDATE_JOB_HEALTH_QUERY =
            "UPDATE jobs SET is_healthy=? WHERE eid=? AND job_id=?";

    private static final ImmutableList<String> CLEAR_FIELDS_QUERIES =
            ImmutableList.of("DELETE FROM fields", "DELETE FROM eeo_fields", "DELETE FROM json_fields");
    private static final String INSERT_CANDIDATE_FIELDS_QUERY =
            "INSERT INTO fields VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String LOOKUP_FIELDS_QUERY =
            "SELECT 1 FROM fields WHERE eid=? AND job_id=?";
    private static final String DELETE_FIELDS_QUERY =
            "DELETE FROM fields WHERE eid=? AND job_id=?";

    private static final String INSERT_JSON_QUERY =
            "INSERT INTO json_fields VALUES(?, ?, ?)";
    private static final String DELETE_JSON_FIELDS_QUERY =
            "DELETE FROM json_fields WHERE eid=? AND job_id=?";

    private static final String INSERT_EEO_FIELDS_QUERY =
            "INSERT INTO eeo_fields VALUES(?, ?, ?, ?, ?, ?, ?, ?)";

    private final Connection conn;
    // thread safe counter for the number of EIds found.
    private AtomicInteger eidsFound = new AtomicInteger();
    // thread safe counter for the number of jobs found.
    private AtomicInteger jobsFound = new AtomicInteger();
    // thread safe counter for the number of healthy jobs found.
    private AtomicInteger healthyJobs = new AtomicInteger();

    public SqliteClient(String databaseLocation) throws SQLException, ClassNotFoundException {
        Class.forName(DRIVER);

        this.conn = DriverManager.getConnection(
                DATABASE_URL_PREFIX + databaseLocation);
    }

    public List<CompanyId> listCompanies() {
        Statement st = null;
        try {
            ImmutableList.Builder<CompanyId> companyIds = ImmutableList.builder();
            st = conn.createStatement();
            ResultSet resultSet = st.executeQuery(LIST_COMPANIES_QUERY);
            while(resultSet.next()){
                String name = resultSet.getString("name");
                String humanReadableName = resultSet.getString("human_name");
                String eid = resultSet.getString("eid");
                companyIds.add(new CompanyId(name, humanReadableName, eid));
            }
            resultSet.close();
            st.close();
            return companyIds.build();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            return ImmutableList.of();
        }
    }

    public void addCompany(CompanyId companyId) {
        Statement st = null;
        try {
            st = conn.createStatement();
            st.executeUpdate(String.format(
                    INSERT_COMPANY_QUERY,
                    companyId.getHumanReadableName(),
                    companyId.getCompanyName(),
                    companyId.getCompanyId()));
            st.close();
            LOGGER.info(String.format("Added %s to the database", companyId.getCompanyName()));
            eidsFound.incrementAndGet();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
        }
    }

    public boolean hasCompany(String companyName) {
        Statement st = null;
        try {
            st = conn.createStatement();
            ResultSet resultSet = st.executeQuery(String.format(
                    LOOKUP_COMPANY_QUERY,
                    companyName));
            int rowCount = 0;
            while(resultSet.next()){
                rowCount++;
            }
            resultSet.close();
            st.close();
            return rowCount != 0;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
        }
        return false;
    }

    public void clearJobs() {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(CLEAR_JOBS_QUERY);
            st.executeUpdate();
            st.close();
            LOGGER.info("All jobs cleared");
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            throw new IllegalStateException("Couldn't clear fields. Pipelines aren't safe to run", e);
        }
    }

    public List<SqlJob> listJobs() {
        Statement st = null;
        try {
            ImmutableList.Builder<SqlJob> jobs = ImmutableList.builder();
            st = conn.createStatement();
            ResultSet resultSet = st.executeQuery(LIST_JOBS_QUERY);
            while(resultSet.next()){
                String eid = resultSet.getString("eid");
                String jobId = resultSet.getString("job_id");
                String title = resultSet.getString("title");
                String applyUrl = resultSet.getString("apply_url");
                jobs.add(new SqlJob(eid, jobId, title, applyUrl));
            }
            resultSet.close();
            st.close();
            return jobs.build();
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
            return ImmutableList.of();
        }
    }

    public void addJobs(CompanyData companyData) {
        int jobCount = 0;
        for (Job job : companyData.getJobs()) {

            if (hasJob(job, companyData.getCompany().getCompanyId())) {
                LOGGER.info(String.format(
                        "[%s]: %s is already in the DB.",
                        companyData.getCompany().getHumanReadableName(),
                        job.getTitle()));
                continue;

            }
            PreparedStatement st = null;
            try {
                st = conn.prepareStatement(INSERT_JOB_QUERY);
                st.setString(1, companyData.getCompany().getCompanyId());
                st.setString(2, job.getJobId());
                st.setString(3, job.getLocation());
                st.setString(4, job.getTitle());
                st.setString(5, job.getDescription());
                st.setString(6, job.getLink());
                st.setInt(7, 0); // jobs are assumed to be unhealthy until scraped

                st.executeUpdate();
                st.close();
                LOGGER.info(String.format(
                        "[%s]: Added %s to the database",
                        companyData.getCompany().getHumanReadableName(),
                        job.getTitle()));
                jobsFound.incrementAndGet();
            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
            }
        }
    }

    public boolean hasJob(Job job, String eid) {
        Statement st = null;
        try {
            st = conn.createStatement();
            ResultSet resultSet = st.executeQuery(String.format(
                    LOOKUP_JOB_QUERY,
                    job.getJobId(),
                    eid));
            int rowCount = 0;
            while(resultSet.next()){
                rowCount++;
            }
            resultSet.close();
            st.close();
            return rowCount != 0;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
        }
        return false;
    }

    public void setJobHealthy(SqlJob job, boolean healthy) {
            if (healthy) {
                healthyJobs.incrementAndGet();
            }
            PreparedStatement st = null;
            try {
                st = conn.prepareStatement(UPDATE_JOB_HEALTH_QUERY);
                st.setInt(1, healthy? 1 : 0);
                st.setString(2, job.getEid());
                st.setString(3, job.getJobId());
                st.executeUpdate();
                st.close();
                LOGGER.fine(String.format(
                        "[%s]: Table health set to %s",
                        job.getApplyUrl(),
                        healthy ? "healthy" : "unhealthy"));
            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
            }
    }

    public void clearFields() {
        CLEAR_FIELDS_QUERIES.forEach(query -> {
            PreparedStatement st = null;
            try {
                st = conn.prepareStatement(query);
                st.executeUpdate();
                st.close();
            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
                throw new IllegalStateException("Couldn't clear fields. Pipelines aren't safe to run", e);
            }
        });
        LOGGER.info(String.format("All fields cleared"));
    }


    public boolean addFields(SqlJob sqliteJob, List<Field> fields, List<Field> eeoFields, JSONObject screeningJson) {
        String jsonString = screeningJson.toString();
        PreparedStatement st = null;
        if (!jsonString.equals(EMPTY_JSON_STRING)) {
            try {
                st = conn.prepareStatement(INSERT_JSON_QUERY);
                st.setString(1, sqliteJob.getEid());
                st.setString(2, sqliteJob.getJobId());
                st.setString(3, jsonString);
                st.executeUpdate();
                st.close();
            } catch (SQLException e) {
                deleteAllFields(sqliteJob);
                LOGGER.severe(
                        String.format("[%s] Failed to output json for %s. Caused by: %s",
                                sqliteJob.getEid(),
                                sqliteJob.getJobId(),
                                e.getMessage()));
                return false;
            }
        }

        int counter = 0;
        for (Field field : fields) {
            try {
                st = conn.prepareStatement(INSERT_CANDIDATE_FIELDS_QUERY);
                st.setString(1, sqliteJob.getEid());
                st.setString(2, sqliteJob.getJobId());
                st.setString(3, field.getId());
                st.setString(4, field.getFieldType());
                st.setString(5, field.getName());
                JSONObject jsonObject = new JSONObject(field.getValues());
                st.setString(6, jsonObject.toString());
                st.setInt(7, field.isMultiselect() ? 1 : 0);
                st.setInt(8, field.isRequired() ? 1 : 0);
                st.executeUpdate();
                st.close();
            } catch (SQLException e) {
                deleteAllFields(sqliteJob);
                LOGGER.severe(
                        String.format("EID: %s, JOBID: %s, ID: %s. Failed to output. Caused by: %s",
                                sqliteJob.getEid(),
                                sqliteJob.getJobId(),
                                field.getId(),
                                e.getMessage()));
                return false;
            }
            counter++;
        }

        for (Field field : eeoFields) {
            try {
                st = conn.prepareStatement(INSERT_EEO_FIELDS_QUERY);
                st.setString(1, sqliteJob.getEid());
                st.setString(2, sqliteJob.getJobId());
                st.setString(3, field.getId());
                st.setString(4, field.getFieldType());
                st.setString(5, field.getName());
                JSONObject jsonObject = new JSONObject(field.getValues());
                st.setString(6, jsonObject.toString());
                st.setInt(7, field.isMultiselect() ? 1 : 0);
                st.setInt(8, field.isRequired() ? 1 : 0);
                st.executeUpdate();
                st.close();
            } catch (SQLException e) {
                deleteAllFields(sqliteJob);
                LOGGER.severe(
                        String.format("EID: %s, JOBID: %s, ID: %s. Failed to output. Caused by: %s",
                                sqliteJob.getEid(),
                                sqliteJob.getJobId(),
                                field.getId(),
                                e.getMessage()));
                return false;
            }
            counter++;
        }

        LOGGER.info(String.format(
                "[%s]: Added %d fields to the database. Extra JSON fields: %s",
                sqliteJob.getEid(),
                counter,
                jsonString.equals(EMPTY_JSON_STRING) ? "no" : "yes"));
        return true;
    }

    public void deleteAllFields(SqlJob sqliteJob) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(DELETE_FIELDS_QUERY);
            st.setString(1, sqliteJob.getEid());
            st.setString(2, sqliteJob.getJobId());
            st.executeUpdate();
            st.close();

            st = conn.prepareStatement(DELETE_JSON_FIELDS_QUERY);
            st.setString(1, sqliteJob.getEid());
            st.setString(2, sqliteJob.getEid());
        } catch (SQLException e) {
            // swallow
            LOGGER.severe(e.getMessage());
        }
    }

    public boolean hasFields(SqlJob sqliteJob) {
        PreparedStatement st = null;
        try {
            st = conn.prepareStatement(LOOKUP_FIELDS_QUERY);
            st.setString(1, sqliteJob.getEid());
            st.setString(2, sqliteJob.getJobId());
            ResultSet resultSet = st.executeQuery();
            int rowCount = 0;
            while(resultSet.next()){
                rowCount++;
            }
            resultSet.close();
            st.close();
            return rowCount != 0;
        } catch (SQLException e) {
            LOGGER.severe(e.getMessage());
        }
        return false;
    }

    public void close() throws SQLException {
        LOGGER.info("Newly found EIds: " + eidsFound.get());
        LOGGER.info("Newly added jobs: " + jobsFound.get());
        LOGGER.info("Number of healthy jobs " + healthyJobs.get());
        conn.close();
    }
}
