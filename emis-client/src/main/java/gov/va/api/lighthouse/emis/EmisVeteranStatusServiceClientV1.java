package gov.va.api.lighthouse.emis;

import emisveteranstatusservice.EMISVeteranStatusServicePortTypes;
import emisveteranstatusservice.EMISVeteranStatusServicePortTypes_Service;
import gov.va.viers.cdi.emis.requestresponse.v1.EMISveteranStatusResponseType;
import gov.va.viers.cdi.emis.requestresponse.v1.InputEdiPiOrIcn;
import java.net.URL;
import javax.net.ssl.SSLContext;
import javax.xml.ws.BindingProvider;
import lombok.Getter;
import lombok.SneakyThrows;

@Getter
public class EmisVeteranStatusServiceClientV1 extends EmisClient {
  private final SSLContext sslContext;

  private final EmisConfig config;

  private EmisVeteranStatusServiceClientV1(EmisConfig config) {
    this.config = config;
    this.sslContext = createSslContext(config);
    javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
  }

  @SneakyThrows
  private EMISVeteranStatusServicePortTypes port() {
    EMISVeteranStatusServicePortTypes port =
        new EMISVeteranStatusServicePortTypes_Service(new URL(config.getWsdlLocation()))
            .getEMISVeteranStatusServicePort();
    BindingProvider bp = (BindingProvider) port;
    bp.getRequestContext()
        .put(
            com.sun.xml.ws.developer.JAXWSProperties.SSL_SOCKET_FACTORY,
            sslContext().getSocketFactory());
    bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, config.getUrl());
    return port;
  }

  public EMISveteranStatusResponseType veteranStatusRequest(InputEdiPiOrIcn ediPiOrIcn) {
    return port().getVeteranStatus(ediPiOrIcn);
  }
}
