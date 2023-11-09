package objectselector.amountprovider;

import objectselector.Util;

public class UniformArrayAmountProvider extends AmountProvider {

    protected final int[] amounts;

    public UniformArrayAmountProvider(int[] amounts, double amountModifier) {
        super(amountModifier);
        this.amounts = amounts;
    }

    @Override
    public int getAmount() {
        return amounts[random.nextInt(amounts.length)];
    }

    @Override
    public int getAmount(double amountBonus) {
        int chosenAmount = amounts[random.nextInt(amounts.length)];
        return amountModifier == 0 ? chosenAmount : random.nextInt(chosenAmount, chosenAmount + Util.minecraftRoundUp(amountModifier * amountBonus) + 1);
    }
}
