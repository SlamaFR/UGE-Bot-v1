package fr.irwin.uge.utils;

import fr.irwin.uge.redis.Redis;
import net.dv8tion.jda.api.entities.Guild;
import org.redisson.api.RBucket;

public final class RedisUtils {
    public static <T> String getKey(Guild guild, T t) {
        return getKey(guild, t.getClass());
    }

    public static <T> String getKey(Guild guild, Class<T> tClass) {
        return guild.getId() + ":" + tClass.getSimpleName() + "s";
    }

    public static <T> void addFeature(Guild guild, long messageId, T t)
    {
        Redis.instance().getMap(getKey(guild, t)).put(messageId, t);
    }

    public static <T> void removeFeature(Guild guild, long messageId, T t)
    {
        Redis.instance().getMap(getKey(guild, t)).remove(messageId);
    }

    public static <T> T getObject(Class<T> tClass)
    {
        RBucket<T> bucket = Redis.instance().getBucket(tClass.getSimpleName());
        return bucket.get();
    }

    public static <T> void setObject(T t)
    {
        RBucket<T> bucket = Redis.instance().getBucket(t.getClass().getSimpleName());
        bucket.set(t);
    }
}
