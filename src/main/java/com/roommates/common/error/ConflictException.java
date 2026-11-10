package com.roommates.common.error;

public class ConflictException extends RuntimeException {
    public ConflictException(String mesaj) {
        super(mesaj);
    }
}
