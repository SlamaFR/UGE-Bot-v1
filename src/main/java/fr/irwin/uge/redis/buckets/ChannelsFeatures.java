package fr.irwin.uge.redis.buckets;

import com.google.gson.Gson;
import fr.irwin.uge.internals.AutoRole;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChannelsFeatures {

    private final Map<Long, AutoRole> autoRoles = new HashMap<>();
    private final Set<Long> trafficNotificationChannels = new HashSet<>();

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public Map<Long, AutoRole> getAutoRoles() {
        return autoRoles;
    }

    public Set<Long> getTrafficNotificationChannels() {
        return trafficNotificationChannels;
    }
}
