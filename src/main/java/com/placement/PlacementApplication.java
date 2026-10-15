package com.placement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class PlacementApplication {

    private static final Logger logger = LoggerFactory.getLogger(PlacementApplication.class);

    public static void main(String[] args) {
        var context = SpringApplication.run(PlacementApplication.class, args);
        Environment env = context.getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String dataDir = env.getProperty("app.storage.data-dir", "./data");

        logger.info("====================================================================");
        logger.info("   Placement Drive Management System successfully started!         ");
        logger.info("   Access UI & REST API: http://localhost:{}                      ", port);
        logger.info("   Data Directory:       {}                                       ", dataDir);
        logger.info("   Health Check:         http://localhost:{}/api/health           ", port);
        logger.info("====================================================================");
    }
}
