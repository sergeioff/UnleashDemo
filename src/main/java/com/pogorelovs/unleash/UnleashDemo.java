package com.pogorelovs.unleash;

import no.finn.unleash.DefaultUnleash;
import no.finn.unleash.UnleashException;
import no.finn.unleash.event.UnleashSubscriber;
import no.finn.unleash.repository.FeatureToggleResponse;
import no.finn.unleash.util.UnleashConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

public class UnleashDemo {

    static final Logger logger = LoggerFactory.getLogger(UnleashDemo.class);

    private static final int FETCH_INTERVAL_SECONDS = 5;

    public static void main(String[] args) throws InterruptedException {
        var unleashConfig = UnleashConfig.builder()
                .appName("UnleashDemo")
                .instanceId("Instance 1")
                .unleashAPI("http://127.0.0.1:4242/api")
                .fetchTogglesInterval(FETCH_INTERVAL_SECONDS)
                .subscriber(new UnleashSubscriber() {
                    @Override
                    public void togglesFetched(FeatureToggleResponse toggleResponse) {
                        logger.info("Fetched toggles");
                    }

                    @Override
                    public void onError(UnleashException unleashException) {
                        logger.info("Error: {}", unleashException.getMessage());
                    }
                })
                .build();



        var unleash = new DefaultUnleash(unleashConfig);

        while (true) {
            Map<String, String> featureStatuses = unleash.getFeatureToggleNames().stream()
                    .collect(Collectors.toMap(
                            feature -> feature,
                            feature -> Boolean.toString(unleash.isEnabled(feature)))
                    );

            logger.info("Feature statuses: {}", featureStatuses);

            if (unleash.isEnabled("stop")) {
                logger.info("Stopping application");
                break;
            }

            Thread.sleep(FETCH_INTERVAL_SECONDS * 1000);
        }

        unleash.shutdown();
    }
}
