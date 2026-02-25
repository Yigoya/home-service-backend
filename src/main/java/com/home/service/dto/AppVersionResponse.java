package com.home.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppVersionResponse {
    private int minSupportedVersion;
    private int latestVersion;
    private boolean forceUpdate;
    private String updateMessage;
}
