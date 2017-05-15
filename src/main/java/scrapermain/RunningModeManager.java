package scrapermain;

import database.SqliteClient;

/**
 * Manager that completes actions based on the {@link RunningMode} of the scraper.
 */
public class RunningModeManager {

    private final SqliteClient sqliteClient;

    public RunningModeManager(SqliteClient sqliteClient) {
        this.sqliteClient = sqliteClient;
    }

    public void setMode(RunningMode runningMode) {
        switch (runningMode) {
            case RETAIN_COMPANIES:
                sqliteClient.clearJobs();
                sqliteClient.clearFields();
        }
    }

}
