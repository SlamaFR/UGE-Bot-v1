package fr.irwin.uge.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.irwin.uge.features.message.OrganizationDisplay.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Config
{
    public static final Logger LOGGER = LoggerFactory.getLogger(Config.class);

    @JsonProperty(required = true)
    public String token = "";

    @JsonProperty
    public Map<String, String> presenceMessages = new HashMap<>();

    @JsonProperty(required = true)
    public Map<String, Guild> guilds = new HashMap<>();
    @JsonProperty(required = true)
    public Redis redis = new Redis();
    @JsonProperty(required = true)
    public Mail mail = new Mail();

    public static Config parseFile(String path) throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper(new JsonFactory());

        final String jsonSource;
        try {
            jsonSource = Files.readString(Paths.get(path));
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

    public static class Guild
    {

        @JsonProperty(required = true)
        public Roles roles = new Roles();
        @JsonProperty
        public Channels channels = new Channels();
        @JsonProperty
        public Map<String, AutoRole> autoRoles = new HashMap<>();
        @JsonProperty
        public Map<String, OrganizationDisplay> organizationDisplays = new HashMap<>();
        @JsonProperty
        public String[] disabledCommands = new String[]{};

        public static class Roles
        {

            @JsonProperty(required = true)
            public String adminRole = "";

            @JsonProperty(required = true)
            public String managerRole = "";

            @JsonProperty(required = true)
            public String teacherRole = "";

            @JsonProperty(required = true)
            public String studentRole = "";
        }

        public static class Channels
        {

            @JsonProperty
            public String announcements = "";

            @JsonProperty
            public Map<String, Long> elearning = new HashMap<>();
        }

        public static class AutoRole
        {

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

        public static class OrganizationDisplay
        {

            @JsonProperty
            public String title = "";

            @JsonProperty
            public Map<String, Field> fields = new HashMap<>();
        }
    }

    public static class Redis
    {

        @JsonProperty(required = true)
        public String host = "127.0.0.1";

        @JsonProperty(required = true)
        public int port = 6379;

        @JsonProperty(required = true)
        public String password = "";
    }

    public static class Mail
    {

        @JsonProperty(required = true)
        public String host = "";

        @JsonProperty(required = true)
        public String protocol = "imap";

        @JsonProperty(required = true)
        public int port = 993;

        @JsonProperty(required = true)
        public String user = "";

        @JsonProperty(required = true)
        public String password = "";
    }
}
