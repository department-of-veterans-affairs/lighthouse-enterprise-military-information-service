package gov.va.api.lighthouse.emis;

import java.time.Duration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("emis")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = false)
public class EmisClientConfig {
  private Ssl ssl;

  private Service militaryInformationServiceV2;

  private Service veteranStatusServiceV1;

  private Duration connectionTimeout;

  private Duration readTimeout;

  /** Lazy getter for connection timeout; if no connection timeout defined, define a default. */
  public Duration getConnectionTimeout() {
    if (connectionTimeout == null) {
      connectionTimeout = Duration.ofSeconds(5);
    }
    return connectionTimeout;
  }

  /** Lazy getter for read timeout; if no read timeout defined, define a default. */
  public Duration getReadTimeout() {
    if (readTimeout == null) {
      readTimeout = Duration.ofSeconds(10);
    }
    return readTimeout;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Accessors(fluent = false)
  public static class KeyStore {
    private String path;

    private String password;

    private String keyPassword;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Accessors(fluent = false)
  public static class TrustStore {
    private String path;
    private String password;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Accessors(fluent = false)
  public static class Service {
    private String url;

    private String wsdl;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Accessors(fluent = false)
  public static class Ssl {
    @Builder.Default private boolean enabled = true;

    private KeyStore keyStore;

    private TrustStore trustStore;
  }
}
