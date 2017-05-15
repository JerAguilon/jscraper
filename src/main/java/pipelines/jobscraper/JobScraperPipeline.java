package pipelines.jobscraper;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import common.Field;
import common.PageRequester;
import common.SqlJob;
import database.SqliteClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pipeline that scrapes a job for its given fields.
 */
public class JobScraperPipeline {

    private static final Logger LOGGER =
            Logger.getLogger(JobScraperPipeline.class.getName());

    @VisibleForTesting
    static final Pattern CANDIDATE_FIELD_PATTERN = Pattern.compile("(?:(\\{)(?:[\\s]*applyFields:))", Pattern.DOTALL);

    private final SqliteClient sqliteClient;
    private final PageRequester pageRequester;

    public JobScraperPipeline(SqliteClient sqliteClient, PageRequester pageRequester) {
        this.sqliteClient = sqliteClient;
        this.pageRequester = pageRequester;
    }

    public int scrapeJobs(List<SqlJob> jobs) {
        int counter = 0;
        for (SqlJob job : jobs) {
            sqliteClient.setJobHealthy(job, false);

            if (sqliteClient.hasFields(job)) {
                LOGGER.info(String.format("Job %s has already been parsed. Skipping.", job.getEid()));
                sqliteClient.setJobHealthy(job, true);
                counter++;
                continue;
            }

            try {
                String page = pageRequester.get(job.getApplyUrl()).getPage();
                Matcher candidateFieldMatcher = CANDIDATE_FIELD_PATTERN.matcher(page);
                if (!candidateFieldMatcher.find()) {
                    continue;
                }
                String jsonString = getJsonString(page, candidateFieldMatcher.start(1));

                ImmutableList.Builder<String> jsonKeys = ImmutableList.builder();

                JSONObject fieldsJson = new JSONObject(jsonString);
                List<Field> candidateFields = getCandidateFields(fieldsJson);
                List<Field> eeoFields = getEeoFields(fieldsJson);
                JSONObject screeningJson = getScreeningJson(fieldsJson);

                boolean healthy = sqliteClient.addFields(job, candidateFields, eeoFields, screeningJson);
                if (healthy) {
                    counter++;
                    sqliteClient.setJobHealthy(job, true);
                }
            } catch (IOException | JSONException e) {
                LOGGER.severe(String.format(
                        "[%s] Could not parse job %s because: %s",
                        job.getApplyUrl(),
                        job.getTitle(),
                        e.getMessage()));
                e.printStackTrace();
            }
        }
        return counter;
    }

    private JSONObject getScreeningJson(JSONObject fieldsJson) {
        try {
            JSONObject screeningJson = new JSONObject();
            if (fieldsJson.has("ofccpFields")) {
                screeningJson.put("ofccpFields", fieldsJson.get("ofccpFields"));
            }
            if (fieldsJson.has("preScreeningFormFields")) {
                screeningJson.put("preScreeningFormFields", fieldsJson.get("preScreeningFormFields"));
            }
            return screeningJson;
        } catch(JSONException e) {
            throw new IllegalArgumentException("Unreachable line", e);
        }
    }

    private List<Field> getCandidateFields(JSONObject fields) throws JSONException {
        ImmutableList.Builder<Field> convertedFields = ImmutableList.builder();
        JSONArray applyFields = fields.getJSONArray("applyFields");
        for (int i = 0; i < applyFields.length(); i++) {
            JSONObject jsonObject = applyFields.getJSONObject(i);
            if (jsonObject.has("fieldType")) {
                convertedFields.add(transformToField(jsonObject));
            }

        }
        return convertedFields.build();
    }

    private List<Field> getEeoFields(JSONObject fields) throws JSONException {
        ImmutableList.Builder<Field> convertedFields = ImmutableList.builder();
        if (fields.has("eeoFields")) {
            JSONArray eeoFields = fields.getJSONArray("eeoFields");
            for (int i = 0; i < eeoFields.length(); i++) {
                JSONObject jsonObject = eeoFields.getJSONObject(i);
                if (jsonObject.has("fieldType")) {
                    convertedFields.add(transformToField(jsonObject));
                }
            }
        }
        return convertedFields.build();
    }

    private static Field transformToField(JSONObject jsonObject) throws JSONException {
        String fieldType = jsonObject.getString("fieldType");
        String id =
                jsonObject.has("viewFieldEId")
                        ? jsonObject.getString("viewFieldEId")
                        : jsonObject.getString("fieldId");
        String name = jsonObject.getString("name");
        boolean required = jsonObject.getBoolean("required");
        boolean multiselect = jsonObject.getBoolean("multiSelect");
        Map<String, String> values = ImmutableMap.of();

        if (jsonObject.has("values")) {
            ImmutableMap.Builder<String, String> valuesBuilder = ImmutableMap.builder();
            JSONArray jsonValues = jsonObject.getJSONArray("values");
            for (int i = 0; i < jsonValues.length(); i++) {
                JSONObject value = jsonValues.getJSONObject(i);
                valuesBuilder.put(value.getString("key"), value.getString("value"));
            }
            values = valuesBuilder.build();
        }
        return Field
                .builder()
                .setFieldType(fieldType)
                .setId(id)
                .setName(name)
                .setRequired(required)
                .setMultiselect(multiselect)
                .setValues(values)
                .build();
    }

    /**
     * Simple method that starts at the beginning of a json object and loops until the terminating character of a json
     * object is found. Essentially this happens when we have encountered an equal number of '{' and '}' chars. A stack
     * structure is convenient and is abstracted through an int where each '{' denotes pushing to the stack and '}'
     * denotes popping.
     */
    private static String getJsonString(String page, int startIndex) {
        int stack = 1;
        int index = startIndex + 1;
        while (stack != 0) {
            if (page.charAt(index) == '{') {
                stack++;
            } else if (page.charAt(index) == '}') {
                stack--;
            }
            index++;
        }
        return page.substring(startIndex, index);
    }
}
