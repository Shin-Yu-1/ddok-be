package goorm.ddok.notification.support;

import goorm.ddok.reputation.domain.UserReputation;
import goorm.ddok.reputation.repository.UserReputationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class NotificationMessageHelper {

    private final UserReputationRepository reputationRepository;

    public String withTemperatureSuffix(Long counterpartUserId, String baseMessage) {
        BigDecimal temp = reputationRepository.findByUser_Id(counterpartUserId)
                .map(UserReputation::getTemperature)
                .orElse(null);

        if (temp == null) return baseMessage;

        BigDecimal oneDecimal = temp.setScale(1, RoundingMode.HALF_UP);
        return baseMessage + " (상대 온도 " + oneDecimal.toPlainString() + "℃)";
    }
}
