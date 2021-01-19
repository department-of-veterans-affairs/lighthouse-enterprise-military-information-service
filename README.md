# lighthouse-enterprise-military-information-service
Library used to facilitate interfacing with the EMIS (Enterprise Military Information Service) SOAP Services.

#### Modules
* emis-model - Generates java classes from:
    * V2 Military Information Service `(eMISMilitaryInformationService.wsdl)`
    * V2 eMIS DoD Adapter `(emisDidAdapter.wsdl)`
    * V1 Veteran Status Service `(eMISVeteranStatusService.wsdl)`.
* emis-client -  Provides a service class that can be imported into applications that require calling an eMIS SOAP Service.
Supported services include:
    * V2 Deployments request
    * V2 Service Episodes request
    * V1 Veteran Status request
