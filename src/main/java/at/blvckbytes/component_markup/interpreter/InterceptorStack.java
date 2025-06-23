package at.blvckbytes.component_markup.interpreter;

import at.blvckbytes.component_markup.ast.node.AstNode;

import java.util.*;

public class InterceptorStack {

  private static class InterceptorEntry {
    InterpreterInterceptor interceptor;
    Stack<EnumSet<InterceptionFlag>> flagStack;

    InterceptorEntry(InterpreterInterceptor interceptor) {
      this.interceptor = interceptor;
      this.flagStack = new Stack<>();
    }
  }

  private final OutputBuilder builder;
  private final Interpreter interpreter;
  private final Stack<InterceptorEntry> interceptorStack;

  public InterceptorStack(OutputBuilder builder, Interpreter interpreter) {
    this.builder = builder;
    this.interpreter = interpreter;
    this.interceptorStack = new Stack<>();
  }

  public void add(InterpreterInterceptor interceptor) {
    interceptorStack.add(new InterceptorEntry(interceptor));
  }

  public boolean handleBeforeAndGetIfSkip(AstNode node) {
    List<EnumSet<InterceptionFlag>> flagsList = new ArrayList<>();

    boolean skip = false;

    for (int i = 0; i < interceptorStack.size(); ++i) {
      InterceptorEntry entry = interceptorStack.get(i);
      EnumSet<InterceptionFlag> flags = entry.interceptor.interceptInterpretation(node, builder, interpreter);

      if (flags.contains(InterceptionFlag.SKIP_PROCESSING)) {
        while (interceptorStack.size() > i + 1)
          interceptorStack.pop().interceptor.onSkippedByOther(node, builder, interpreter);

        skip = true;
        break;
      }

      flagsList.add(flags);
    }

    for (int i = 0; i < interceptorStack.size(); ++i)
      interceptorStack.get(i).flagStack.push(flagsList.get(i));

    return skip;
  }

  public void handleAfter(AstNode node) {
    for (Iterator<InterceptorEntry> iterator = interceptorStack.iterator(); iterator.hasNext();) {
      InterceptorEntry entry = iterator.next();
      EnumSet<InterceptionFlag> flags = entry.flagStack.pop();

      if (flags.contains(InterceptionFlag.CALL_AFTER))
        entry.interceptor.afterInterpretation(node, builder, interpreter);

      if (entry.flagStack.isEmpty())
        iterator.remove();
    }
  }
}
