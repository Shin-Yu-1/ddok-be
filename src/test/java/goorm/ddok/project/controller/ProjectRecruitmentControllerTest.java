package goorm.ddok.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import goorm.ddok.project.domain.ProjectMode;
import goorm.ddok.project.dto.request.ProjectRecruitmentCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectRecruitmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("프로젝트 모집글 생성 API - 성공")
    @WithMockUser(username = "1", roles = {"USER"}) // DB 유저 없어도 SecurityContext 채워줌
    void createProjectRecruitment_success() throws Exception {
        // given
        ProjectRecruitmentCreateRequest request = ProjectRecruitmentCreateRequest.builder()
                .title("테스트 프로젝트")
                .expectedStart(LocalDate.of(2025, 9, 1))
                .expectedMonth(3)
                .mode(ProjectMode.OFFLINE)
                .capacity(5)
                .positions(List.of("백엔드", "프론트엔드"))
                .leaderPosition("백엔드")
                .detail("테스트 모집 상세 내용입니다.")
                .build();

        // when & then
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
