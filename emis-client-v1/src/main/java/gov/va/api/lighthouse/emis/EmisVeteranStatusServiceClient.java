package gov.va.api.lighthouse.emis;

import gov.va.viers.cdi.emis.requestresponse.v1.EMISveteranStatusResponseType;
import gov.va.viers.cdi.emis.requestresponse.v1.InputEdiPiOrIcn;
import org.apache.http.HttpResponse;

import java.io.IOException;

public interface EmisVeteranStatusServiceClient {
  HttpResponse wsdl()  throws IOException;
  EMISveteranStatusResponseType veteranStatusRequest(InputEdiPiOrIcn ediPiOrIcn);
}
