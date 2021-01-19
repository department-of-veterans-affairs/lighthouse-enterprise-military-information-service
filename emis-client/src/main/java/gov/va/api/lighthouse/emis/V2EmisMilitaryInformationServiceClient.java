package gov.va.api.lighthouse.emis;

import emismilitaryinformationserivce.EMISMilitaryInformationSerivcePortTypes;
import emismilitaryinformationserivce.EMISMilitaryInformationSerivcePortTypes_Service;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISdeploymentResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISserviceEpisodeResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.InputEdiPiOrIcn;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import javax.xml.ws.BindingProvider;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.util.ResourceUtils;

@Getter
public class V2EmisMilitaryInformationServiceClient implements V2EmisClient {
  private final SSLContext sslContext;

  private final EmisConfig config;

  private V2EmisMilitaryInformationServiceClient(EmisConfig config) {
    this.config = config;
    this.sslContext = createSslContext();
    javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
  }

  public static V2EmisMilitaryInformationServiceClient of(EmisConfig config) {
    return new V2EmisMilitaryInformationServiceClient(config);
  }

  @SneakyThrows
  private SSLContext createSslContext() {
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
      return sslContext;
    }
  }

  @Override
  public EMISdeploymentResponseType deploymentRequest(InputEdiPiOrIcn ediPiOrIcn) {
    return port().getDeployment(ediPiOrIcn);
  }

  @SneakyThrows
  private EMISMilitaryInformationSerivcePortTypes port() {
    EMISMilitaryInformationSerivcePortTypes port =
        new EMISMilitaryInformationSerivcePortTypes_Service(new URL(config.getWsdlLocation()))
            .getEMISMilitaryInformationSerivcePort();
    BindingProvider bp = (BindingProvider) port;
    bp.getRequestContext()
        .put(
            com.sun.xml.ws.developer.JAXWSProperties.SSL_SOCKET_FACTORY,
            sslContext().getSocketFactory());
    bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, config.getUrl());
    return port;
  }

  @Override
  public EMISserviceEpisodeResponseType serviceEpisodesRequest(InputEdiPiOrIcn ediPiOrIcn) {
    return port().getMilitaryServiceEpisodes(ediPiOrIcn);
  }
}
