package goorm.ddok.team.service;

import goorm.ddok.chat.service.ChatService;
import goorm.ddok.member.domain.User;
import goorm.ddok.team.domain.Team;
import goorm.ddok.team.domain.TeamMemberRole;
import goorm.ddok.team.domain.TeamType;
import goorm.ddok.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TeamCommandService {

    private final ChatService chatService;
    private final TeamRepository teamRepository;

    /**
     * 모집글 기반 팀 생성 (프로젝트 / 스터디 공용)
     */
    public Team createTeamForRecruitment(Long recruitmentId, TeamType type, String title, User leader) {
        Team team = Team.builder()
                .recruitmentId(recruitmentId)
                .type(type)
                .title(title)
                .user(leader)
                .build();

        // 리더 팀원 자동 추가
        team.addMember(leader, TeamMemberRole.LEADER);
        // 팀 채팅 추가
        chatService.createTeamChatRoom(team, leader);

        return teamRepository.save(team);
    }
}
