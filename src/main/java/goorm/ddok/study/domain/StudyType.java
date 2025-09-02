package goorm.ddok.study.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum StudyType {
    CERTIFICATION("자격증 취득"),
    JOB_INTERVIEW("취업/면접"),
    SELF_DEV("자기 개발"),
    LANGUAGE("어학"),
    LIFE("생활"),
    HOBBY("취미/교양"),
    ETC("기타");

    private final String label;

    StudyType(String label) {
        this.label = label;
    }

    /** 응답 시 한글 값 반환 */
    @JsonValue
    public String getLabel(){
        return label;
    }

    /** 요청 시 한글 -> ENUM 매핑 */
    @JsonCreator
    public static StudyType fromLabel(String label) {
        return Arrays.stream(values())
                .filter(type -> type.label.equals(label))
                .findFirst()
                .orElse(null); // Jackson 바인딩 실패 시 null -> 서비스에서 isValid()로 잡기
    }

    /** ENUM 유효성 검증 */
    public static boolean isValid(StudyType type) {
        return type != null && Arrays.asList(values()).contains(type);
    }



}
