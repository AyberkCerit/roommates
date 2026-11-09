package com.roommates.user;

/**
 * KULLANICI SERVİSİ (arayüz)
 *
 * Kullanıcılarla ilgili iş kurallarının listesi (M5).
 * Kuralların nasıl uygulandığı UserServiceImpl sınıfındadır.
 */
public interface UserService {

    /**
     * Uygulama ilk açıldığında 3 rolü ve 4 örnek kullanıcıyı oluşturur:
     * admin, editor, viewer, pasif. Zaten varlarsa dokunmaz.
     */
    void seedDefaultUsers(String adminPassword,
                          String editorPassword,
                          String viewerPassword,
                          String inactivePassword);
}
