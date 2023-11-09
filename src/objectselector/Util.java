package objectselector;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.SplittableRandom;

public class Util {
    private static final SplittableRandom r = new SplittableRandom();

    /**
     *
     * @param pct the chance of statement being true
     * @return true 'pct' percent of the time
     */
    public static boolean roll(double pct) {
        if(pct <= 0.0D) return false;
        if(pct >= 100.0D) return true;
        return r.nextDouble(100.0D) < pct;
    }

    /**
     * Rounds up the given value randomly based on the magnitude of its decimal. In other words the value has decimal/1.0 chance to be round up.
     *
     * @param amount value to be rounded
     * @return rounded value
     */
    public static int minecraftRoundUp(double amount) {
        int whole = (int) amount;
        double decimal = amount - whole;
        return r.nextDouble() < decimal ? whole + 1 : whole;
    }

    /**
     * Generates a random int from 0 to rounded up upperBound.
     * If upperBound is a whole number, then returns a random number [0, upperBound] uniformly.
     * If upperBound is a fraction, then the chance of returning any number [0, (int) upperBound) is equal -> X.
     * However, there is also a decimal/X chance of returning the rounded up upperBound.
     * @param upperBound the maximum amount (rounded up) to be returned
     * @return random integer between 0 (inc) and upper bound rounded up (inc)
     */
    public static int minecraftRandomAmount(double upperBound) {
        if(upperBound < 1.0D) return minecraftRoundUp(upperBound);
        int ceiling = (int) upperBound + 1;
        return minecraftRoundUp((upperBound - (int) upperBound) / (upperBound + 1)) == 1 ? ceiling : r.nextInt(ceiling);
    }

    public static <T> @Nullable T removeLast(ArrayList<T> list) {
        if(list.size() == 0) return null;
        return list.remove(list.size() - 1);
    }
}
