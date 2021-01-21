package gov.va.api.lighthouse.emis;

import gov.va.viers.cdi.emis.requestresponse.v1.EMISveteranStatusResponseType;
import gov.va.viers.cdi.emis.requestresponse.v1.InputEdiPiOrIcn;

public interface EmisVeteranStatusServiceClient {
  EMISveteranStatusResponseType veteranStatusRequest(InputEdiPiOrIcn ediPiOrIcn);
}
