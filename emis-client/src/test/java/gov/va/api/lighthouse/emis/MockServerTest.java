package gov.va.api.lighthouse.emis;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import gov.va.viers.cdi.emis.requestresponse.v1.EMISveteranStatusResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISdeploymentResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISguardReserveServicePeriodsResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.EMISserviceEpisodeResponseType;
import gov.va.viers.cdi.emis.requestresponse.v2.InputEdiPiOrIcn;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.xml.ws.WebServiceException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class MockServerTest {
  private static WireMockServer wireMockServer;

  @AfterAll
  public static void after() {
    if (wireMockServer != null) {
      wireMockServer.shutdown();
    }
  }

  @BeforeAll
  @SneakyThrows
  public static void setupWiremock() {
    log.info("setting up wiremock");
    wireMockServer = new WireMockServer(options().dynamicPort().notifier(new Slf4jNotifier(true)));
    var claimant_response =
        """
      <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope">
          <env:Header>
              <NS2:essResponseCode xmlns:NS2="http://va.gov/ess/message/v1">Success</NS2:essResponseCode>
          </env:Header>
          <env:Body>
              <eMISserviceEpisodeResponse xmlns="http://viers.va.gov/cdi/eMIS/RequestResponse/MilitaryInfo/v2"
                                          xmlns:ns2="http://viers.va.gov/cdi/eMIS/commonService/v2"
                                          xmlns:ns3="http://viers.va.gov/cdi/eMIS/RequestResponse/v2"
                                          xmlns:ns4="http://viers.va.gov/cdi/CDI/commonService/v2">
                  <ns3:militaryServiceEpisode>
                      <ns2:edipi>1005490754</ns2:edipi>
                      <ns2:keyData>
                          <ns2:personnelOrganizationCode>12</ns2:personnelOrganizationCode>
                          <ns2:personnelCategoryTypeCode>A</ns2:personnelCategoryTypeCode>
                          <ns2:personnelSegmentIdentifier>1</ns2:personnelSegmentIdentifier>
                      </ns2:keyData>
                      <ns2:militaryServiceEpisodeData>
                          <ns2:serviceEpisodeStartDate>1992-08-26</ns2:serviceEpisodeStartDate>
                          <ns2:serviceEpisodeEndDate>2017-08-30</ns2:serviceEpisodeEndDate>
                          <ns2:serviceEpisodeTerminationReason>S</ns2:serviceEpisodeTerminationReason>
                          <ns2:branchOfServiceCode>F</ns2:branchOfServiceCode>
                          <ns2:personnelProjectedEndDate>2017-08-31</ns2:personnelProjectedEndDate>
                          <ns2:personnelProjectedEndDateCertaintyCode>R</ns2:personnelProjectedEndDateCertaintyCode>
                          <ns2:dischargeCharacterOfServiceCode>A</ns2:dischargeCharacterOfServiceCode>
                          <ns2:personnelStatusChangeTransactionTypeCode>137</ns2:personnelStatusChangeTransactionTypeCode>
                          <ns2:narrativeReasonForSeparationCode>033</ns2:narrativeReasonForSeparationCode>
                          <ns2:narrativeReasonForSeparationTxt>SUFFICIENT SERVICE FOR RETIREMENT
                          </ns2:narrativeReasonForSeparationTxt>
                          <ns2:post911GIBillLossCategoryCode>06</ns2:post911GIBillLossCategoryCode>
                          <ns2:mgadLossCategoryCode>08</ns2:mgadLossCategoryCode>
                          <ns2:payPlanCode>ME</ns2:payPlanCode>
                          <ns2:payGradeCode>08</ns2:payGradeCode>
                          <ns2:serviceRankNameCode>SMSGT</ns2:serviceRankNameCode>
                          <ns2:serviceRankNameTxt>Senior Master Sergeant</ns2:serviceRankNameTxt>
                          <ns2:payGradeDate>2014-04-01</ns2:payGradeDate>
                          <ns2:activeDutyServiceAgreementQuantity>4</ns2:activeDutyServiceAgreementQuantity>
                          <ns2:uniformServiceInitialEntryDate>1992-05-28</ns2:uniformServiceInitialEntryDate>
                          <ns2:militaryAccessionSourceCode>00</ns2:militaryAccessionSourceCode>
                          <ns2:personnelBeginDateSource>7</ns2:personnelBeginDateSource>
                          <ns2:personnelTerminationDateSourceCode>9</ns2:personnelTerminationDateSourceCode>
                          <ns2:activeFederalMilitaryServiceBaseDate>1992-08-26</ns2:activeFederalMilitaryServiceBaseDate>
                          <ns2:mgsrServiceAgreementDurationYearQuantityCode>Z
                          </ns2:mgsrServiceAgreementDurationYearQuantityCode>
                          <ns2:reserveUnderAge60Code>W</ns2:reserveUnderAge60Code>
                      </ns2:militaryServiceEpisodeData>
                  </ns3:militaryServiceEpisode>
              </eMISserviceEpisodeResponse>
          </env:Body>
      </env:Envelope>
      """;

    var status_response =
        """
        <env:Envelope xmlns:env="http://www.w3.org/2003/05/soap-envelope">
            <env:Header>
                <NS2:essResponseCode xmlns:NS2="http://va.gov/ess/message/v1">Success</NS2:essResponseCode>
            </env:Header>
            <env:Body xmlns:ns2="http://viers.va.gov/cdi/eMIS/RequestResponse/v1"
                xmlns:ns4="http://viers.va.gov/cdi/eMIS/commonService/v1"
                xmlns:ns3="http://viers.va.gov/cdi/CDI/commonService/v1">
                <ns2:veteranStatus>
                    <ns4:edipi>1242991946</ns4:edipi>
                    <ns4:title38StatusCode>V1</ns4:title38StatusCode>
                    <ns4:post911DeploymentIndicator>Y</ns4:post911DeploymentIndicator>
                    <ns4:post911CombatIndicator>N</ns4:post911CombatIndicator>
                    <ns4:pre911DeploymentIndicator>N</ns4:pre911DeploymentIndicator>
                </ns2:veteranStatus>
            </env:Body>
        </env:Envelope>
        """;

    String claimant_wsdl =
        String.join(
            "",
            Files.readAllLines(
                Paths.get(
                    "../emis-model/src/main/resources/META-INF/wsdl/v2/eMISMilitaryInformationService.wsdl")));

    String status_wsdl =
        String.join(
            "",
            Files.readAllLines(
                Paths.get(
                    "../emis-model/src/main/resources/META-INF/wsdl/v1/eMISVeteranStatusService.wsdl")));

    stubRequest(
        wireMockServer,
        WireMock::post,
        claimant_response,
        "/VIERSService/eMIS/v2/MilitaryInformationService",
        Duration.ofSeconds(1));

    stubRequest(
        wireMockServer,
        WireMock::get,
        claimant_wsdl,
        "/VIERSService/eMIS/v2/MilitaryInformationService.wsdl",
        Duration.ZERO);

    stubRequest(
        wireMockServer,
        WireMock::post,
        status_response,
        "/VIERSService/eMIS/v1/VeteranStatusService",
        Duration.ofSeconds(0));

    stubRequest(
        wireMockServer,
        WireMock::get,
        status_wsdl,
        "/VIERSService/eMIS/v1/eMISVeteranStatusService.wsdl",
        Duration.ZERO);
    wireMockServer.start();
  }

  private static void stubRequest(
      final WireMockServer wireMockServer,
      Function<UrlPattern, MappingBuilder> mappingFunction,
      final String responseBody,
      final String path,
      final Duration delay) {
    var mappingBuilder = mappingFunction.apply(urlMatching(path));
    wireMockServer.stubFor(
        mappingBuilder.willReturn(
            aResponse().withFixedDelay((int) delay.toMillis()).withBody(responseBody)));
  }

  private EmisClientConfig getBaseConfig(Duration connectionTimeout, Duration readTimeout) {
    return EmisClientConfig.builder()
        .veteranStatusServiceV1(
            EmisClientConfig.Service.builder()
                .url(
                    "http://localhost:"
                        + wireMockServer.port()
                        + "/VIERSService/eMIS/v1/VeteranStatusService")
                .wsdl(
                    "http://localhost:"
                        + wireMockServer.port()
                        + "/VIERSService/eMIS/v1/eMISVeteranStatusService.wsdl")
                .build())
        .militaryInformationServiceV2(
            EmisClientConfig.Service.builder()
                .url(
                    "http://localhost:"
                        + wireMockServer.port()
                        + "/VIERSService/eMIS/v2/MilitaryInformationService")
                .wsdl(
                    "http://localhost:"
                        + wireMockServer.port()
                        + "/VIERSService/eMIS/v2/MilitaryInformationService.wsdl")
                .build())
        .ssl(
            EmisClientConfig.Ssl.builder()
                .enabled(false)
                .keyStore(
                    EmisClientConfig.KeyStore.builder()
                        .path("classpath:gov/va/api/lighthouse/bgs/DummyTestStore.jks")
                        .password("password")
                        .keyPassword("password")
                        .build())
                .trustStore(
                    EmisClientConfig.TrustStore.builder()
                        .path("classpath:gov/va/api/lighthouse/bgs/DummyTestStore.jks")
                        .password("password")
                        .build())
                .build())
        .connectionTimeout(connectionTimeout)
        .readTimeout(readTimeout)
        .build();
  }

  @Test
  public void testThatInspectorsAreHit() {
    Consumer<InputEdiPiOrIcn> deploymentRequestInspector = mock(Consumer.class);
    Consumer<EMISdeploymentResponseType> deploymentResponseInspector = mock(Consumer.class);
    Consumer<gov.va.viers.cdi.emis.requestresponse.v2.InputEdiPiOrIcn>
        guardReserveServiceRequestInspector = mock(Consumer.class);
    Consumer<EMISguardReserveServicePeriodsResponseType> guardReserveServiceResponseInspector =
        mock(Consumer.class);
    Consumer<gov.va.viers.cdi.emis.requestresponse.v2.InputEdiPiOrIcn>
        serviceEpisodesRequestInspector = mock(Consumer.class);
    Consumer<EMISserviceEpisodeResponseType> serviceEpisodesResponseInspector =
        mock(Consumer.class);
    Consumer<gov.va.viers.cdi.emis.requestresponse.v1.InputEdiPiOrIcn>
        veteranStatusRequestInspector = mock(Consumer.class);
    Consumer<EMISveteranStatusResponseType> veteranStatusResponseInspector = mock(Consumer.class);

    var config = getBaseConfig(Duration.ofSeconds(10), Duration.ofSeconds(10));
    SoapEmisClient client =
        SoapEmisClient.builder()
            .deploymentRequestInspector(deploymentRequestInspector)
            .deploymentResponseInspector(deploymentResponseInspector)
            .guardReserveServiceRequestInspector(guardReserveServiceRequestInspector)
            .guardReserveServiceResponseInspector(guardReserveServiceResponseInspector)
            .serviceEpisodesRequestInspector(serviceEpisodesRequestInspector)
            .serviceEpisodesResponseInspector(serviceEpisodesResponseInspector)
            .veteranStatusRequestInspector(veteranStatusRequestInspector)
            .veteranStatusResponseInspector(veteranStatusResponseInspector)
            .config(config)
            .build();
    var request = InputEdiPiOrIcn.builder().build();

    client.deploymentRequest(request);
    verify(deploymentRequestInspector, times(1)).accept(any());
    verify(deploymentResponseInspector, times(1)).accept(any());


    client.guardReserveServiceRequest(request);
    verify(guardReserveServiceRequestInspector, times(1)).accept(any());
    verify(guardReserveServiceResponseInspector, times(1)).accept(any());


    client.serviceEpisodesRequest(request);
    verify(serviceEpisodesRequestInspector, times(1)).accept(any());
    verify(serviceEpisodesResponseInspector, times(1)).accept(any());


    client.veteranStatusRequest(
        gov.va.viers.cdi.emis.requestresponse.v1.InputEdiPiOrIcn.builder().build());
    verify(veteranStatusRequestInspector, times(1)).accept(any());
    verify(veteranStatusResponseInspector, times(1)).accept(any());
  }

  @Test
  public void timeOutsGreaterThanDelayDoNotTimeOut() {
    var config = getBaseConfig(Duration.ofSeconds(5), Duration.ofSeconds(10));
    SoapEmisClient client = SoapEmisClient.of(config);
    var result = client.serviceEpisodesRequest(InputEdiPiOrIcn.builder().build());
    assertThat(result).isNotNull();
  }

  @Test
  public void timeOutsLessThanDelayTimeOut() {
    var config = getBaseConfig(Duration.ofSeconds(5), Duration.ofMillis(500));
    SoapEmisClient client = SoapEmisClient.of(config);
    assertThatExceptionOfType(WebServiceException.class)
        .isThrownBy(() -> client.serviceEpisodesRequest(InputEdiPiOrIcn.builder().build()));
  }
}
