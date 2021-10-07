package gov.va.api.lighthouse.emis;

import gov.va.viers.cdi.emis.requestresponse.v1.EMISveteranStatusResponseType;
import gov.va.viers.cdi.emis.requestresponse.v1.InputEdiPiOrIcn;
import org.springframework.http.ResponseEntity;

public interface EmisVeteranStatusServiceClient {
  EMISveteranStatusResponseType veteranStatusRequest(InputEdiPiOrIcn ediPiOrIcn);

  ResponseEntity<String> wsdl();
}
