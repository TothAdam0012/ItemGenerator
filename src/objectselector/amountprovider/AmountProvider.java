package objectselector.amountprovider;

import java.util.SplittableRandom;

public abstract class AmountProvider {
    protected static final SplittableRandom random = new SplittableRandom();
    protected final double amountModifier;

    protected AmountProvider(double amountModifier) {
        this.amountModifier = amountModifier;
    }

    public abstract int getAmount();

    public abstract int getAmount(double amountBonus);

    public int getAmount(double amountBonus, int max) {
        return Math.min(getAmount(amountBonus), max);
    }
}

