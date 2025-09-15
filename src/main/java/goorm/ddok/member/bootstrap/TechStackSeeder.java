package goorm.ddok.member.bootstrap;

import goorm.ddok.member.domain.TechStack;
import goorm.ddok.member.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.techstack", name = "seed-on-startup", havingValue = "true", matchIfMissing = true)
public class TechStackSeeder implements ApplicationRunner {

    private final TechStackRepository techStackRepository;

    // 쉼표로 구분된 기본 스택 목록을 yml에서 주입 (미설정 시 기본값 사용)
    @Value("#{'${app.techstack.defaults}'.split(',')}")
    private List<String> defaultStacks;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        long count = techStackRepository.count();
        if (count >= defaultStacks.size()) {
            log.info("[TechStackSeeder] Skip seeding. Existing rows: {}", count);
            return;
        }

        var toSave = defaultStacks.stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .map(name -> TechStack.builder().name(name).build())
                .toList();

        techStackRepository.saveAll(toSave);
        log.info("[TechStackSeeder] Seeded tech_stack with {} rows", toSave.size());
    }
}
