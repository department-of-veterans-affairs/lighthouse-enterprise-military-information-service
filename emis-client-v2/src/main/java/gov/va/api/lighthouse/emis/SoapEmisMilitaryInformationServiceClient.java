package gov.va.api.lighthouse.emis;

import static gov.va.api.lighthouse.emis.EmisClient.createSslContext;

import emismilitaryinformationserivce.EMISMilitaryInformationSerivcePortTypes;
import emismilitaryinformationserivce.EMISMilitaryInformationSerivcePortTypes_Service;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISdeploymentResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISserviceEpisodeResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.InputEdiPiOrIcn;
import java.net.URL;
import javax.net.ssl.SSLContext;
import javax.xml.ws.BindingProvider;
import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class SoapEmisMilitaryInformationServiceClient
    implements EmisMilitaryInformationServiceClient {
  private final SSLContext sslContext;

  private final EmisConfig config;

  private SoapEmisMilitaryInformationServiceClient(EmisConfig config) {
    this.config = config;
    this.sslContext = createSslContext(config);
    javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
  }

  public static SoapEmisMilitaryInformationServiceClient of(EmisConfig config) {
    return new SoapEmisMilitaryInformationServiceClient(config);
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