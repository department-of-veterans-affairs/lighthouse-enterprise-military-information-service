package gov.va.api.lighthouse.emis;

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
@ConfigurationProperties("emis.v2")
@Data
@Accessors(fluent = false)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class EmisConfigV2 {
  String url;
  String wsdlLocation;
  @Builder.Default boolean sslEnabled = true;
  String keystorePath;
  String keystorePassword;
  String keyAlias;
  String truststorePath;
  String truststorePassword;
}
