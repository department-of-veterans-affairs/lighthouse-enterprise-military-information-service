package gov.va.api.lighthouse.emis;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Exceptions {

  public static class EmisCommunicationException extends RuntimeException {
    public EmisCommunicationException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static class EmisConfigurationException extends RuntimeException {
    public EmisConfigurationException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static final class InaccessibleServiceDefinition extends EmisCommunicationException {
    public InaccessibleServiceDefinition(String wsdl, Throwable cause) {
      super("Cannot access service definition: " + wsdl, cause);
    }
  }

  public static final class InvalidServiceResponse extends EmisCommunicationException {
    public InvalidServiceResponse(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static final class InvalidUrl extends EmisConfigurationException {
    public InvalidUrl(String url, Throwable cause) {
      super("Invalid URL: " + url, cause);
    }
  }
}
