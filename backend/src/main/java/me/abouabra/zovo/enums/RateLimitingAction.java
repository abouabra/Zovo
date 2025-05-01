package me.abouabra.zovo.enums;

import lombok.Getter;

/**
 * <p>Defines actions subject to rate limiting, primarily for security and abuse prevention.
 */
@Getter
public enum RateLimitingAction {
    LOGIN("login"),
    REGISTER("register");

    private final String action;

    RateLimitingAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return action;
    }
}
