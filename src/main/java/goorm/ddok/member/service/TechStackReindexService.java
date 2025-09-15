package goorm.ddok.member.service;

import goorm.ddok.member.domain.TechStack;
import goorm.ddok.member.repository.TechStackRepository;
import goorm.ddok.member.search.TechStackDocument;
import goorm.ddok.member.search.TechStackSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class TechStackReindexService {

    private final TechStackRepository jpa;
    private final TechStackSearchRepository es;

    // purge=true면 ES를 비우고 다시 색인
    @Transactional(readOnly = true)
    public int reindexAll(boolean purge) {
        if (purge) {
            es.deleteAll();
        }

        int page = 0, size = 500;
        int total = 0;

        while (true) {
            var slice = jpa.findAll(PageRequest.of(page, size));
            if (!slice.hasContent()) break;

            var docs = new ArrayList<TechStackDocument>(slice.getNumberOfElements());
            for (TechStack t : slice.getContent()) {
                docs.add(TechStackDocument.builder()
                        .id(String.valueOf(t.getId()))
                        .name(t.getName())
                        .build());
            }
            es.saveAll(docs);
            total += docs.size();

            if (!slice.hasNext()) break;
            page++;
        }

        return total;
    }
}
