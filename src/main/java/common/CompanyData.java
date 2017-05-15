package common;

import java.util.ArrayList;
import java.util.List;

/**
 * Container class for an {@link CompanyId} and its corresponding jobs. Used in the {@code JobFinderPipeline} as an
 * immutable store of a company's jobs.
 */
public class CompanyData {

    private final CompanyId company;
    private final List<Job> jobs;

    public static class Job {
        private final String location;
        private final String title;
        private final String description;
        private final String link;
        private final String jobId;

        @Override
        public String toString() {
            return "Job{" +
                    "location='" + location + '\'' +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", link='" + link + '\'' +
                    ", jobId='" + jobId + '\'' +
                    '}';
        }

        public Job(String location, String title, String description, String link, String jobId) {
            this.location = location;
            this.title = title;
            this.description = description;
            this.link = link;
            this.jobId = jobId;
        }

        public String getLocation() {
            return location;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public String getLink() {
            return link;
        }

        public String getJobId() {
            return jobId;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof Job) {
                Job that = (Job) other;
                return this.location.equals(that.location)
                                && this.title.equals(that.title)
                                && this.description.equals(that.description)
                                && this.link.equals(that.link);
            }
            return false;
        }
    }

    public CompanyData(CompanyId company) {
        this.company = company;
        this.jobs = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "CompanyData{" +
                "company=" + company +
                ", jobs=" + jobs +
                '}';
    }

    public void addJob(Job job) {
        jobs.add(job);
    }

    public CompanyId getCompany() {
        return company;
    }

    public List<Job> getJobs() {
        return jobs;
    }
}


