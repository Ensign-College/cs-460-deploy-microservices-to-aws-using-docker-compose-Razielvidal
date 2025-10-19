package com.example.explorecalijpa.config;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Service that exposes boolean feature flags backed by configuration
 * properties. Flags are read from keys with the prefix {@code features}.
 */
@Component
public class FeatureFlagService {

  private final Environment environment;

  public FeatureFlagService(Environment environment) {
    this.environment = environment;
  }

  /**
   * Determine if the given feature is enabled. If the feature flag is not
   * present it is considered disabled.
   *
   * @param featureName name of the feature
   * @return true if enabled, otherwise false
   */
  public boolean isEnabled(String featureName) {
    return environment.getProperty("features." + featureName, Boolean.class, false);
  }
}
