package objectselector.objectprovider;

public class SimpleStaticObjectProvider<T> extends SimpleObjectProvider<T> {
    private final T item;

    public SimpleStaticObjectProvider(T item) {
        this.item = item;
    }

    @Override
    public T get() {
        return item;
    }
}
