package at.blvckbytes.component_markup.markup.interpreter;

@FunctionalInterface
public interface CreationHandler<B> {

  void handle(ExtendedBuilder<B> builder);

}
