package objectselector.amountprovider;

import objectselector.Util;

public class StaticAmountProvider extends AmountProvider {
    protected final int amount;

    public StaticAmountProvider(int amount, double amountModifier) {
        super(amountModifier);
        this.amount = amount;
    }

    @Override
    public int getAmount() {
        return amount;
    }

    @Override
    public int getAmount(double amountBonus) {
        return amountModifier == 0 ? amount : random.nextInt(amount, amount + Util.minecraftRoundUp(amountModifier * amountBonus) + 1);
    }
}