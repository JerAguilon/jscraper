package scrapermain;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;

import java.util.logging.Logger;

public class ScraperMain {
    private static final String RETAIN_COMPANIES_STRING = RunningMode.RETAIN_COMPANIES.name();

    private static final Logger LOGGER =
            Logger.getLogger(ScraperMain.class.getName());

    public static void main(String[] args) throws ParseException {
        CommandLineParser commandLineParser = new DefaultParser();

        CommandLine cmdArgs = commandLineParser.parse(OptionsModule.getOptions(), args);

        RunningMode runningMode = RunningMode.valueOf(cmdArgs.getOptionValue("running_mode", RETAIN_COMPANIES_STRING));

        try(RunningModeManager runningModeManager = RunningModeManager.create()) {
            runningModeManager.run(runningMode);
        } catch (Exception e) {
            // swallow all exceptions and log them to not crash the program
            LOGGER.severe(e.getMessage());
            e.printStackTrace();
        }

    }

}
