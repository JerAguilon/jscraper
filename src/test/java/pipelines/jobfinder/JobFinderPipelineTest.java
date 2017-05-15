package pipelines.jobfinder;

import com.google.common.collect.Iterables;
import common.CompanyData;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by jeremy on 4/11/17.
 */
public class JobFinderPipelineTest {

    private static final String COMPANY_NAME = "company-name";
    private static final String COMPANY_ID = "company-id";
    private static final String LOCATION = "USA";
    private static final String DESCRIPTION = "description";
    private static final String LINK = "applicationLink";

    JobFinderPipeline jobFinderPipeline;
    String inputString;

    @Before
    public void setUp() throws Exception{
        jobFinderPipeline = new JobFinderPipeline();
        inputString = new String(Files.readAllBytes(Paths.get("src/test/java/pipelines/jobfinder/testFile.xml")));
    }

    @Test
    public void identifyInternships() {
        /*CompanyData expected = new CompanyData(COMPANY_NAME, COMPANY_ID);
        CompanyData.Job job1 = new CompanyData.Job(LOCATION, "Intern position", DESCRIPTION, LINK);
        CompanyData.Job job2 = new CompanyData.Job(LOCATION, "Title", DESCRIPTION, LINK);
        expected.addJob(job1);
        expected.addJob(job2);
        CompanyData companyData = jobFinderPipeline.identifyInternships(inputString, COMPANY_NAME, COMPANY_ID);
        assertEquals(companyData.getCompany(), expected.getCompany());
        assertTrue(Iterables.elementsEqual(companyData.getJobs(), expected.getJobs()));
        System.out.println(companyData);*/
    }

}