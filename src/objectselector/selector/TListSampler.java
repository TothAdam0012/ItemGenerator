package objectselector.selector;

import objectselector.Util;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TListSampler<T> {
    private final ArrayList<Node> items;

    public TListSampler() {
        this.items = new ArrayList<>();
    }

    public TListSampler(int initialCapacity) {
        this.items = new ArrayList<>(initialCapacity);
    }

    public void add(T item, double weight) {
        items.add(new Node(item, weight));
    }

    public int size() {
        return items.size();
    }

    public List<Node> getItems() {
        return Collections.unmodifiableList(items);
    }

    public @NotNull List<T> sample() {
        List<T> result = new ArrayList<>();

        for(Node node : items) {
            if(Util.roll(node.weight)) {
                result.add(node.item);
            }
        }

        return result;
    }

    public class Node {
        public final T item;
        public final double weight;

        private Node(T item, double weight) {
            this.item = item;
            this.weight = weight;
        }
    }
}
