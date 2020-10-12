package fr.irwin.uge;

import fr.irwin.uge.commands.core.CommandMap;
import fr.irwin.uge.internals.AutoRole;
import fr.irwin.uge.managers.TrafficNotificationManager;
import fr.irwin.uge.redis.buckets.ChannelsFeatures;
import fr.irwin.uge.utils.RedisUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;

/**
 * Created on 04/10/2018.
 */
public class EventListener extends ListenerAdapter {

    private final CommandMap commandMap;
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    public EventListener(CommandMap commandMap) {
        this.commandMap = commandMap;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);
        if (event.getAuthor().isBot()) return;

        if (event.getMessage().getContentRaw().startsWith(CommandMap.getTag()))
            commandMap.commandUser(event.getMessage().getContentRaw().replaceFirst("!", ""), event.getMessage());
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        super.onReady(event);

        LOGGER.info("Restoring channel features...");
        for (Guild guild : event.getJDA().getGuilds()) {
            final Map<Long, AutoRole> autoRoles = RedisUtils.getObject(ChannelsFeatures.class, guild.getId()).getAutoRoles();
            for (Map.Entry<Long, AutoRole> entry : autoRoles.entrySet()) {
                if (entry.getValue().restore(String.valueOf(entry.getKey()))) {
                    LOGGER.info("Restored AutoRole on {}!", entry.getKey());
                } else {
                    LOGGER.warn("Failed to restore AutoRole on {}!", entry.getKey());
                }
            }

            final Set<Long> trafficNotificationChannels = RedisUtils.getObject(ChannelsFeatures.class, guild.getId()).getTrafficNotificationChannels();
            for (Long channelId : trafficNotificationChannels) {
                TrafficNotificationManager.registerTextChannel(guild, channelId);
                LOGGER.info("Restored TrafficNotifier on {}!", channelId);
            }
        }

        LOGGER.info("Ready!");
    }
}
