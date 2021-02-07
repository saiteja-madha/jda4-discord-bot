package bot.commands.utility;

import bot.command.CommandCategory;
import bot.command.CommandContext;
import bot.command.ICommand;
import me.duncte123.botcommons.messaging.EmbedUtils;
import me.duncte123.botcommons.web.WebUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.List;

public class CovidCommand extends ICommand {

    private final static String COVID_URL = "https://coronavirus-19-api.herokuapp.com/countries/";

    public CovidCommand() {
        this.name = "covid";
        this.help = "get covid statistics in the specified country";
        this.minArgsCount = 1;
        this.usage = "<country>";
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
        this.category = CommandCategory.UTILS;
        this.cooldown = 5;
    }

    @Override
    public void handle(@NotNull CommandContext ctx) {
        final List<String> args = ctx.getArgs();
        String country = String.join(" ", args.subList(0, args.size()));


        WebUtils.ins.getText(COVID_URL + country).async((text) -> {
            if (text.equalsIgnoreCase("Country not found")) {
                ctx.reply("Invalid country `" + args.get(0) + "`");
                return;
            }

            JSONObject json = new JSONObject(text);

            final int cases = (int) json.get("cases");
            final int todayCases = (int) json.get("todayCases");
            final int casesPerOneMillion = (int) json.get("casesPerOneMillion");

            final int deaths = (int) json.get("deaths");
            final int todayDeaths = (int) json.get("todayDeaths");
            final int deathsPerOneMillion = (int) json.get("deathsPerOneMillion");

            final int active = json.get("active").toString().equals("null") ? 0 : (int) json.get("active");
            final int recovered = json.get("recovered").toString().equals("null") ? 0 : (int) json.get("recovered");
            final int critical = (int) json.get("critical");

            final int totalTests = (int) json.get("totalTests");
            final int testsPerOneMillion = (int) json.get("testsPerOneMillion");

            EmbedBuilder embed = EmbedUtils.getDefaultEmbed()
                    .setTitle("Covid Stats - " + country)
                    .addField("Cases", String.valueOf(cases), true)
                    .addField("Cases Today", String.valueOf(todayCases), true)
                    .addField("Cases Per Million", String.valueOf(casesPerOneMillion), true)

                    .addField("Deaths", String.valueOf(deaths), true)
                    .addField("Deaths Today", String.valueOf(todayDeaths), true)
                    .addField("Deaths Per Million", String.valueOf(deathsPerOneMillion), true)

                    .addField("Active Cases", String.valueOf(active), true)
                    .addField("Recovered Cases", String.valueOf(recovered), true)
                    .addField("Critical Cases", String.valueOf(critical), true)

                    .addField("Total Tests", String.valueOf(totalTests), true)
                    .addField("Tests Per Million", String.valueOf(testsPerOneMillion), true)

                    .setFooter("The data provided may be inaccurate! \n" +
                            "Exact data can be found at: https://covid19.who.int/");

            ctx.reply(embed.build());

        });
    }

}
