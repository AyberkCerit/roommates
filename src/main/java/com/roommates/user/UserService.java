package com.roommates.user;

import java.util.List;

import com.roommates.user.dto.UserCreateRequest;
import com.roommates.user.dto.UserPatchRequest;
import com.roommates.user.dto.UserResponse;
import com.roommates.user.dto.UserUpdateRequest;

/**
 * KULLANICI SERVİSİ (arayüz)
 *
 * Kullanıcılarla ilgili iş kurallarının listesi (M5).
 * Kuralların nasıl uygulandığı UserServiceImpl sınıfındadır.
 * Controller sadece bu arayüzü tanır.
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

    /** Kullanıcıları listeler. enabled null ise hepsi, true/false ise filtreli. */
    List<UserResponse> findAll(Boolean enabled);

    /** Numarasıyla tek kullanıcı getirir. Yoksa 404. */
    UserResponse findById(Long id);

    /** Yeni kullanıcı ekler. Aynı kullanıcı adı / e-posta varsa 409. */
    UserResponse create(UserCreateRequest request);

    /** Kullanıcıyı tamamen günceller (PUT). */
    UserResponse update(Long id, UserUpdateRequest request);

    /** Kullanıcıyı kısmen günceller (PATCH). */
    UserResponse patch(Long id, UserPatchRequest request);

    /** Kullanıcıyı siler. Odası veya oturumu varsa 409. */
    void delete(Long id);
}
