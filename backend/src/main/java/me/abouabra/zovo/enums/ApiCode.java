package me.abouabra.zovo.enums;

/**
 * <p>Represents a set of predefined API response codes used for categorizing and
 * handling application responses.</p>
 *
 * <ul>
 *   <li>Defines standard response codes like <code>SUCCESS</code>, <code>BAD_REQUEST</code>, and <code>NOT_FOUND</code>.</li>
 *   <li>Facilitates consistent API response handling and error identification.</li>
 *   <li>Can be used across the application to standardize HTTP and application-level errors.</li>
 * </ul>
 */
public enum ApiCode {
    SUCCESS,
    BAD_REQUEST,
    UNAUTHORIZED,
    FORBIDDEN,
    NOT_FOUND,
    INTERNAL_SERVER_ERROR,
    INVALID_JSON,
    BAD_CREDENTIALS,
    ILLEGAL_ARGUMENT,
    INVALID_VALUE,
    TYPE_MISMATCH,
    METHOD_NOT_ALLOWED,
    TOO_MANY_REQUESTS,
    RATE_LIMITED,
    ACCOUNT_DISABLED,
    RESOURCE_NOT_FOUND,
    USER_NOT_FOUND,
    ROLE_NOT_FOUND,
    USER_ALREADY_EXISTS,
    INVALID_VERIFICATION_TOKEN,
    LOGIN_NEEDS_2FA,
    TWO_FACTOR_AUTH_REQUIRED,
    INVALID_TWO_FACTOR_CODE,
    TWO_FACTOR_AUTH_NOT_ENABLED,
    TWO_FACTOR_ALREADY_ENABLED,
}
