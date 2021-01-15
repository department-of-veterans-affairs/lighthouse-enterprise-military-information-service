package gov.va.api.lighthouse.emis;

import emismilitaryinformationserivce.EMISMilitaryInformationSerivcePortTypes;
import emismilitaryinformationserivce.EMISMilitaryInformationSerivcePortTypes_Service;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISdeploymentResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISserviceEpisodeResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.InputEdiPiOrIcn;
import java.net.URL;
import javax.xml.ws.BindingProvider;
import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class V2EmisMilitaryInformationServiceClient implements V2EmisClient {
  private final EmisConfig config;

  private V2EmisMilitaryInformationServiceClient(EmisConfig config) {
    this.config = config;
  }

  @SneakyThrows
  private EMISMilitaryInformationSerivcePortTypes port() {
    EMISMilitaryInformationSerivcePortTypes port =
        new EMISMilitaryInformationSerivcePortTypes_Service(new URL(
            config.getWsdlLocation()
        )).getEMISMilitaryInformationSerivcePort();
    BindingProvider bp = (BindingProvider) port;
    /*bp.getRequestContext()
        .put(
            com.sun.xml.ws.developer.JAXWSProperties.SSL_SOCKET_FACTORY,
            sslContext().getSocketFactory());*/
    bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, config.getUrl());
    return port;
  }

  @Override
  public EMISdeploymentResponseType deploymentRequest(InputEdiPiOrIcn ediPiOrIcn) {
    return port().getDeployment(ediPiOrIcn);
  }

  @Override
  public EMISserviceEpisodeResponseType serviceEpisodesRequest(InputEdiPiOrIcn ediPiOrIcn) {
    return port().getMilitaryServiceEpisodes(ediPiOrIcn);
  }
}
