package com.roommates.study;

import java.util.List;

import com.roommates.study.dto.StudySessionCreateRequest;
import com.roommates.study.dto.StudySessionPatchRequest;
import com.roommates.study.dto.StudySessionResponse;
import com.roommates.study.dto.StudySessionUpdateRequest;

public interface StudySessionService {
    List<StudySessionResponse> findAll(Long roomId, StudySessionStatus status);

    StudySessionResponse findById(Long id);

    StudySessionResponse create(StudySessionCreateRequest request);

    StudySessionResponse update(Long id, StudySessionUpdateRequest request);

    StudySessionResponse patch(Long id, StudySessionPatchRequest request);

    void delete(Long id);
}
