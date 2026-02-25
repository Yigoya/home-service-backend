package com.home.service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.home.service.dto.AppVersionResponse;

@RestController
@RequestMapping("/app")
public class AppVersionController {

    private final int minSupportedVersion;
    private final int latestVersion;
    private final boolean forceUpdate;
    private final String updateMessage;

    public AppVersionController(
            @Value("${app.version.min-supported:5}") int minSupportedVersion,
            @Value("${app.version.latest:5}") int latestVersion,
            @Value("${app.version.force-update:true}") boolean forceUpdate,
            @Value("${app.version.update-message:Please update the app to continue using our services.}") String updateMessage) {
        this.minSupportedVersion = minSupportedVersion;
        this.latestVersion = latestVersion;
        this.forceUpdate = forceUpdate;
        this.updateMessage = updateMessage;
    }

    @GetMapping("/version")
    public ResponseEntity<AppVersionResponse> getAppVersion() {
        AppVersionResponse response = new AppVersionResponse(
                minSupportedVersion,
                latestVersion,
                forceUpdate,
                updateMessage);
        return ResponseEntity.ok(response);
    }
}
