package objectselector.objectprovider;

import java.util.List;
import java.util.function.Consumer;

public abstract class ExtendedObjectProvider<T> {

    /**
     * Puts the item(s) in the given list.
     * Some holders can return multiple items.
     * @param objectList The list which the object(s) will be added to.
     */
    public abstract void get(List<T> objectList);

    public abstract void get(List<T> objectList, double amountBonus, double chanceBonus);

    public abstract void get(Consumer<T> consumer);

    public abstract void get(Consumer<T> consumer, double amountBonus, double chanceBonus);
}