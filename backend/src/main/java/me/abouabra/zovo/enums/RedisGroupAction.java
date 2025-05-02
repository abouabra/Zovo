package me.abouabra.zovo.enums;

import lombok.Getter;


/**
 * Represents a group action in Redis, categorized by a specific group name.
 * <p>
 * Provides pre-defined groups such as:
 * <ul>
 *   <li><b>AUTH</b> for authentication-related actions.</li>
 *   <li><b>ROLE</b> for role management actions.</li>
 *   <li><b>TWO_FA</b> for two-factor authentication actions.</li>
 * </ul>
 * Each action is associated with a group name used for categorization.
 */
@Getter
public enum RedisGroupAction {
    AUTH("auth"),
    ROLE("role"),
    TWO_FA("2fa");

    private final String groupName;

    RedisGroupAction(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return groupName;
    }
}
