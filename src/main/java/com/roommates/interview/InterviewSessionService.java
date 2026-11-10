package com.roommates.interview;

import java.util.List;

import com.roommates.interview.dto.InterviewSessionCreateRequest;
import com.roommates.interview.dto.InterviewSessionPatchRequest;
import com.roommates.interview.dto.InterviewSessionResponse;
import com.roommates.interview.dto.InterviewSessionUpdateRequest;

public interface InterviewSessionService {
    List<InterviewSessionResponse> findAll(InterviewStatus status);

    InterviewSessionResponse findById(Long id);

    InterviewSessionResponse create(InterviewSessionCreateRequest request);

    InterviewSessionResponse update(Long id, InterviewSessionUpdateRequest request);

    InterviewSessionResponse patch(Long id, InterviewSessionPatchRequest request);

    void delete(Long id);
}
