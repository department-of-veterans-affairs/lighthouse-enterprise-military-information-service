package gov.va.api.lighthouse.emis;

import gov.va.viers.cdi.emis.requestresponse.v1.EMISveteranStatusResponseType;
import gov.va.viers.cdi.emis.requestresponse.v1.InputEdiPiOrIcn;
import java.net.MalformedURLException;
import org.springframework.http.ResponseEntity;

public interface EmisVeteranStatusServiceClient {
  ResponseEntity<String> health() throws MalformedURLException;

  EMISveteranStatusResponseType veteranStatusRequest(InputEdiPiOrIcn ediPiOrIcn);
}
