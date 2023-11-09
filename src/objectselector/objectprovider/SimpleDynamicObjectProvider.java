package objectselector.objectprovider;

import objectselector.selector.SimpleObjectSelector;

public class SimpleDynamicObjectProvider<T> extends SimpleObjectProvider<T> {
    private final SimpleObjectSelector<T> innerSelector;

    public SimpleDynamicObjectProvider() {
        this.innerSelector = new SimpleObjectSelector<>();
    }

    public SimpleDynamicObjectProvider(int initialCapacity) {
        this.innerSelector = new SimpleObjectSelector<>(initialCapacity);
    }

    public void add(SimpleObjectProvider<T> provider, double weight) {
        innerSelector.add(provider, weight);
    }

    public void build() {
        innerSelector.build();
    }

    @Override
    public T get() {
        return innerSelector.weightedSample();
    }
}
