package me.abouabra.zovo.exceptions;

public class TwoFactorAuthAlreadyEnabledException extends RuntimeException {
    public TwoFactorAuthAlreadyEnabledException(String message) {
        super(message);
    }
}
