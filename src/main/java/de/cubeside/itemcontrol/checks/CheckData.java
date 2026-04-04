package de.cubeside.itemcontrol.checks;

public class CheckData {
    private int itemStackCount;

    public int increaseItemStackCount() {
        itemStackCount++;
        return itemStackCount;
    }
}
