package at.blvckbytes.component_markup.util.logging;

import at.blvckbytes.component_markup.util.InputView;

public interface InterpreterLogger {

  void logErrorScreen(InputView positionProvider, String message);

  void logErrorScreen(InputView positionProvider, String message, Throwable e);

}
