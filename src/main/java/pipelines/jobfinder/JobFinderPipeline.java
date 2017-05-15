package pipelines.jobfinder;

import com.google.common.collect.ImmutableSet;
import common.CompanyData;
import common.CompanyData.Job;
import common.CompanyId;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pipeline that takes a given company and associates it with a list of internships.
 */
public class JobFinderPipeline  {

    private static final Logger LOGGER =
        Logger.getLogger(JobFinderPipeline.class.getName());

    private static final String GETTABLE_LINK_FORMAT = "http://jobs.jobvite.com/careers/%s/job/%s/apply";
    private static final Pattern JOB_ID_PATTERN = Pattern.compile("j=([^&]+)");
    private static final ImmutableSet<String> BANNED_KEYWORDS =
            ImmutableSet.of("INTERNAL", "INTERNATIONAL");
    private static final ImmutableSet<String> DESIRED_KEYWORDS =
            ImmutableSet.of("INTERN", "INTERNSHIP");

    public CompanyData identifyInternships(String xml, CompanyId companyId) {
        LOGGER.info(String.format("[%s] Finding internships.", companyId.getHumanReadableName()));
        CompanyData companyData = new CompanyData(companyId);

        Document doc;
        try {
             doc = loadXMLFromString(xml);
        } catch (Exception e) {
            LOGGER.severe(String.format("Unable to parse xml:\n%s", e.getMessage()));
            return companyData; // swallow and just don't add internships
        }

        doc.getDocumentElement().normalize();
        NodeList jobs = doc.getElementsByTagName("job");

        for (int i = 0; i < jobs.getLength(); i++) {
            Node node = jobs.item(i);
            if (node instanceof Element) {
                Element element = (Element) node;
                String category = getFirstText(element.getElementsByTagName("category")).trim();
                String title = getFirstText(element.getElementsByTagName("title")).trim();
                String link = getFirstText(element.getElementsByTagName("apply-url")).trim();
                String description = getFirstText(element.getElementsByTagName("description")).trim();
                String location = getFirstText(element.getElementsByTagName("location")).trim();
                Matcher jobIdMatcher = JOB_ID_PATTERN.matcher(link);

                boolean banned = BANNED_KEYWORDS.stream().anyMatch(
                        k -> category.toUpperCase().contains(k) || title.toUpperCase().contains(k));
                if (banned) {
                    continue;
                }
                boolean desired = DESIRED_KEYWORDS.stream().anyMatch(
                        d -> category.toUpperCase().contains(d) || title.toUpperCase().contains(d));

                if (desired && jobIdMatcher.find()) {
                    String jobId = jobIdMatcher.group(1);

                    // problem on jobvite's end means that the link above rarely works. I'm manually constructing a working
                    // one that you can safely GET request
                    String gettableLink = String.format(GETTABLE_LINK_FORMAT, companyId.getCompanyName(), jobId);
                    companyData.addJob(new Job(location, title, description, gettableLink, jobIdMatcher.group(1)));
                }
            }
        }

        LOGGER.info(String.format("Found %s internships", companyData.getJobs().size()));
        return companyData;
    }

    private static String getFirstText(NodeList list) {
        return list.item(0).getTextContent();
    }

    private static Document loadXMLFromString(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }
}
