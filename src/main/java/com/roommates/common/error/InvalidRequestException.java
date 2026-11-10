package com.roommates.common.error;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String mesaj) {
        super(mesaj);
    }
}
