package etf.iot.cloud.platform.services.services.impl;

import etf.iot.cloud.platform.services.dao.ProtocolInputDao;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupService {

    private final ProtocolInputDao protocolInputDao;

    @PostConstruct
    public void onStart() {
        protocolInputDao.deleteAll();
    }
}
