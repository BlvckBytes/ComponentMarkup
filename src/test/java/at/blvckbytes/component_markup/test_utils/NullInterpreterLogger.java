package at.blvckbytes.component_markup.test_utils;

import at.blvckbytes.component_markup.util.InputView;
import at.blvckbytes.component_markup.util.logging.InterpreterLogger;
import org.jetbrains.annotations.Nullable;

public class NullInterpreterLogger implements InterpreterLogger {

  public static final NullInterpreterLogger INSTANCE = new NullInterpreterLogger();

  private NullInterpreterLogger() {}

  @Override
  public void log(InputView view, int position, String message, @Nullable Throwable e) {}

}
