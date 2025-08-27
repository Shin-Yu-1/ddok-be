package goorm.ddok.global.util;

public class AddressNormalizer {
    /**
     * 1depth 행정구역명을 사용자 친화적으로 변환
     * - 서울특별시 → 서울
     * - 부산광역시 → 부산
     * - 경기도 → 경기
     * - 전라북도 → 전북
     * - 충청남도 → 충남
     * - 강원도 → 강원
     * - 경상북도 → 경북
     * - 경상남도 → 경남
     * - 제주특별자치도 → 제주
     */
    public static String normalizeRegion1(String region1depthName) {
        if (region1depthName == null) return null;

        return region1depthName
                .replace("특별시", "")
                .replace("광역시", "")
                .replace("특별자치시", "")
                .replace("특별자치도", "")
                .replace("자치도", "")
                .replace("자치시", "")
                .replace("도", "");
    }

    /**
     * 최종 주소 조립
     * @param region1 1depth (ex. 서울특별시, 경기도)
     * @param region2 2depth (ex. 강남구, 고양시)
     * @return ex) 서울 강남구, 경기 고양시
     */
    public static String buildAddress(String region1, String region2) {
        if (region1 == null || region2 == null) return null;
        return normalizeRegion1(region1) + " " + region2;
    }
}
