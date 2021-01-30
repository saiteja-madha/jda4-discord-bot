package bot.utils;

import bot.Config;
import bot.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.natanbc.reliqua.request.RequestException;
import me.duncte123.botcommons.web.WebParserUtils;
import me.duncte123.botcommons.web.WebUtils;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
    private final static String TRANSLATE_URL = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=%s&dt=t&q=%s";
    private final static String GIST_URL = "https://api.github.com/gists";

    @Nullable
    public static String[] translate(String outputCode, String input) {
        ArrayNode outputJson = WebUtils.ins
                .getJSONArray(String.format(TRANSLATE_URL, outputCode, input))
                .execute();

        if (outputJson.get(0).get(0).isEmpty())
            return null;

        // Input & Output
        input = outputJson.get(0).get(0).get(1).asText();
        String output = outputJson.get(0).get(0).get(0).asText();

        // Input Language Code
        String inputCode = outputJson.get(2).asText();

        // Input & Output Languages
        String inputLanguage = MiscUtils.getLanguage(inputCode);
        String outputLanguage = Constants.langCodes.get(outputCode);

        return new String[]{inputCode, outputCode, inputLanguage, outputLanguage, input, output};

    }

    public static String postLogsToGist(String postBody) {
        RequestBody rBody = RequestBody.create(MediaType.parse("application/json"), postBody);

        Request request = new Request.Builder().url(GIST_URL)
                .addHeader("Authorization", "Bearer " + Config.get("GITHUB_HEADER"))
                .post(rBody)
                .build();
        try {
            final ObjectNode json = WebUtils.ins.prepareRaw(request,
                    (r) -> WebParserUtils.toJSONObject(r, new ObjectMapper())).execute();
            try {
                String url = json.get("html_url").asText();
                url += "#azazel-ticket-logs";
                return url;
            } catch (Exception ex) {
                LOGGER.error("Gist response error: " + json);
            }

        } catch (RequestException ex) {
            LOGGER.error("Post to Gist Failed: " + ex.getMessage());
        }

        return null;

    }

}
