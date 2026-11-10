package com.roommates.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class UserSeeder implements ApplicationRunner {
    private final UserService kullaniciServisi;
    private final String adminParolasi;
    private final String editorParolasi;
    private final String viewerParolasi;
    private final String pasifParolasi;

    public UserSeeder(UserService kullaniciServisi,
                      @Value("${roommates.seed.admin-password}") String adminParolasi,
                      @Value("${roommates.seed.editor-password}") String editorParolasi,
                      @Value("${roommates.seed.viewer-password}") String viewerParolasi,
                      @Value("${roommates.seed.inactive-password}") String pasifParolasi) {
        this.kullaniciServisi = kullaniciServisi;
        this.adminParolasi = adminParolasi;
        this.editorParolasi = editorParolasi;
        this.viewerParolasi = viewerParolasi;
        this.pasifParolasi = pasifParolasi;
    }

    @Override
    public void run(ApplicationArguments args) {
        kullaniciServisi.seedDefaultUsers(adminParolasi, editorParolasi, viewerParolasi, pasifParolasi);
    }
}
