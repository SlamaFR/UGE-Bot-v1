package fr.irwin.uge.commands;

import fr.irwin.uge.UGEBot;
import fr.irwin.uge.commands.core.Command;
import fr.irwin.uge.managers.TicketManager;
import fr.irwin.uge.utils.EmotesUtils;
import fr.irwin.uge.utils.MessageUtils;
import fr.irwin.uge.utils.StringUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PublicCommands {

    @Command(name = "ticket")
    private void ticket(Guild guild, TextChannel textChannel, Member member, Message message) {
        if (guild == null) return;
        if (!UGEBot.config().guilds.containsKey(guild.getId())) return;

        if (!TicketManager.hasOpenTickedManager(textChannel)) {
            MessageUtils.sendErrorMessage(textChannel, "Aucune file d'attente ouverte dans ce salon !");
            return;
        }

        if (TicketManager.getTicketManager(textChannel).openTicket(member)) {
            message.addReaction(EmotesUtils.YES.replace(">", "")).queue();
        } else {
            message.addReaction(EmotesUtils.NO.replace(">", "")).queue();
        }
    }

    @Command(name = "table", aliases = {"tab"})
    private void table(TextChannel textChannel, Message message) {
        List<List<String>> table = new ArrayList<>();
        List<Integer> sizes = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        List<String> args = StringUtils.splitArgs(message.getContentRaw());
        args.remove(0);

        for (String arg : args) {
            String[] split = arg.split(";");
            String s;
            for (int i = 0; i < split.length; i++) {
                s = split[i];
                if (table.size() <= i) {
                    table.add(new ArrayList<>());
                }
                if (sizes.size() <= i) {
                    sizes.add(s.length());
                } else if (sizes.get(i) < s.length()) {
                    sizes.set(i, s.length());
                }
                table.get(i).add(s);
            }
            for (int i = split.length; i < table.size(); i++) {
                table.get(i).add("");
            }
        }

        builder.append("```\n");
        writeTab(builder, args.size(), table, sizes);
        builder.append("```");

        textChannel.sendMessage(builder.toString()).queue();
    }

    private void writeTab(StringBuilder builder, int lines, List<List<String>> table, List<Integer> sizes) {
        for (int i = 0; i < lines; i++) {
            writeHorizontalLine(builder, table.size(), sizes);
            for (int j = 0; j < table.size(); j++) {
                builder.append('|');
                builder.append(' ').append(table.get(j).get(i)).append(' ');
                for (int k = table.get(j).get(i).length(); k < sizes.get(j); k++) {
                    builder.append(' ');
                }
            }
            builder.append("|\n");
        }
        writeHorizontalLine(builder, table.size(), sizes);
    }

    private void writeHorizontalLine(StringBuilder builder, int tableSize, List<Integer> sizes) {
        for (int j = 0; j < tableSize; j++) {
            builder.append('+');
            for (int k = 0; k < sizes.get(j) + 2; k++) {
                builder.append('-');
            }
        }
        builder.append("+\n");
    }

}
