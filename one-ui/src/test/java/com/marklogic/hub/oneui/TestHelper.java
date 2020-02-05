package com.marklogic.hub.oneui;

import com.marklogic.hub.oneui.auth.MarkLogicAuthenticationManager;
import com.marklogic.hub.oneui.models.EnvironmentInfo;
import com.marklogic.hub.oneui.models.HubConfigSession;
import com.marklogic.mgmt.ManageClient;
import com.marklogic.mgmt.ManageConfig;
import com.marklogic.mgmt.api.API;
import com.marklogic.mgmt.api.security.User;
import org.junit.jupiter.api.AfterAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@PropertySource("classpath:application-test.properties")
public class TestHelper {
    @Autowired
    MarkLogicAuthenticationManager markLogicAuthenticationManager;

    @Value("${test.mlHost:localhost}")
    public String mlHost;

    @Value("${test.dataHubDeveloperUsername:data-hub-developer-user}")
    public String dataHubDeveloperUsername;
    @Value("${test.dataHubDeveloperPassword:data-hub-developer-user}")
    public String dataHubDeveloperPassword;

    @Value("${test.adminUserName:admin}")
    public String adminUserName;
    @Value("${test.adminPassword:admin}")
    public String adminPassword;

    private API adminAPI;

    private ManageClient client;

    private User user;

    @Autowired
    private HubConfigSession hubConfig;

    public void authenticateSession() {
        createUser();
        EnvironmentInfo environmentInfo = new EnvironmentInfo(mlHost, "DIGEST", 8000,"DIGEST", 8002,"DIGEST", 8010, "DIGEST", 8011);
        hubConfig.setCredentials(environmentInfo, dataHubDeveloperUsername, dataHubDeveloperPassword);
    }

    private void createUser() {
        client = new ManageClient();
        client.setManageConfig(new ManageConfig(mlHost, 8002, adminUserName, adminPassword));
        adminAPI = new API(client);

        user = new User(adminAPI, dataHubDeveloperUsername);
        user.setUserName(dataHubDeveloperUsername);
        user.setPassword(dataHubDeveloperPassword);
        user.setRole(Stream.of("data-hub-developer").collect(Collectors.toList()));
        user.save();
    }

    @AfterAll
    private void deleteUser() {
        user.delete();
    }
}

