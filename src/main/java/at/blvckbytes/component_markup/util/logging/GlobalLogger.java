package at.blvckbytes.component_markup.util.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GlobalLogger {

  private static Logger logger = Logger.getAnonymousLogger();

  public static void log(Level level, String message, Throwable throwable) {
    logger.log(level, message, throwable);
  }

  public static void log(Level level, String message, boolean printStackTrace) {
    logger.log(level, message);

    if (!printStackTrace)
      return;

    for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace())
      logger.log(level, "    at " + stackTraceElement);
  }

  public static void log(Level level, String message) {
    log(level, message, true);
  }

  public static void set(Logger logger) {
    GlobalLogger.logger = logger;
  }
}
