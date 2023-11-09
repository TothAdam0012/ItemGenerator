import objectselector.objectprovider.ExtendedStaticObjectProvider;
import objectselector.selector.ExtendedObjectSelector;
import objectselector.legacy.ExtendedObjectSelector2;



public class Main {
    public static void main(String[] args) {
        final int SAMPLE_AMOUNT = 10_000_000;


        ExtendedObjectSelector<String> selector = new ExtendedObjectSelector<>(7);

        selector.add(new ExtendedStaticObjectProvider<>("Apple"), 1, 0);
        selector.add(new ExtendedStaticObjectProvider<>("Blueberry"), 1, 0);
        selector.add(new ExtendedStaticObjectProvider<>("Kolbi"), 1, 0);
        selector.add(new ExtendedStaticObjectProvider<>("Raspberry"), 1, 0);
        selector.add(new ExtendedStaticObjectProvider<>("Pear"), 20, 0);
        selector.add(new ExtendedStaticObjectProvider<>("Mushroom"), 1, 0);
        selector.build();


        ExtendedObjectSelector2<String> selector2 = new ExtendedObjectSelector2<>(6);

        selector2.add(new ExtendedStaticObjectProvider<>("Apple"), 1, 0);
        selector2.add(new ExtendedStaticObjectProvider<>("Blueberry"), 1, 0);
        selector2.add(new ExtendedStaticObjectProvider<>("Kolbi"), 1, 0);
        selector2.add(new ExtendedStaticObjectProvider<>("Raspberry"), 1, 0);
        selector2.add(new ExtendedStaticObjectProvider<>("Pear"), 20, 0);
        selector2.add(new ExtendedStaticObjectProvider<>("Mushroom"), 1, 0);
        selector2.build();

        printMillis("selector: ", () -> {
            for(int i = 0; i < SAMPLE_AMOUNT; i++) {
                selector.weightedSample((res) -> {

                }, 0, 2);
            }
        });

        printMillis("selector 2: ", () -> {
            for(int i = 0; i < SAMPLE_AMOUNT; i++) {
                selector2.weightedSample((res) -> {

                }, 0, 2);
            }
        });

    }

    public static void printNanos(String text, Runnable runnable) {
        long startNanos = System.nanoTime();
        runnable.run();
        System.out.println(text + (System.nanoTime() - startNanos));
    }

    public static void printMillis(String text, Runnable runnable) {
        long startMillis = System.currentTimeMillis();
        runnable.run();
        System.out.println(text + (System.currentTimeMillis() - startMillis));
    }

    private static class Counter {
        private int count = 0;

        public void increase() {
            count++;
        }

        public void decrease() {
            count--;
        }

        public int getValue() {
            return count;
        }

        public String toString() {
            return String.valueOf(count);
        }
    }
}