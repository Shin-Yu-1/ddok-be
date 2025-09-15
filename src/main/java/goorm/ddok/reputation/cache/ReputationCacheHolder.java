package goorm.ddok.reputation.cache;

import goorm.ddok.reputation.dto.response.TemperatureRankResponse;
import goorm.ddok.reputation.dto.response.TemperatureRegionResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ReputationCacheHolder {
    private final AtomicReference<TemperatureRankResponse> top1 = new AtomicReference<>();
    private final AtomicReference<List<TemperatureRankResponse>> top10 = new AtomicReference<>();
    private final AtomicReference<List<TemperatureRegionResponse>> regionTop1 = new AtomicReference<>();
    private final AtomicReference<Instant> lastUpdated = new AtomicReference<>();

    public void setTop1(TemperatureRankResponse v, Instant at) {
        top1.set(v);
        lastUpdated.set(at);
    }

    public void setTop10(List<TemperatureRankResponse> v, Instant at) {
        top10.set(v);
        lastUpdated.set(at);
    }

    public void setRegionTop1(List<TemperatureRegionResponse> v, Instant at) {
        regionTop1.set(v);
        lastUpdated.set(at);
    }

    public TemperatureRankResponse getTop1() { return top1.get(); }
    public List<TemperatureRankResponse> getTop10() { return top10.get(); }
    public List<TemperatureRegionResponse> getRegionTop1() { return regionTop1.get(); }
    public Instant getLastUpdated() { return lastUpdated.get(); }
}
