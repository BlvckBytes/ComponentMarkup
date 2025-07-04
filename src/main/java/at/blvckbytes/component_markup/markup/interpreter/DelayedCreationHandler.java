package at.blvckbytes.component_markup.markup.interpreter;

public interface DelayedCreationHandler {

  DelayedCreationHandler NONE_SENTINEL = created -> {};
  DelayedCreationHandler IMMEDIATE_SENTINEL = created -> {};

  void handle(Object created);

}
