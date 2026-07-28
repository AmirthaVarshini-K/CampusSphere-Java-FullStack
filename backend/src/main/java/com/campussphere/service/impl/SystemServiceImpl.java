package com.campussphere.service.impl;

import com.campussphere.config.ApplicationProperties;
import com.campussphere.dto.ApplicationInfoDto;
import com.campussphere.mapper.ApplicationInfoMapper;
import com.campussphere.service.SystemService;
import org.springframework.core.env.Environment;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

@Service
public class SystemServiceImpl implements SystemService {

    private final ApplicationProperties applicationProperties;
    private final ApplicationInfoMapper applicationInfoMapper;
    private final Environment environment;

    public SystemServiceImpl(ApplicationProperties applicationProperties, ApplicationInfoMapper applicationInfoMapper, Environment environment) {
        this.applicationProperties = applicationProperties;
        this.applicationInfoMapper = applicationInfoMapper;
        this.environment = environment;
    }

    @Override
    public ApplicationInfoDto getApplicationInfo() {
        return applicationInfoMapper.toDto(applicationProperties.getAppName(), "0.1.0-SNAPSHOT", environment);
    }
}
