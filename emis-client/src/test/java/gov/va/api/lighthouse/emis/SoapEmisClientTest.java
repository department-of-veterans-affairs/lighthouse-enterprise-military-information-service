/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package gov.va.api.lighthouse.emis;

import static org.junit.jupiter.api.Assertions.*;

import gov.va.viers.cdi.emis.requestresponse.v1.InputEdiPiOrIcn;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * @author Larry Crochet
 */
public class SoapEmisClientTest {

  @Test
  @EnabledIfSystemProperty(named = "interactive", matches = "true")
  public void soapEmisClientTest1() {

    EmisClientConfig config =
        EmisClientConfig.builder()
            .ssl(
                EmisClientConfig.Ssl.builder()
                    .enabled(true)
                    .keyStore(
                        EmisClientConfig.KeyStore.builder()
                            .path("")
                            .keyPassword("")
                            .password("")
                            .build())
                    .trustStore(EmisClientConfig.TrustStore.builder().path("").password("").build())
                    .build())
            .veteranStatusServiceV1(EmisClientConfig.Service.builder().wsdl("").url("").build())
            .build();
    EmisClient emisClient = SoapEmisClient.of(config);
    InputEdiPiOrIcn ediPiOrIcn = null;
    var result = emisClient.veteranStatusRequest(ediPiOrIcn);
    assertNotNull(result);
  }
}
