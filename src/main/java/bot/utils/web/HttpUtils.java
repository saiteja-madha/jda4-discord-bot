package bot.utils.web;

import bot.Constants;
import bot.utils.MiscUtils;
import com.fasterxml.jackson.databind.node.ArrayNode;
import me.duncte123.botcommons.web.WebUtils;
import org.jetbrains.annotations.Nullable;

public class HttpUtils {

    private final static String TRANSLATE_URL = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=%s&dt=t&q=%s";

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

}
