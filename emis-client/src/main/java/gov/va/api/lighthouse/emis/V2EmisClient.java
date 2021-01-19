package gov.va.api.lighthouse.emis;

import gov.va.viers.cdi.emis.requestresponse.v2.EMISdeploymentResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISserviceEpisodeResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.InputEdiPiOrIcn;

public interface V2EmisClient {
  EMISdeploymentResponseType deploymentRequest(InputEdiPiOrIcn ediPiOrIcn);

  EMISserviceEpisodeResponseType serviceEpisodesRequest(InputEdiPiOrIcn ediPiOrIcn);
}
