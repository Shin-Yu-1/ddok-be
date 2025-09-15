package goorm.ddok.reputation.cache;

public final class ReputationRankRedisKeys {
    private ReputationRankRedisKeys() {}

    public static final String TOP1_LIVE = "rank:temperature:top1";
    public static final String TOP10_LIVE = "rank:temperature:top10";
    public static final String REGION_TOP1_LIVE = "rank:temperature:region-top1";

    public static final String TOP1_TMP = TOP1_LIVE + ":tmp";
    public static final String TOP10_TMP = TOP10_LIVE + ":tmp";
    public static final String REGION_TOP1_TMP = REGION_TOP1_LIVE + ":tmp";

    public static final long DEFAULT_TTL_SECONDS = 4000;
}
