package com.campussphere.mapper;

import com.campussphere.dto.ApplicationInfoDto;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Arrays;

@Component
public class ApplicationInfoMapper {

    public ApplicationInfoDto toDto(String applicationName, String version, Environment environment) {
        ApplicationInfoDto dto = new ApplicationInfoDto();
        dto.setApplicationName(applicationName);
        dto.setVersion(version);
        dto.setActiveProfiles(Arrays.asList(environment.getActiveProfiles()));
        dto.setStartedAt(Instant.now());
        return dto;
    }
}
