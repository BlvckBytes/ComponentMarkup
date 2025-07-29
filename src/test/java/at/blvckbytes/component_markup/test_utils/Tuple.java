package at.blvckbytes.component_markup.test_utils;

public class Tuple<FirstType, Secondtype> {

  public final FirstType first;
  public final Secondtype second;

  public Tuple(FirstType first, Secondtype second) {
    this.first = first;
    this.second = second;
  }
}
