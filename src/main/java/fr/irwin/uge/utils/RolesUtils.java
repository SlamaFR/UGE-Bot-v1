package fr.irwin.uge.utils;

import fr.irwin.uge.UGEBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.Collection;

/**
 * Created on 04/10/2018.
 */
public class RolesUtils {

    public static boolean isStudent(Member member) {
        String studentRoleId = UGEBot.config().guilds.get(member.getGuild().getId()).roles.studentRole;
        return member.getRoles().stream().anyMatch(r -> r.getId().equals(studentRoleId));
    }

    public static boolean isTeacher(Member member) {
        String teacherRoleId = UGEBot.config().guilds.get(member.getGuild().getId()).roles.teacherRole;
        return member.getRoles().stream().anyMatch(r -> r.getId().equals(teacherRoleId));
    }

    public static boolean isAdmin(Member member) {
        String adminRoleId = UGEBot.config().guilds.get(member.getGuild().getId()).roles.adminRole;
        return member.getRoles().stream().anyMatch(r -> r.getId().equals(adminRoleId)) || member.isOwner();
    }

    public static long commonRoles(Collection<Role> roles1, Collection<Role> roles2) {
        return roles1.stream().filter(roles2::contains).count();
    }
}
