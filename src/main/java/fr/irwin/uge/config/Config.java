package fr.irwin.uge.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Config {

    public static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    @JsonProperty(required = true)
    public String token = "";

    @JsonProperty
    public Map<String, String> presenceMessages = new HashMap<>();

    @JsonProperty(required = true)
    public Map<String, Guild> guilds = new HashMap<>();

    public static class Guild {

        @JsonProperty(required = true)
        public Roles roles = new Roles();

        public static class Roles {

            @JsonProperty(required = true)
            public String adminRole = "";

            @JsonProperty(required = true)
            public String teacherRole = "";

            @JsonProperty(required = true)
            public String studentRole = "";

        }

        @JsonProperty
        public Channels channels = new Channels();

        public static class Channels {

            @JsonProperty
            public String announcements = "";

        }

        @JsonProperty
        public Map<String, AutoRole> autoRoles = new HashMap<>();

        public static class AutoRole {

            @JsonProperty(required = true)
            public String title = "";

            @JsonProperty(required = true)
            public String description = "";

            @JsonProperty
            public boolean rolesList = false;

            @JsonProperty
            public int maxRoles = 1;

            @JsonProperty(required = true)
            public Map<String, String> roles;

        }

    }

    @JsonProperty(required = true)
    public Redis redis = new Redis();

    public static class Redis {

        @JsonProperty(required = true)
        public String host = "127.0.0.1";

        @JsonProperty(required = true)
        public int port = 6379;

        @JsonProperty(required = true)
        public String password = "";

    }

    public static Config parseFile(String path) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper(new JsonFactory());

        final String jsonSource;
        try {
            jsonSource = new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
            LOGGER.info("Successfully loaded config!");
        } catch (IOException e) {
            LOGGER.warn("Missing config file!");
            try {
                mapper.writeValue(new File("./config.json"), new Config());
                LOGGER.info("An empty config file has been created! Fill it and run the bot.");
                System.exit(2);
                return null;
            } catch (IOException e1) {
                LOGGER.error("Couldn't generate config file!");
                System.exit(1);
                return null;
            }
        }
        return mapper.readValue(jsonSource, Config.class);
    }

}
