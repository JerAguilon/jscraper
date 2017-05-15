package scrapermain;

/**
 * Policy enum for which pipelines will be run/what data will be deleted before running.
 */
public enum RunningMode {
    // the whole DB will be cleared, so all companies in the input csv file will be queried from scratch.
    // not normally needed unless the list is changed or a fresh DB is being used.
    FULL_RUN,
    // all pipelines will run, but companies that have been discovered will not be deleted
    RETAIN_COMPANIES,
    // additional jobs will not be discovered, and scraping will automatically start. Useful if your pipeline fails
    // while scraping and you want to rerun the scraping step without losing work.
    RETAIN_JOBS
}
