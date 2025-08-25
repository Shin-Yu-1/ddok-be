package goorm.ddok.member.service;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import goorm.ddok.member.dto.response.TechStackResponse;
import goorm.ddok.member.repository.TechStackRepository;
import goorm.ddok.member.search.TechStackDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TechStackSearchService {

    private final ElasticsearchOperations operations;
    private final TechStackRepository jpaRepo;

    public TechStackResponse searchTechStacks(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return new TechStackResponse(List.of());
        }

        String original = keyword.trim();
        String normalized = normalizeKeyword(keyword);

        NativeQuery query = buildOptimizedQuery(original, normalized);

        try {
            var hits = operations.search(query, TechStackDocument.class);

            List<String> names = hits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(TechStackDocument::getName)
                    .filter(s -> s != null && !s.isBlank())
                    .distinct()
                    .limit(10)
                    .toList();

            if (names.isEmpty()) {
                names = jpaRepo.findNamesByKeyword(original, org.springframework.data.domain.PageRequest.of(0, 10));
            }
            return new TechStackResponse(names);
        } catch (DataAccessResourceFailureException e) {
            // ES down 등 연결 문제
            return new TechStackResponse(List.of());
        } catch (org.springframework.data.elasticsearch.UncategorizedElasticsearchException |
                 co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
            // 매핑/필드 오류 포함 → 폴백
            return new TechStackResponse(List.of());
        }
    }

    private String normalizeKeyword(String keyword) {

        String trimmed = keyword == null ? "" : keyword.strip();
        if (trimmed.isEmpty()) return "";

        return trimmed
                .toLowerCase(java.util.Locale.ROOT)
                .replaceAll("\\s+", "")
                .replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private NativeQuery buildOptimizedQuery(String original, String normalized) {
        var bool = QueryBuilders.bool()
                // 정확매치(공백 제거 단일 토큰)
                .should(QueryBuilders.term()
                        .field("name.norm")
                        .value(normalized)
                        .boost(10.0f)
                        .build()._toQuery())
                // 접두사
                .should(QueryBuilders.prefix()
                        .field("name.norm")
                        .value(normalized)
                        .boost(5.0f)
                        .build()._toQuery())
                // 한국어 형태소 매치 (자연어)
                .should(QueryBuilders.match()
                        .field("name.ko")
                        .query(original)
                        .boost(4.0f)
                        .build()._toQuery())
                // 일반 매치
                .should(QueryBuilders.match()
                        .field("name")
                        .query(original)
                        .boost(3.0f)
                        .build()._toQuery())
                // 오타 허용
                .should(QueryBuilders.match()
                        .field("name")
                        .query(original)
                        .fuzziness("AUTO")
                        .boost(1.0f)
                        .build()._toQuery())
                .minimumShouldMatch("1")
                .build();

        return NativeQuery.builder()
                .withQuery(bool._toQuery())
                .withPageable(org.springframework.data.domain.PageRequest.of(0, 20))
                .build();
    }
}
