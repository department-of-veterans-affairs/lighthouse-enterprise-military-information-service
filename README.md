# lighthouse-enterprise-military-information-service
Library used to facilitate interfacing with the EMIS (Enterprise Military Information Service) SOAP Services.

#### Modules
* emis-model - Generates java classes from:
    * V2 Military Information Service `(eMISMilitaryInformationService.wsdl)`
    * V2 eMIS DoD Adapter `(emisDidAdapter.wsdl)`
    * V1 Veteran Status Service `(eMISVeteranStatusService.wsdl)`.
* emis-client-core - Utility library for shared classes between the different emis client versions.
* emis-client-v1 -  Provides a service class that can be imported into applications that require calling an eMIS SOAP Service.
Supported services include:
    * V1 Veteran Status request
* emis-client-v2 -  Provides a service class that can be imported into applications that require calling an eMIS SOAP Service.
Supported services include:
    * V2 Deployments request
    * V2 Service Episodes request
