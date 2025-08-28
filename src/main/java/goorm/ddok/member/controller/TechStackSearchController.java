package goorm.ddok.member.controller;

import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.member.dto.response.TechStackResponse;
import goorm.ddok.member.service.TechStackSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "Auth", description = "회원가입, 로그인, 비밀번호 재설정 등 사용자 인증 API")
public class TechStackSearchController {

    private final TechStackSearchService techStackSearchService;

    @Operation(
            summary = "기술 스택 검색",
            description = "키워드로 기술 스택을 검색합니다. (as-you-type + 오타 허용)",
            security = @SecurityRequirement(name = "Authorization")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "기술 스택 검색 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Spring 검색 결과",
                                    value = """
                                    {
                                      "status": 200,
                                      "message": "기술 스택 검색에 성공하였습니다.",
                                      "data": {
                                        "techStacks": [
                                          "Spring Boot",
                                          "Spring Data JPA",
                                          "Spring Security"
                                        ]
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청(필수 파라미터 누락/길이 위반 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "keyword 누락",
                                            value = """
                                            {
                                              "status": 400,
                                              "message": "keyword는 필수입니다.",
                                              "data": null
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "길이 위반",
                                            value = """
                                            {
                                              "status": 400,
                                              "message": "keyword는 1~50자여야 합니다.",
                                              "data": null
                                            }
                                            """
                                    )
                            }
                    )
            ),
    })
    @GetMapping("/stacks")
    public ResponseEntity<ApiResponseDto<TechStackResponse>> searchTechStacks(
            @Parameter(
                    in = ParameterIn.QUERY,
                    required = true,
                    description = "검색할 기술 스택 키워드(1~50자)",
                    schema = @Schema(type = "string", minLength = 1, maxLength = 50, example = "spring")
            )
            @RequestParam
            @NotBlank(message = "keyword는 필수입니다.")
            @Size(min = 1, max = 50, message = "keyword는 1~50자여야 합니다.")
            String keyword
    ) {
        // 수동 길이 체크는 제거 (@Size로 충분)
        TechStackResponse response = techStackSearchService.searchTechStacks(keyword);
        return ResponseEntity.ok(ApiResponseDto.of(200, "기술 스택 검색에 성공하였습니다.", response));
    }
}
