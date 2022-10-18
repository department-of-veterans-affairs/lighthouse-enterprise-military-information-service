package gov.va.api.lighthouse.emis;

import com.sun.xml.ws.fault.ServerSOAPFaultException;
import com.sun.xml.ws.wsdl.parser.InaccessibleWSDLException;
import emismilitaryinformationserivce.EMISMilitaryInformationSerivcePortTypes_Service;
import emisveteranstatusservice.EMISVeteranStatusServicePortTypes_Service;
import gov.va.viers.cdi.emis.requestresponse.v1.EMISveteranStatusResponseType;
import gov.va.viers.cdi.emis.requestresponse.v1.InputEdiPiOrIcn;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISdeploymentResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISguardReserveServicePeriodsResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISserviceEpisodeResponseType;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.function.Function;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.ws.BindingProvider;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ResourceUtils;

@Slf4j
public class SoapEmisClient implements EmisClient {
  private final Optional<SSLContext> sslContext;

  private final EmisClientConfig config;

  private SoapEmisClient(EmisClientConfig config) {
    this.config = config;
    this.sslContext = createSslContext(config.getSsl());
  }

  public static SoapEmisClient of(EmisClientConfig config) {
    return new SoapEmisClient(config);
  }

  @SneakyThrows
  private Optional<SSLContext> createSslContext(EmisClientConfig.Ssl ssl) {
    if (!ssl.isEnabled()) {
      return Optional.empty();
    }
    log.info("Initializing eMIS SSL");
    try (var inputStream = ResourceUtils.getURL(ssl.getKeyStore().getPath()).openStream()) {
      var ks = KeyStore.getInstance("JKS");
      ks.load(inputStream, ssl.getKeyStore().getPassword().toCharArray());
      var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(ks, ssl.getKeyStore().getKeyPassword().toCharArray());
      var trustStream = ResourceUtils.getURL(ssl.getTrustStore().getPath()).openStream();
      var ts = KeyStore.getInstance("JKS");
      ts.load(trustStream, ssl.getTrustStore().getPassword().toCharArray());
      var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(ts);
      var sslContext = SSLContext.getInstance("TLS");
      sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
      return Optional.of(sslContext);
    }
  }

  @Override
  public EMISdeploymentResponseType deploymentRequest(
      gov.va.viers.cdi.emis.requestresponse.v2.InputEdiPiOrIcn ediPiOrIcn) {
    try {
      var port =
          port(
              config.getMilitaryInformationServiceV2(),
              url ->
                  new EMISMilitaryInformationSerivcePortTypes_Service(url)
                      .getEMISMilitaryInformationSerivcePort());
      return port.getDeployment(ediPiOrIcn);
    } catch (ServerSOAPFaultException e) {
      log.error("Received invalid response from service: {}", e.getMessage());
      throw new Exceptions.InvalidServiceResponse(e.getMessage(), e);
    }
  }

  @Override
  public EMISguardReserveServicePeriodsResponseType guardReserveServiceRequest(
      gov.va.viers.cdi.emis.requestresponse.v2.InputEdiPiOrIcn ediPiOrIcn) {
    try {
      var port =
          port(
              config.getMilitaryInformationServiceV2(),
              url ->
                  new EMISMilitaryInformationSerivcePortTypes_Service(url)
                      .getEMISMilitaryInformationSerivcePort());
      return port.getGuardReserveServicePeriods(ediPiOrIcn);
    } catch (ServerSOAPFaultException e) {
      log.error("Received invalid response from service: {}", e.getMessage());
      throw new Exceptions.InvalidServiceResponse(e.getMessage(), e);
    }
  }

  @Override
  public ResponseEntity<String> militaryInformationServiceV2Health() {
    try {
      port(
          config.getMilitaryInformationServiceV2(),
          url ->
              new EMISMilitaryInformationSerivcePortTypes_Service(url)
                  .getEMISMilitaryInformationSerivcePort());
    } catch (Exceptions.InaccessibleServiceDefinition e) {
      throw e;
    } catch (Exceptions.InvalidUrl e) {
      throw e;
    }
    return ResponseEntity.ok("Up");
  }

  @SneakyThrows
  private <T> T port(EmisClientConfig.Service svc, Function<URL, T> toPort) {
    try {
      T port = toPort.apply(new URL(svc.getWsdl()));
      final var bindingProvider = ((BindingProvider) port);
      sslContext.ifPresent(
          context ->
              bindingProvider
                  .getRequestContext()
                  .put(
                      "com.sun.xml.ws.transport.https.client.SSLSocketFactory",
                      context.getSocketFactory()));
      bindingProvider
          .getRequestContext()
          .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, svc.getUrl());

      bindingProvider
          .getRequestContext()
          .put(
              com.sun.xml.ws.developer.JAXWSProperties.CONNECT_TIMEOUT,
              (int) config.getConnectionTimeout().toMillis());

      bindingProvider
          .getRequestContext()
          .put(
              com.sun.xml.ws.developer.JAXWSProperties.REQUEST_TIMEOUT,
              (int) config.getReadTimeout().toMillis());

      return port;
    } catch (InaccessibleWSDLException e) {
      log.error("WSDL is inaccessible: {}", e.getMessage());
      e.getErrors().forEach(t -> log.error("Reason {}", t.getMessage()));
      throw new Exceptions.InaccessibleServiceDefinition(svc.getWsdl(), e);
    } catch (MalformedURLException e) {
      log.error("Failed to create port for {}", svc.getWsdl(), e);
      throw new Exceptions.InvalidUrl(svc.getWsdl(), e);
    }
  }

  @Override
  public EMISserviceEpisodeResponseType serviceEpisodesRequest(
      gov.va.viers.cdi.emis.requestresponse.v2.InputEdiPiOrIcn ediPiOrIcn) {
    try {
      var port =
          port(
              config.getMilitaryInformationServiceV2(),
              url ->
                  new EMISMilitaryInformationSerivcePortTypes_Service(url)
                      .getEMISMilitaryInformationSerivcePort());
      return port.getMilitaryServiceEpisodes(ediPiOrIcn);
    } catch (ServerSOAPFaultException e) {
      log.error("Received invalid response from service: {}", e.getMessage());
      throw new Exceptions.InvalidServiceResponse(e.getMessage(), e);
    }
  }

  @Override
  public EMISveteranStatusResponseType veteranStatusRequest(InputEdiPiOrIcn ediPiOrIcn) {
    try {
      var port =
          port(
              config.getVeteranStatusServiceV1(),
              url ->
                  new EMISVeteranStatusServicePortTypes_Service(url)
                      .getEMISVeteranStatusServicePort());
      return port.getVeteranStatus(ediPiOrIcn);
    } catch (ServerSOAPFaultException e) {
      log.error("Received invalid response from service: {}", e.getMessage());
      throw new Exceptions.InvalidServiceResponse(e.getMessage(), e);
    }
  }

  @Override
  public ResponseEntity<String> veteranStatusServiceV1Health() {
    try {
      port(
          config.getVeteranStatusServiceV1(),
          url ->
              new EMISVeteranStatusServicePortTypes_Service(url).getEMISVeteranStatusServicePort());
    } catch (Exceptions.InaccessibleServiceDefinition e) {
      throw e;
    } catch (Exceptions.InvalidUrl e) {
      throw e;
    }
    return ResponseEntity.ok("Up");
  }
}
