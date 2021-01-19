package gov.va.api.lighthouse.emis.v1;

import gov.va.viers.cdi.emis.requestresponse.v1.EMISveteranStatusResponseType;
import gov.va.viers.cdi.emis.requestresponse.v1.InputEdiPiOrIcn;

public interface EmisClientV1 {
  EMISveteranStatusResponseType veteranStatusRequest(InputEdiPiOrIcn ediPiOrIcn);
}
