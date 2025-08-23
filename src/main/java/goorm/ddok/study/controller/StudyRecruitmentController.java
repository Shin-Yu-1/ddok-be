package goorm.ddok.study.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.study.dto.request.StudyRecruitmentCreateRequest;
import goorm.ddok.study.dto.response.StudyRecruitmentCreateResponse;
import goorm.ddok.study.service.StudyRecruitmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyRecruitmentController {

    private final StudyRecruitmentService studyRecruitmentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<StudyRecruitmentCreateResponse>> createStudy(
            @RequestPart("request") @Valid StudyRecruitmentCreateRequest request,
            @RequestPart(value = "bannerImage", required = false) MultipartFile bannerImage,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        StudyRecruitmentCreateResponse response = studyRecruitmentService.createStudy(request, bannerImage, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.of(201, "스터디 생성이 성공했습니다.", response));
    }
}
