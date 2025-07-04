package at.blvckbytes.component_markup.util;

import java.util.logging.Logger;

public class LoggerProvider {

  private static Logger logger = Logger.getAnonymousLogger();

  public static Logger get() {
    return logger;
  }

  public static void set(Logger logger) {
    LoggerProvider.logger = logger;
  }
}
