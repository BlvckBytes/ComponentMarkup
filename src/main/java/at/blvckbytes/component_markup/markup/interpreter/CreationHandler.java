package at.blvckbytes.component_markup.markup.interpreter;

@FunctionalInterface
public interface CreationHandler<B, C> {

  void handle(ExtendedBuilder<B, C> builder);

}
