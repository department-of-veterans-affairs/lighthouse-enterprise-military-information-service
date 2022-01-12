package gov.va.api.lighthouse.emis;

import gov.va.viers.cdi.emis.requestresponse.v1.EMISveteranStatusResponseType;
import gov.va.viers.cdi.emis.requestresponse.v1.InputEdiPiOrIcn;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISdeploymentResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISguardReserveServicePeriodsResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISserviceEpisodeResponseType;
import org.springframework.http.ResponseEntity;

public interface EmisClient {
  EMISdeploymentResponseType deploymentRequest(
      gov.va.viers.cdi.emis.requestresponse.v2.InputEdiPiOrIcn ediPiOrIcn);

  EMISguardReserveServicePeriodsResponseType guardReserveServiceRequest(
      gov.va.viers.cdi.emis.requestresponse.v2.InputEdiPiOrIcn ediPiOrIcn);

  ResponseEntity<String> militaryInformationServiceV2Health();

  EMISserviceEpisodeResponseType serviceEpisodesRequest(
      gov.va.viers.cdi.emis.requestresponse.v2.InputEdiPiOrIcn ediPiOrIcn);

  EMISveteranStatusResponseType veteranStatusRequest(InputEdiPiOrIcn ediPiOrIcn);

  ResponseEntity<String> veteranStatusServiceV1Health();
}
