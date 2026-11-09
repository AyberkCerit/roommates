package com.roommates.common.error;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String kaynakAdi, Long id) {
        super(kaynakAdi + " bulunamadı: id=" + id);
    }
}
