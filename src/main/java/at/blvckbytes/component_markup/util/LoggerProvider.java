package at.blvckbytes.component_markup.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerProvider {

  private static Logger logger = Logger.getAnonymousLogger();

  public static void log(Level level, String message, Throwable throwable) {
    logger.log(level, message, throwable);
  }

  public static void log(Level level, String message) {
    logger.log(level, message);

    for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
      logger.log(level, "    at " + stackTraceElement);
    }
  }

  public static void set(Logger logger) {
    LoggerProvider.logger = logger;
  }
}
