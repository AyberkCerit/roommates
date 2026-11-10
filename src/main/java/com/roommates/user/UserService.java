package com.roommates.user;

import java.util.List;

import com.roommates.user.dto.UserCreateRequest;
import com.roommates.user.dto.UserPatchRequest;
import com.roommates.user.dto.UserResponse;
import com.roommates.user.dto.UserUpdateRequest;

public interface UserService {
    void seedDefaultUsers(String adminPassword,
                          String editorPassword,
                          String viewerPassword,
                          String inactivePassword);

    List<UserResponse> findAll(Boolean enabled);

    UserResponse findById(Long id);

    UserResponse create(UserCreateRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    UserResponse patch(Long id, UserPatchRequest request);

    void delete(Long id);
}
