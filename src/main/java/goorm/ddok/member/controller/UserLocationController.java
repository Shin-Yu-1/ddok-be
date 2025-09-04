package goorm.ddok.member.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.global.security.auth.CustomUserDetails;
import goorm.ddok.member.dto.request.LocationUpdateRequest;
import goorm.ddok.member.dto.response.LocationResponse;
import goorm.ddok.member.service.UserLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players/profile")
@RequiredArgsConstructor
public class UserLocationController {

    private final UserLocationService userLocationService;

    /* =========================
     *  주 활동 지역 수정
     * ========================= */
    @PatchMapping("/location")
    @Operation(summary = "주 활동 지역 수정", description = "사용자의 주 활동 지역 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ApiResponseDto.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "message": "주 활동 지역 수정에 성공했습니다.",
                              "data": {
                                "address": "서울특별시 강남구 역삼동 테헤란로 123-45",
                                "region1depthName": "서울특별시",
                                "region2depthName": "강남구",
                                "region3depthName": "역삼동",
                                "roadName": "테헤란로",
                                "mainBuildingNo": "123",
                                "subBuildingNo": "45",
                                "zoneNo": "06236",
                                "latitude": 37.5665,
                                "longitude": 126.978
                              }
                            }""")))
    })
    public ResponseEntity<ApiResponseDto<LocationResponse>> updateLocation(
            @Valid @RequestBody LocationUpdateRequest request,
            @AuthenticationPrincipal CustomUserDetails me
    ) {
        LocationResponse response = userLocationService.updateLocation(request, me);
        return ResponseEntity.ok(ApiResponseDto.of(200, "주 활동 지역 수정에 성공했습니다.", response));
    }
}