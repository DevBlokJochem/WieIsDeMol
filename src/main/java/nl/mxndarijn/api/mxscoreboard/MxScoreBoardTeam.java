package nl.mxndarijn.api.mxscoreboard;

import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.chat.EnumChatFormat;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Team;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MxScoreBoardTeam {
    private final String id;
    private final String line;

    private final Team team;
    private final String entry;
    private final MxScoreBoard scoreboard;


    protected MxScoreBoardTeam(MxScoreBoard scoreboard) {
        this.line = "";
        this.id = UUID.randomUUID().toString();
        this.scoreboard = scoreboard;

        Random r = new Random();

        ChatColor randomColor = ChatColor.values()[r.nextInt(ChatColor.values().length)];
        ChatColor randomColor1 = ChatColor.values()[r.nextInt(ChatColor.values().length)];
        ChatColor randomColor2 = ChatColor.values()[r.nextInt(ChatColor.values().length)];
        ChatColor randomColor3 = ChatColor.values()[r.nextInt(ChatColor.values().length)];

        team = scoreboard.getScoreboard().registerNewTeam(this.id);
        this.entry = randomColor.toString() + randomColor1.toString() + randomColor2.toString() + randomColor3.toString() + ChatColor.RESET;
        team.addEntry(entry);

    }

    public String getId() {
        return id;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        line = EnumChatFormat.color(line);
        if (line.length() > scoreboard.MAX_LINE_LENGTH) {
            throw new ScoreboardNameToLongException(line, scoreboard.MAX_LINE_LENGTH);
        }

        String regex = "&#([0-9a-fA-F]{6})";

        // Compileer het patroon
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(line);
        int count = 0;
        while (matcher.find()) {
            String hexColor = matcher.group();
            count++;
        }

        int lineLength = line.length() - 8 * count + count * 2;
        if (lineLength <= scoreboard.MAX_LINE_LENGTH / 2) {
            team.prefix(IChatBaseComponent.optimizedComponent(line).toAdventureComponent(ProtocolVersion.V1_19_4));
            team.suffix(Component.text(""));
        } else {
            String prefix = line.substring(0, scoreboard.MAX_LINE_LENGTH / 2);
            String suffix = getLatestChatColor(prefix) + line.substring(scoreboard.MAX_LINE_LENGTH / 2);
            if ((prefix + suffix).length() > scoreboard.MAX_LINE_LENGTH)
                throw new ScoreboardNameToLongException(line, scoreboard.MAX_LINE_LENGTH);
            team.prefix(IChatBaseComponent.optimizedComponent(prefix).toAdventureComponent(ProtocolVersion.V1_19_4));
            team.suffix(IChatBaseComponent.optimizedComponent(suffix).toAdventureComponent(ProtocolVersion.V1_19_4));

        }
    }


    String[] splitText(String text, int maxNameLength) {
        String prefixValue;
        String nameValue;
        String suffixValue;
        if (text.length() <= maxNameLength) {
            prefixValue = "";
            nameValue = text;
            suffixValue = "";
        } else {
            String[] prefixOther = this.split(text, 16);
            prefixValue = prefixOther[0];
            String other = prefixOther[1];

            String[] nameSuffix = this.split(other, maxNameLength);
            nameValue = nameSuffix[0];
            suffixValue = nameSuffix[1];
        }

        return new String[]{prefixValue, nameValue, suffixValue};
    }

    String[] split(@NonNull String string, int firstElementMaxLength) {
        if (string == null) {
            throw new NullPointerException("string is marked non-null but is null");
        } else if (string.length() <= firstElementMaxLength) {
            return new String[]{string, ""};
        } else {
            int splitIndex = firstElementMaxLength;
            if (string.charAt(firstElementMaxLength - 1) == 167) {
                splitIndex = firstElementMaxLength - 1;
            }

            return new String[]{string.substring(0, splitIndex), string.substring(splitIndex)};
        }
    }

    public Team getTeam() {
        return team;
    }

    public void destroy() {
        team.unregister();
    }

    private String getLatestChatColor(String s) {
        return ChatColor.getLastColors(s);
    }

    public String getEntry() {
        return entry;
    }
}
