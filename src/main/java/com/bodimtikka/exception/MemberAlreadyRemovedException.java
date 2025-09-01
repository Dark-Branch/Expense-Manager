package com.bodimtikka.exception;

public class MemberAlreadyRemovedException extends RuntimeException {
    public MemberAlreadyRemovedException(String message) {
        super(message);
    }
}
