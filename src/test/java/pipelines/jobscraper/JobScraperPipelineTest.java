package pipelines.jobscraper;

import common.PageRequester;
import common.PageRequester.Page;
import database.SqliteClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;

import static org.junit.Assert.assertTrue;
import static pipelines.jobscraper.JobScraperPipeline.CANDIDATE_FIELD_PATTERN;

@RunWith(MockitoJUnitRunner.class)
public class JobScraperPipelineTest {

    private static final String COMPANY_ID = "abc123";
    private static final String COMPANY_NAME = "foo_bar_corp";
    private static final String JOB_LOCATION = "ATL";
    private static final String JOB_TITLE = "CEO";
    private static final String JOB_DESCRIPTION = "job description";
    private static final String JOB_LINK = "joblink.com";
    private static final String JOB_ID = "job-id";

    private Page page;

    @Mock
    private SqliteClient sqliteClient;

    @Mock
    private PageRequester pageRequester;

    private JobScraperPipeline pipeline;

    @Before
    public void setUp() throws Exception {
        pipeline = new JobScraperPipeline(sqliteClient, pageRequester);
        page = new Page(new String(Files.readAllBytes(Paths.get("src/test/java/pipelines/jobscraper/testFile.html"))), "");

        //Mockito.when(pageRequester.get(JOB_LINK)).thenReturn(page);
    }

    @Test
    public void fieldPatternTest() {
        Matcher candidateFields = CANDIDATE_FIELD_PATTERN.matcher(page.getPage());
        assertTrue(candidateFields.find());
    }


    @Test
    public void scrapeCompany_oneJob() throws Exception {
        /*CompanyData company = new CompanyData(new CompanyId(COMPANY_NAME, COMPANY_NAME, COMPANY_ID));
        CompanyData.Job job = new CompanyData.Job(JOB_LOCATION, JOB_TITLE, JOB_DESCRIPTION, JOB_LINK, JOB_ID);
        company.addJob(job);

        Field field1 =
                Field.builder()
                        .setRequired(true)
                        .setId("id1")
                        .setFieldType("Name")
                        .setName("First Name")
                        .setMultiselect(false)
                        .setValues(ImmutableMap.of())
                        .build();
        Field field2 =
                Field.builder()
                        .setRequired(true)
                        .setId("id2")
                        .setFieldType("Date")
                        .setName("Start date:")
                        .setMultiselect(false)
                        .setValues(ImmutableMap.of())
                        .build();

        List<Field> expected = ImmutableList.of(field1, field2);

        Map<CompanyData.Job, List<Field>> output = pipeline.scrapeJobs(company);
        assertEquals(output.get(job), expected);*/
    }

}