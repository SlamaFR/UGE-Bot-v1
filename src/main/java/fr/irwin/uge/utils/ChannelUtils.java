package fr.irwin.uge.utils;

import fr.irwin.uge.UGEBot;
import fr.irwin.uge.config.Config;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.Nullable;

public class ChannelUtils
{
    @Nullable
    public static TextChannel getAnnouncementsChannel(Guild guild)
    {
        Config.Guild guildConfig = UGEBot.config().guilds.get(guild.getId());
        if (guildConfig == null) return null;

        String channelId = guildConfig.channels.announcements;
        return guild.getTextChannelById(channelId);
    }

    @Nullable
    public static TextChannel getCourseChannel(Guild guild, String courseId)
    {
        Config.Guild guildConfig = UGEBot.config().guilds.get(guild.getId());
        if (guildConfig == null) return null;

        if (!guildConfig.channels.elearning.containsKey(courseId)) return null;
        return guild.getTextChannelById(guildConfig.channels.elearning.get(courseId));
    }
}
