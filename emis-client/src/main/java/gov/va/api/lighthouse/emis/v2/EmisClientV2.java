package gov.va.api.lighthouse.emis.v2;

import gov.va.viers.cdi.emis.requestresponse.v2.EMISdeploymentResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISserviceEpisodeResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.InputEdiPiOrIcn;

public interface EmisClientV2 {
  EMISdeploymentResponseType deploymentRequest(InputEdiPiOrIcn ediPiOrIcn);

  EMISserviceEpisodeResponseType serviceEpisodesRequest(InputEdiPiOrIcn ediPiOrIcn);
}
