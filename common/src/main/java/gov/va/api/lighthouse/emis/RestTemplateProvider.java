package gov.va.api.lighthouse.emis;

import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class RestTemplateProvider {
  Long connectionTimeout;
  Long readTimeout;
  Optional<SSLContext> sslContext;

  /** constructor. */
  public RestTemplateProvider(
      Long connectionTimeout, Long readTimeout, Optional<SSLContext> sslContext) {
    this.connectionTimeout = connectionTimeout;
    this.readTimeout = readTimeout;
    this.sslContext = sslContext;
  }

  private Supplier<ClientHttpRequestFactory> bufferingRequestFactory(HttpClient client) {
    return () ->
        new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory(client));
  }

  @SneakyThrows
  private CloseableHttpClient httpClientWithoutSsl() {
    return sslContext.isPresent()
        ? HttpClients.custom().setSSLContext(sslContext.get()).build()
        : HttpClients.custom()
            .setSSLSocketFactory(
                new SSLConnectionSocketFactory(
                    SSLContexts.custom()
                        .loadTrustMaterial(null, (X509Certificate[] chain, String authType) -> true)
                        .build()))
            .build();
  }

  /** Create RestTemplate. */
  public RestTemplate restTemplate() {
    RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
    Duration connectionTimeout = Duration.ofSeconds(this.connectionTimeout);
    Duration readTimeout = Duration.ofSeconds(this.readTimeout);
    log.info("Creating RestTemplate using: {}", getClass().getSimpleName());
    if (connectionTimeout != null) {
      log.info("Setting connection timeout to {} seconds.", connectionTimeout);
      restTemplateBuilder = restTemplateBuilder.setConnectTimeout(connectionTimeout);
    }
    if (readTimeout != null) {
      log.info("Setting read timeout to {} seconds.", readTimeout);
      restTemplateBuilder = restTemplateBuilder.setReadTimeout(readTimeout);
    }
    return restTemplateBuilder
        .requestFactory(bufferingRequestFactory(httpClientWithoutSsl()))
        .build();
  }
}
