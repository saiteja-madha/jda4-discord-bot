package bot;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Constants {

    public final static Map<String, String> langCodes;
    public final static Map<String, String> flagCodes;

    public final static String CUBE_BULLET = "\u2752";
    public final static String CIRCLE_BULLET = "\u2022";
    public final static String ARROW_BULLET = "\u00BB";
    public final static String ARROW = "\u276F";
    public final static String TICK = "\u2713";
    public final static String X_MARK = "\u2715";

    static {

        langCodes = new LinkedHashMap<>(55);
        langCodes.put("af", "Afrikaans");
        langCodes.put("sq", "Albanian");
        langCodes.put("ar", "Arabic");
        langCodes.put("bn", "Bengali");
        langCodes.put("bg", "Bulgarian");
        langCodes.put("ca", "Catalan");
        langCodes.put("hr", "Croatian");
        langCodes.put("cs", "Czech");
        langCodes.put("da", "Danish");
        langCodes.put("nl", "Dutch");
        langCodes.put("en", "English");
        langCodes.put("et", "Estonian");
        langCodes.put("tl", "Filipino");
        langCodes.put("fi", "Finnish");
        langCodes.put("fr", "French");
        langCodes.put("de", "German");
        langCodes.put("el", "Greek");
        langCodes.put("gu", "Gujarati");
        langCodes.put("hi", "Hindi");
        langCodes.put("hu", "Hungarian");
        langCodes.put("id", "Indonesian");
        langCodes.put("it", "Italian");
        langCodes.put("ja", "Japanese");
        langCodes.put("kn", "Kannada");
        langCodes.put("ko", "Korean");
        langCodes.put("lv", "Latvian");
        langCodes.put("lt", "Lithuanian");
        langCodes.put("mk", "Macedonian");
        langCodes.put("ml", "Malayalam");
        langCodes.put("mr", "Marathi");
        langCodes.put("ne", "Nepali");
        langCodes.put("no", "Norwegian");
        langCodes.put("fa", "Persian");
        langCodes.put("pl", "Polish");
        langCodes.put("pt", "Portuguese");
        langCodes.put("pa", "Punjabi");
        langCodes.put("ro", "Romanian");
        langCodes.put("ru", "Russian");
        langCodes.put("sk", "Slovak");
        langCodes.put("sl", "Slovenian");
        langCodes.put("so", "Somali");
        langCodes.put("es", "Spanish");
        langCodes.put("sw", "Swahili");
        langCodes.put("sv", "Swedish");
        langCodes.put("ta", "Tamil");
        langCodes.put("te", "Telugu");
        langCodes.put("th", "Thai");
        langCodes.put("tr", "Turkish");
        langCodes.put("uk", "Ukrainian");
        langCodes.put("ur", "Urdu");
        langCodes.put("vi", "Vietnamese");
        langCodes.put("cy", "Welsh");
        langCodes.put("he", "Hebrew");
        langCodes.put("zh-cn", "Chinese (Simplified)");
        langCodes.put("zh-tw", "Chinese (Traditional)");

        flagCodes = new HashMap<>(26);
        flagCodes.put("U+1F1E6", "A");
        flagCodes.put("U+1F1E7", "B");
        flagCodes.put("U+1F1E8", "C");
        flagCodes.put("U+1F1E9", "D");
        flagCodes.put("U+1F1EA", "E");
        flagCodes.put("U+1F1EB", "F");
        flagCodes.put("U+1F1EC", "G");
        flagCodes.put("U+1F1ED", "H");
        flagCodes.put("U+1F1EE", "I");
        flagCodes.put("U+1F1EF", "J");
        flagCodes.put("U+1F1F0", "K");
        flagCodes.put("U+1F1F1", "L");
        flagCodes.put("U+1F1F2", "M");
        flagCodes.put("U+1F1F3", "N");
        flagCodes.put("U+1F1F4", "O");
        flagCodes.put("U+1F1F5", "P");
        flagCodes.put("U+1F1F6", "Q");
        flagCodes.put("U+1F1F7", "R");
        flagCodes.put("U+1F1F8", "S");
        flagCodes.put("U+1F1F9", "T");
        flagCodes.put("U+1F1FA", "U");
        flagCodes.put("U+1F1FB", "V");
        flagCodes.put("U+1F1FC", "W");
        flagCodes.put("U+1F1FD", "X");
        flagCodes.put("U+1F1FE", "Y");
        flagCodes.put("U+1F1FF", "Z");
    }

}
