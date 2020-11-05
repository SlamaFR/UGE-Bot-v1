package fr.irwin.uge.redis;

import fr.irwin.uge.UGEBot;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Redis
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Redis.class);
    private static Redis instance;
    private final RedissonClient redissonClient;

    private Redis()
    {
        final String host = UGEBot.config().redis.host;
        final int port = UGEBot.config().redis.port;
        final String password = UGEBot.config().redis.password;

        this.redissonClient = initRedisson(host, port, password);
    }

    public static RedissonClient instance()
    {
        if (instance == null) instance = new Redis();
        return instance.redissonClient;
    }

    public static void close()
    {
        LOGGER.info("Closing Redis connection...");
        Redis.instance().shutdown();
    }

    private RedissonClient initRedisson(String host, int port, String password)
    {
        final Config config = new Config();

        LOGGER.info("Connecting to Redis...");

        config.setCodec(new JsonJacksonCodec());
        config.setThreads(2);
        config.setNettyThreads(2);
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setPassword(password)
                .setDatabase(0)
                .setClientName("uge-bot");


        RedissonClient redissonClient;
        try
        {
            redissonClient = Redisson.create(config);
        } catch (Exception e)
        {
            LOGGER.error("Couldn't connect to Redis!");
            System.exit(1);
            return null;
        }

        LOGGER.info("Successfully connected to Redis!");
        return redissonClient;
    }
}
