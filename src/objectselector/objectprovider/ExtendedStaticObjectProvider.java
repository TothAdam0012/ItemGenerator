package objectselector.objectprovider;

import java.util.List;
import java.util.function.Consumer;

public class ExtendedStaticObjectProvider<T> extends ExtendedObjectProvider<T> {
    private final T item;

    public ExtendedStaticObjectProvider(T item) {
        this.item = item;
    }

    @Override
    public void get(List<T> objectList) {
        objectList.add(item);
    }

    @Override
    public void get(List<T> objectList, double amountBonus, double chanceBonus) {
        get(objectList);
    }

    @Override
    public void get(Consumer<T> consumer) {
        consumer.accept(item);
    }

    @Override
    public void get(Consumer<T> consumer, double amountBonus, double chanceBonus) {
        get(consumer);
    }
}
