package gov.va.api.lighthouse.emis;

import gov.va.viers.cdi.emis.requestresponse.v1.EMISveteranStatusResponseType;
import gov.va.viers.cdi.emis.requestresponse.v1.InputEdiPiOrIcn;
import java.io.IOException;
import org.apache.http.HttpResponse;

public interface EmisVeteranStatusServiceClient {
  EMISveteranStatusResponseType veteranStatusRequest(InputEdiPiOrIcn ediPiOrIcn);

  HttpResponse wsdl() throws IOException;
}
