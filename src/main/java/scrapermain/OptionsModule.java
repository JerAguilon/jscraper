package scrapermain;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Sets up the command line arguments for the scraper. Consumed by {@code ScraperMain}.
 */
public class OptionsModule {

    private static final String RUNNING_MODE_DESCRIPTION =
        "The running mode of the pipelines:\n\n" +
                "FULL_RUN: The entire DB will be cleared and companies will be parsed from the input list\n" +
                "RETAIN_COMPANIES (default): Companies will be retained, but their jobs will be rescraped\n" +
                "RETAIN_JOBS: Companies and job ids will be retained, but the fields themselves will be rescraped.\n\t" +
                "Useful if you experience a pipeline failure and just want to rerun without losing work";

    public static Options getOptions() {
        Options options = new Options();
        options.addOption(
                Option.builder("running_mode").argName("running mode").desc(RUNNING_MODE_DESCRIPTION).hasArg().build());
        return options;
    }
}
