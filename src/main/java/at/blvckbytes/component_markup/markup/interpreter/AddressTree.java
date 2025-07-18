package at.blvckbytes.component_markup.markup.interpreter;

import at.blvckbytes.component_markup.util.IntList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

public class AddressTree {

  private final IntList indices;
  private final List<EnumMap<MembersSlot, AddressTree>> values;

  public AddressTree(int initialCapacity) {
    this.indices = new IntList(initialCapacity);
    this.values = new ArrayList<>(initialCapacity);
  }

  public AddressTree() {
    this.indices = new IntList();
    this.values = new ArrayList<>();
  }

  public void put(int index, @Nullable EnumMap<MembersSlot, AddressTree> value) {
    this.indices.add(index);
    this.values.add(value);
  }

  public @Nullable EnumMap<MembersSlot, AddressTree> getValue(int index) {
    return this.values.get(index);
  }

  public int getIndex(int index) {
    return this.indices.get(index);
  }

  public int getSize() {
    return this.indices.getSize();
  }

  public @Nullable EnumMap<MembersSlot, AddressTree> getForFirstIndex() {
    if (this.indices.getSize() == 0)
      return null;

    if (this.indices.get(0) != 0)
      return null;

    return this.values.get(0);
  }

  public static EnumMap<MembersSlot, AddressTree> emptyValue() {
    return new EnumMap<>(MembersSlot.class);
  }

  public static void put(EnumMap<MembersSlot, AddressTree> map, MembersSlot slot, int index, @Nullable EnumMap<MembersSlot, AddressTree> value) {
    map.computeIfAbsent(slot, key -> new AddressTree()).put(index, value);
  }

  public static AddressTree singleton(EnumMap<MembersSlot, AddressTree> value) {
    AddressTree addressTree = new AddressTree(1);
    addressTree.put(0, value);
    return addressTree;
  }
}