package fr.irwin.uge.commands;

import com.google.common.base.MoreObjects;
import fr.irwin.uge.UGEBot;
import fr.irwin.uge.commands.core.Command;
import fr.irwin.uge.config.Config;
import fr.irwin.uge.features.MessageFeature;
import fr.irwin.uge.features.channel.TrafficNotifier;
import fr.irwin.uge.features.message.AutoRole;
import fr.irwin.uge.features.message.OrganizationDisplay;
import fr.irwin.uge.internals.EventWaiter;
import fr.irwin.uge.utils.MessageUtils;
import fr.irwin.uge.utils.RolesUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.util.Arrays;
import java.util.regex.Pattern;

public class AdminCommands
{
    @Command(name = "autorole")
    private void autoRole(Guild guild, TextChannel textChannel, Member member, Message message, String[] args)
    {
        if (guild == null)
        {
            return;
        }
        if (!UGEBot.config().guilds.containsKey(guild.getId()))
        {
            return;
        }
        if (!RolesUtils.isAdmin(member))
        {
            return;
        }

        if (args.length < 1 || !UGEBot.config().guilds.get(guild.getId()).autoRoles.containsKey(args[0]))
        {
            MessageUtils.sendErrorMessage(textChannel,
                    "Vous devez spécifier le nom d'un AutoRole parmi la liste suivante :\n" + "```\n" +
                    String.join(", ", UGEBot.config().guilds.get(guild.getId()).autoRoles.keySet()) + "\n```");
            return;
        }

        final Config.Guild.AutoRole autoRoleConfig = UGEBot.config().guilds.get(guild.getId()).autoRoles.get(args[0]);
        final AutoRole autoRole = new AutoRole(textChannel, autoRoleConfig);
        restoreOrStartMessageFeature(autoRole, message, args);

        message.delete().queue();
    }

    @Command(name = "traffic")
    private void traffic(Guild guild, TextChannel textChannel, Member member, Message message, String[] args)
    {
        if (guild == null)
        {
            return;
        }
        if (!RolesUtils.isAdmin(member))
        {
            return;
        }

        if (args.length == 0)
        {
            TrafficNotifier.instance().registerTextChannel(textChannel.getIdLong());
            return;
        }

        if (args[0].equals("off"))
        {
            TrafficNotifier.instance().unregisterTextChannel(textChannel.getIdLong());
        }
        message.delete().queue();
    }

    @Command(name = "display")
    private void display(Guild guild, TextChannel textChannel, Member member, Message message, String[] args)
    {
        if (guild == null)
        {
            return;
        }
        if (!UGEBot.config().guilds.containsKey(guild.getId()))
        {
            return;
        }
        if (!RolesUtils.isAdmin(member))
        {
            return;
        }

        if (args.length < 1 || !UGEBot.config().guilds.get(guild.getId()).organizationDisplays.containsKey(args[0]))
        {
            MessageUtils.sendErrorMessage(textChannel,
                    "Vous devez spécifier le nom d'un Display parmi la liste suivante :\n" + "```\n" +
                    String.join(", ", UGEBot.config().guilds.get(guild.getId()).organizationDisplays.keySet()) +
                    "\n```");
            return;
        }

        Config.Guild.OrganizationDisplay config = UGEBot.config().guilds.get(guild.getId()).organizationDisplays
                .get(args[0]);
        final OrganizationDisplay display = new OrganizationDisplay(guild.getIdLong(), textChannel.getIdLong(),
                config.title, config.fields);
        restoreOrStartMessageFeature(display, message, args);

        message.delete().queue();
    }

    @Command(name = "broadcast", aliases = {"bc"})
    private void broadcast(Guild guild, TextChannel textChannel, Member member, Message message, String[] args)
    {
        if (guild == null || !RolesUtils.isAdmin(member))
        {
            return;
        }

        StringBuilder awaitingText = new StringBuilder("Envoyez votre message ici pour terminer...");
        final String senderName = member.getEffectiveName();
        final String courseName;

        if (args.length > 0)
        {
            if (!message.getMentionedChannels().isEmpty())
            {
                if (args.length >= 2)
                {
                    courseName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                }
                else
                {
                    courseName = "Annonce générale";
                }
            }
            else
            {
                courseName = String.join(" ", args);
            }
        }
        else
        {
            courseName = "Annonce générale";
        }
        awaitingText.append("\n\nLe titre sera '").append(courseName).append("'.");

        final String avatarUrl = member.getUser().getAvatarUrl();
        final Color color = member.getColor();

        final TextChannel broadcastChannel;
        if (!message.getMentionedChannels().isEmpty())
        {
            broadcastChannel = message.getMentionedChannels().get(0);
        }
        else
        {
            broadcastChannel = textChannel;
        }
        awaitingText.append("\n\nLe message sera envoyé dans ").append(broadcastChannel.getAsMention()).append(".");

        Message awaitingMessage = textChannel.sendMessage(
                new EmbedBuilder().setTitle("En attente").setDescription("Envoyez votre message ici pour terminer...")
                        .addField("Informations", String.format(
                                ":pencil: Le titre sera **\"%s\"**.\n\n:incoming_envelope:Le message sera envoyé dans %s.",
                                courseName, broadcastChannel.getAsMention()), false)
                        .setFooter("Envoyez \"cancel\" pour annuler.").setColor(0xe67e22).build()).complete();

        new EventWaiter.Builder(GuildMessageReceivedEvent.class, e -> e.getAuthor().getIdLong() == member.getIdLong(),
                (e, ew) -> {
                    textChannel.deleteMessages(Arrays.asList(message, e.getMessage(), awaitingMessage)).queue();

                    if (e.getMessage().getContentDisplay().equalsIgnoreCase("cancel"))
                    {
                        ew.close();
                        return;
                    }

                    broadcastChannel.sendMessage(
                            new EmbedBuilder().setTitle(MoreObjects.firstNonNull(courseName, "Annonce"))
                                    .setAuthor(senderName, null, avatarUrl)
                                    .setDescription(e.getMessage().getContentRaw()).setColor(color)
                                    .setFooter("Via Discord").build()).queue();
                }).build();
    }

    private void restoreOrStartMessageFeature(MessageFeature feature, Message message, String[] args)
    {
        if (args.length == 2)
        {
            String messageId;
            if (Pattern.matches("\\d{18}-\\d{18}", args[1]))
            {
                messageId = args[1].split("-")[1];
            }
            else
            {
                messageId = args[1];
            }
            feature.restore(messageId, null);
        }
        else
        {
            feature.send();
        }
    }
}
