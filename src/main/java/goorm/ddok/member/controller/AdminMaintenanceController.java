package goorm.ddok.member.controller;


import goorm.ddok.global.response.ApiResponseDto;
import goorm.ddok.member.service.TechStackReindexService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/maintenance")
public class AdminMaintenanceController {

    private final TechStackReindexService svc;

    @PostMapping("/reindex/tech-stacks")
    public ResponseEntity<ApiResponseDto<Map<String, Object>>> reindex(
            @RequestParam(defaultValue = "false") boolean purge
    ) {
        int count = svc.reindexAll(purge);
        return ResponseEntity.ok(ApiResponseDto.of(200, "reindexed", Map.of("count", count, "purge", purge)));
    }
}
