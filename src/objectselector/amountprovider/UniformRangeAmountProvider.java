package objectselector.amountprovider;

import objectselector.Util;

public class UniformRangeAmountProvider extends AmountProvider {

    protected final int min;
    protected final int max;

    public UniformRangeAmountProvider(int min, int max, double amountModifier) {
        super(amountModifier);
        this.min = min;
        this.max = max;
    }

    @Override
    public int getAmount() {
        return random.nextInt(min, max + 1);
    }

    @Override
    public int getAmount(double amountBonus) {
        int chosenAmount = random.nextInt(min, max + 1);
        return amountModifier == 0 ? chosenAmount : random.nextInt(chosenAmount, chosenAmount + Util.minecraftRoundUp(amountModifier * amountBonus) + 1);
    }
}