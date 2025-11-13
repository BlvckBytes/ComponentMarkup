package at.blvckbytes.component_markup.test_utils;

import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;

public class NullInterpreterLogger implements InterpreterLogger {

  public static final NullInterpreterLogger INSTANCE = new NullInterpreterLogger();

  private NullInterpreterLogger() {}

  @Override
  public void logErrorScreen(InputView positionProvider, String message) {}

  @Override
  public void logErrorScreen(InputView positionProvider, String message, Throwable e) {}
}
