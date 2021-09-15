package gov.va.api.lighthouse.emis;

import gov.va.viers.cdi.emis.requestresponse.v2.EMISdeploymentResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISguardReserveServicePeriodsResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISserviceEpisodeResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.InputEdiPiOrIcn;

public interface EmisMilitaryInformationServiceClient {
  EMISdeploymentResponseType deploymentRequest(InputEdiPiOrIcn ediPiOrIcn);

  EMISguardReserveServicePeriodsResponseType guardReserveServiceRequest(InputEdiPiOrIcn ediPiOrIcn);

  EMISserviceEpisodeResponseType serviceEpisodesRequest(InputEdiPiOrIcn ediPiOrIcn);
}
