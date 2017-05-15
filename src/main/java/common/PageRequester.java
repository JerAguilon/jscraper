package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 * Helper class to make requests on a given url. Returns the body as a string.
 */
public class PageRequester {
    private static final Logger LOGGER =
            Logger.getLogger(PageRequester.class.getName());


    public static class Page {

        private final String page;
        private final String responseUrl;

        public Page(String page, String responseUrl) {
            this.page = page;
            this.responseUrl = responseUrl;
        }

        public String getPage() {
            return page;
        }

        public String getResponseUrl() {
            return responseUrl;
        }

    }

    public Page get(String link) throws IOException {
        LOGGER.info("Querying: " + link);
        StringBuilder page = new StringBuilder();
        URL url = new URL(link);
        HttpURLConnection conn = ((HttpURLConnection) url.openConnection());
        String str = conn.getURL().toString();
        url.openStream();
        conn.getInputStream();
        BufferedReader rd =
                new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
        String line;
        while ((line = rd.readLine()) != null) {
            page.append(line);
        }

        rd.close();
        return new Page(page.toString(), conn.getURL().toString());
    }
}
