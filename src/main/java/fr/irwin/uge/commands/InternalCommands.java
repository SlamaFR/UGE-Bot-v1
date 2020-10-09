package fr.irwin.uge.commands;

import fr.irwin.uge.UGEBot;
import fr.irwin.uge.commands.core.Command;
import fr.irwin.uge.utils.StringUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class InternalCommands {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Command(name = "die")
    private void die(Message message) {
        if (message != null) return;
        UGEBot.instance().setRunning(false);
    }

    @Command(name = "reload")
    private void reload(Message message) {
        if (message != null) return;
        UGEBot.instance().reloadConfig();
    }

    @Command(name = "say")
    private void say(Message message, JDA jda, String[] args) {
        if (message != null) return;

        if (args.length < 1) {
            LOGGER.error("Must provide guild!");
            return;
        }

        if (!UGEBot.config().guilds.containsKey(args[0])) {
            LOGGER.error("This guild is not registered in config!");
            return;
        }

        Guild guild = jda.getGuildById(args[0]);
        if (guild == null) {
            LOGGER.error("Couldn't find {} guild!", args[0]);
            return;
        }

        if (args.length < 2) {
            LOGGER.error("Must provide channel!");
            return;
        }

        TextChannel textChannel = jda.getTextChannelById(args[1]);
        if (textChannel == null) {
            LOGGER.error("Couldn't find {} text channel!", args[1]);
            return;
        }

        if (args.length < 3) {
            LOGGER.error("Must provide message!");
            return;
        }

        textChannel.sendMessage(String.join(" ", Arrays.copyOfRange(args, 2, args.length))).queue();
    }

    @Command(name = "announce")
    private void announce(Message message, JDA jda, String[] args) {
        if (message != null) return;

        if (args.length < 1) {
            LOGGER.error("Must provide guild or \"all\"!");
            return;
        }

        if (!UGEBot.config().guilds.containsKey(args[0])) {
            LOGGER.error("This guild is not registered in config!");
            return;
        }

        Set<String> guildsId;
        if (args[0].equals("all")) {
            guildsId = jda.getGuilds().stream().map(ISnowflake::getId).collect(Collectors.toSet());
        } else {
            guildsId = new HashSet<>(Collections.singletonList(args[0]));
        }

        String msg = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        for (String guildId : guildsId) {
            Guild guild = jda.getGuildById(guildId);
            if (guild == null) {
                LOGGER.error("Couldn't find {} guild!", guildId);
                continue;
            }

            if (args.length < 2) {
                LOGGER.error("Must provide message!");
                return;
            }

            TextChannel textChannel = guild.getTextChannelById(UGEBot.config().guilds.get(guildId).channels.announcements);
            if (textChannel == null) {
                LOGGER.error("Couldn't find announcements text channel on guild {}!", guildId);
                continue;
            }

            textChannel.sendMessage(msg).queue();
        }
    }

    @Command(name = "rename")
    private void rename(Message message, JDA jda, String[] args) {
        if (message != null) return;

        if (args.length < 1) {
            LOGGER.error("Must provide guild");
            return;
        }

        if (!UGEBot.config().guilds.containsKey(args[0])) {
            LOGGER.error("This guild is not registered in config!");
            return;
        }

        Guild guild = jda.getGuildById(args[0]);
        if (guild == null) {
            LOGGER.error("Can't find guild!");
            return;
        }

        StringBuilder renamed = new StringBuilder();
        StringBuilder ignored = new StringBuilder();
        StringBuilder declined = new StringBuilder();

        LOGGER.info("Checking members...");
        for (Member m : guild.loadMembers().get()) {
            if (m.getUser().isBot()) continue;
            if (StringUtils.isCapitalized(m.getEffectiveName())) continue;

            String nick = m.getEffectiveName().replaceAll("[._]", " ");

            if (m.getEffectiveName().equals(StringUtils.capitalizeString(nick))) {
                if (ignored.length() > 1) ignored.append(", ");
                ignored.append(m.getEffectiveName());
                continue;
            }

            try {
                m.modifyNickname(StringUtils.capitalizeString(nick)).complete();
                if (renamed.length() > 1) renamed.append(", ");
                renamed.append(m.getEffectiveName());
            } catch (HierarchyException e) {
                if (ignored.length() > 1) ignored.append(", ");
                ignored.append(m.getEffectiveName());
            }
        }

        LOGGER.info("Verifying renaming...");
        for (Member m : guild.loadMembers().get()) {
            if (m.getUser().isBot()) continue;
            if (StringUtils.isCapitalized(m.getEffectiveName())) continue;

            if (declined.length() > 1) declined.append(", ");
            declined.append(m.getEffectiveName());
        }

        System.out.println();
        if (renamed.length() > 0) {
            System.out.println("Members renamed:");
            System.out.println(renamed.toString());
            System.out.println();
        }
        if (ignored.length() > 0) {
            System.out.println("Ignored members:");
            System.out.println(ignored.toString());
            System.out.println();
        }
        if (declined.length() > 0) {
            System.out.println("Declined members:");
            System.out.println(declined.toString());
            System.out.println();
        }

        if (renamed.length() + ignored.length() + declined.length() == 0) {
            LOGGER.info("Nothing changed.");
        }
    }

}
