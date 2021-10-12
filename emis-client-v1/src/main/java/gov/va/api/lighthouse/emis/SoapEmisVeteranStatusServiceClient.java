package gov.va.api.lighthouse.emis;

import com.sun.xml.ws.wsdl.parser.InaccessibleWSDLException;
import emisveteranstatusservice.EMISVeteranStatusServicePortTypes;
import emisveteranstatusservice.EMISVeteranStatusServicePortTypes_Service;
import gov.va.viers.cdi.emis.requestresponse.v1.EMISveteranStatusResponseType;
import gov.va.viers.cdi.emis.requestresponse.v1.InputEdiPiOrIcn;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Optional;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.xml.ws.BindingProvider;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;

@Getter
@Slf4j
public class SoapEmisVeteranStatusServiceClient implements EmisVeteranStatusServiceClient {
  private final Optional<SSLContext> sslContext;

  private final EmisConfigV1 config;

  private SoapEmisVeteranStatusServiceClient(EmisConfigV1 config) {
    this.config = config;
    this.sslContext = createSslContext();
    sslContext.ifPresent(
        context ->
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(
                context.getSocketFactory()));
  }

  public static SoapEmisVeteranStatusServiceClient of(EmisConfigV1 config) {
    return new SoapEmisVeteranStatusServiceClient(config);
  }

  /** Configure SSL for SOAP communication with EMIS. */
  @SneakyThrows
  private Optional<SSLContext> createSslContext() {
    if (!config.isSslEnabled()) {
      return Optional.empty();
    }
    try (InputStream keystoreInputStream =
            ResourceUtils.getURL(config.getKeystorePath()).openStream();
        InputStream truststoreInputStream =
            ResourceUtils.getURL(config.getTruststorePath()).openStream()) {
      // Keystore
      KeyStore keystore = KeyStore.getInstance("JKS");
      keystore.load(keystoreInputStream, config.getKeystorePassword().toCharArray());
      KeyManagerFactory kmf =
          KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(keystore, config.getKeystorePassword().toCharArray());
      final X509KeyManager origKm = (X509KeyManager) kmf.getKeyManagers()[0];
      // Truststore
      KeyStore truststore = KeyStore.getInstance("JKS");
      truststore.load(truststoreInputStream, config.getTruststorePassword().toCharArray());
      TrustManagerFactory trustManagerFactory =
          TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(truststore);
      // SSL Context
      SSLContext sslContext = SSLContext.getInstance("TLS");
      X509KeyManager keyManager =
          new X509KeyManager() {
            @Override
            public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
              return config.getKeyAlias();
            }

            @Override
            public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
              return origKm.chooseServerAlias(keyType, issuers, socket);
            }

            @Override
            public X509Certificate[] getCertificateChain(String alias) {
              return origKm.getCertificateChain(alias);
            }

            @Override
            public String[] getClientAliases(String keyType, Principal[] issuers) {
              return origKm.getClientAliases(keyType, issuers);
            }

            @Override
            public PrivateKey getPrivateKey(String alias) {
              return origKm.getPrivateKey(alias);
            }

            @Override
            public String[] getServerAliases(String keyType, Principal[] issuers) {
              return origKm.getServerAliases(keyType, issuers);
            }
          };
      sslContext.init(
          new KeyManager[] {keyManager},
          trustManagerFactory.getTrustManagers(),
          new SecureRandom());
      return Optional.of(sslContext);
    } catch (IOException | GeneralSecurityException e) {
      log.error("Failed to create SSL context", e);
      throw e;
    }
  }

  @SneakyThrows
  @Override
  public ResponseEntity<String> health() {
    port();
    return ResponseEntity.ok("Up");
  }

  @SneakyThrows
  private EMISVeteranStatusServicePortTypes port() {
    try {
      EMISVeteranStatusServicePortTypes port =
          new EMISVeteranStatusServicePortTypes_Service(new URL(config.getWsdlLocation()))
              .getEMISVeteranStatusServicePort();
      BindingProvider bp = (BindingProvider) port;
      if (sslContext().isPresent()) {
        SSLSocketFactory socketFactory = sslContext().get().getSocketFactory();
        bp.getRequestContext()
            .put(com.sun.xml.ws.developer.JAXWSProperties.SSL_SOCKET_FACTORY, socketFactory);
      }
      bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, config.getUrl());
      return port;
    } catch (InaccessibleWSDLException e) {
      log.error("WSDL is inaccessible: {}", e.getMessage());
      e.getErrors().forEach(t -> log.error("Reason {}", t.getMessage()));
      throw e;
    } catch (MalformedURLException e) {
      log.error("Failed to create port for {}", config.getWsdlLocation(), e);
      throw e;
    }
  }

  @Override
  public EMISveteranStatusResponseType veteranStatusRequest(InputEdiPiOrIcn ediPiOrIcn) {
    return port().getVeteranStatus(ediPiOrIcn);
  }
}
