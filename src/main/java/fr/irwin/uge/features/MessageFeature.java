package fr.irwin.uge.features;

import fr.irwin.uge.UGEBot;
import fr.irwin.uge.utils.RedisUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MessageFeature
{
    protected final long guildId;
    protected final long textChannelId;
    protected transient final Logger logger;

    protected MessageFeature(long guildId, long textChannelId) {
        this.guildId = guildId;
        this.textChannelId = textChannelId;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    public abstract void send();

    public void restore(String messageId, @Nullable Runnable success) {
        final Guild guild = UGEBot.JDA().getGuildById(guildId);
        if (guild == null) {
            return;
        }

        final TextChannel textChannel = guild.getTextChannelById(textChannelId);
        if (textChannel == null) {
            return;
        }

        textChannel.retrieveMessageById(messageId).queue(message -> {
            if (!message.getAuthor().equals(UGEBot.JDA().getSelfUser())) {
                return;
            }

            start(message);
            RedisUtils.addFeature(textChannel.getGuild(), message.getIdLong(), this);
            logger.info("Restored {} on {}!", getClass().getSimpleName(), messageId);
            if (success != null) {
                success.run();
            }
        }, failure -> logger.warn("Failed to restore {} on {}!", getClass().getSimpleName(), messageId));
    }

    protected abstract void start(Message message);
}
