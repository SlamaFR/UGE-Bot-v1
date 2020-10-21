package fr.irwin.uge.utils;

import fr.irwin.uge.internals.AutoRole;
import fr.irwin.uge.internals.OrganizationDisplay;
import fr.irwin.uge.redis.Redis;
import fr.irwin.uge.redis.buckets.ChannelsFeatures;
import net.dv8tion.jda.api.entities.Guild;
import org.redisson.api.RBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

public class RedisUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(RedisUtils.class);

    public static <T> T getObject(Class<T> type, String guildId) {
        final String key = guildId + ":" + type.getSimpleName().toLowerCase();
        RBucket<T> bucket = Redis.instance().getBucket(key);
        T t = bucket.get();
        if (t == null) {
            try {
                t = type.getDeclaredConstructor().newInstance();
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                LOGGER.error("[FATAL] Failed to initialize {} bucket!", type.getSimpleName());
            }
        }
        return t;
    }

    public static <T> void setObject(T t, String guildId) {
        final String key = guildId + ":" + t.getClass().getSimpleName().toLowerCase();
        RBucket<T> bucket = Redis.instance().getBucket(key);
        bucket.set(t);
    }

    public static void addAutoRole(Guild guild, long messageId, AutoRole autoRole) {
        ChannelsFeatures channelsFeatures = getObject(ChannelsFeatures.class, guild.getId());
        channelsFeatures.getAutoRoles().put(messageId, autoRole);
        setObject(channelsFeatures, guild.getId());
    }

    public static void removeAutoRole(Guild guild, long messageId) {
        ChannelsFeatures channelsFeatures = getObject(ChannelsFeatures.class, guild.getId());
        channelsFeatures.getAutoRoles().remove(messageId);
        setObject(channelsFeatures, guild.getId());
    }

    public static void addTrafficChannel(Guild guild, long textChannelId) {
        ChannelsFeatures channelsFeatures = getObject(ChannelsFeatures.class, guild.getId());
        channelsFeatures.getTrafficNotificationChannels().add(textChannelId);
        setObject(channelsFeatures, guild.getId());
    }

    public static void removeTrafficChannel(Guild guild, long textChannelId) {
        ChannelsFeatures channelsFeatures = getObject(ChannelsFeatures.class, guild.getId());
        channelsFeatures.getTrafficNotificationChannels().remove(textChannelId);
        setObject(channelsFeatures, guild.getId());
    }

    public static void addDisplay(Guild guild, long messageId, OrganizationDisplay display) {
        ChannelsFeatures channelsFeatures = getObject(ChannelsFeatures.class, guild.getId());
        channelsFeatures.getDisplays().put(messageId, display);
        setObject(channelsFeatures, guild.getId());
    }

    public static void removeDisplay(Guild guild, long messageId) {
        ChannelsFeatures channelsFeatures = getObject(ChannelsFeatures.class, guild.getId());
        channelsFeatures.getDisplays().remove(messageId);
        setObject(channelsFeatures, guild.getId());
    }
}
