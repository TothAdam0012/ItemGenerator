package objectselector.selector;

import objectselector.Util;
import objectselector.objectprovider.ExtendedObjectProvider;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class ExtendedObjectSelector<T> {
    private final SplittableRandom r = new SplittableRandom();
    private final ArrayList<Node> modifiedNodes;
    private final ArrayList<Node> items;
    private int size = 0;
    private final Node head;
    public double weightSum;
    public double weightSumExtra;


    public ExtendedObjectSelector() {
        this.items = new ArrayList<>();
        this.modifiedNodes = new ArrayList<>();
        this.head = new Node();
    }

    public ExtendedObjectSelector(int initialCapacity) {
        this.items = new ArrayList<>(initialCapacity);
        this.modifiedNodes = new ArrayList<>(initialCapacity);
        this.head = new Node();
    }

    public void add(ExtendedObjectProvider<T> provider, double weight, double bonusModifier) {
        items.add(new Node(provider, weight, bonusModifier));
    }

    public void build() {
        if(items.isEmpty()) throw new IllegalArgumentException("Collection size must be 1 or more.");
        Collections.sort(items);
        this.head.leftChild = buildNode(2, items); // recursively builds the nodes
        this.head.rightChild = buildNode(3, items);

        this.head.branchWeight = this.head.leftChild.branchWeight;
        this.head.branchWeightExtra = this.head.leftChild.branchWeightExtra;

        this.head.leftChild.parent = this.head;
        if(this.head.rightChild != null) {
            this.head.rightChild.parent = this.head;
            this.head.branchWeight += this.head.rightChild.branchWeight;
            this.head.branchWeightExtra = this.head.rightChild.branchWeightExtra;
        }

        this.weightSum = this.head.branchWeight;
        this.weightSumExtra = this.head.branchWeightExtra;

        this.size = items.size();
    }

    public int size() {
        return size;
    }

    public List<Node> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void weightedSample(Consumer<T> consumer) {
        weightedSample(consumer,0, 1);
    }

    public void weightedSample(Consumer<T> consumer, double amountBonus, double dropRate) {
        if(dropRate < 1.0D) throw new UnsupportedOperationException("Weight decrease is not implemented yet. Drop rate must be 1 or higher!");
        if(size == 0) throw new IllegalStateException("Selector must be built!");

        double chanceBonus = dropRate - 1;
        weightedNode(false, chanceBonus).provider.get(consumer, amountBonus, dropRate);
    }

    public void weightedSample(Consumer<T> consumer, int k) {
        weightedSample(consumer, k, 0, 1);
    }

    public void weightedSample(Consumer<T> consumer, int k, double amountBonus, double dropRate) {
        if(dropRate < 1.0D) throw new UnsupportedOperationException("Weight decrease is not implemented yet. Drop rate must be 1 or higher!");
        if(size == 0) throw new IllegalStateException("Selector must be built!");
        if(k < 1) throw new IllegalArgumentException("K must be 1 or higher!");
        if(k > size) throw new IllegalArgumentException("K can't be higher than amount of elements in generator; amount: " + size + ", requested amount: " + k);
        if(k == 1) weightedSample(consumer);

        double chanceBonus = dropRate - 1;
        double temp = weightSum;
        double tempExtra = weightSumExtra;
        for(int i = 0; i < k-1; i++) {
            weightedNode(true, chanceBonus).provider.get(consumer, amountBonus, dropRate);
        }
        weightedNode(false, chanceBonus).provider.get(consumer, amountBonus, dropRate);
        resetState();
        weightSum = temp;
        weightSumExtra = tempExtra;
    }

    //TODO: ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public List<T> weightedSample() {
        return weightedSample(0, 1);
    }

    public List<T> weightedSample(double amountBonus, double dropRate) {
        if(dropRate < 1.0D) throw new UnsupportedOperationException("Weight decrease is not implemented yet. Drop rate must be 1 or higher!");
        if(size == 0) throw new IllegalStateException("Selector must be built!");

        double chanceBonus = dropRate - 1;
        List<T> itemList = new ArrayList<>();
        weightedNode(false, chanceBonus).provider.get(itemList, amountBonus, dropRate);
        return itemList;
    }

    public List<T> weightedSample(int k) {
        return weightedSample(k, 0, 1);
    }

    public List<T> weightedSample(int k, double amountBonus, double dropRate) {
        if(dropRate < 1.0D) throw new UnsupportedOperationException("Weight decrease is not implemented yet. Drop rate must be 1 or higher!");
        if(size == 0) throw new IllegalStateException("Selector must be built!");
        if(k < 1) throw new IllegalArgumentException("K must be 1 or higher!");
        if(k > size) throw new IllegalArgumentException("K can't be higher than amount of elements in generator; amount: " + size + ", requested amount: " + k);
        if(k == 1) return weightedSample();

        double chanceBonus = dropRate - 1;
        double temp = weightSum;
        double tempExtra = weightSumExtra;
        List<T> itemList = new ArrayList<>();
        for(int i = 0; i < k-1; i++) {
            weightedNode(true, chanceBonus).provider.get(itemList, amountBonus, dropRate);
        }
        weightedNode(false, chanceBonus).provider.get(itemList, amountBonus, dropRate);
        resetState();
        weightSum = temp;
        weightSumExtra = tempExtra;
        return itemList;
    }

    //TODO: ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // magic
    private Node weightedNode(boolean remove, double chanceBonus) {
        double number = r.nextDouble(weightSum + chanceBonus*weightSumExtra);
        Node currentNode = head;
        double temp;
        do {
            if(currentNode.leftChild != null) {
                temp = number - (currentNode.leftChild.branchWeight + chanceBonus*currentNode.leftChild.branchWeightExtra);

                if(temp <= 0.0D) {
                    currentNode = currentNode.leftChild;
                } else {
                    currentNode = currentNode.rightChild;
                    number = temp;
                }
            } else {
                if(currentNode.rightChild != null) {
                    currentNode = currentNode.rightChild;
                } else {
                    // if both children nodes are null and number is still not <= 0.0 (due to precision errors)
                    if(remove) zeroNode(currentNode);
                    return currentNode;
                }
            }

            number -= currentNode.weight + chanceBonus*currentNode.weightExtra;

        } while(number > 0.0D);
        if(remove) zeroNode(currentNode);
        return currentNode;
    }

    private void zeroNode(Node node) {
        double missingWeight = node.weight;
        double missingWeightExtra = node.weightExtra;
        weightSum -= missingWeight;
        weightSumExtra -= missingWeightExtra;
        node.weight = 0.0D;
        node.weightExtra = 0.0D;
        Node currentNode = node;
        while(currentNode.parent != null) {
            currentNode.branchWeight -= missingWeight;
            currentNode.branchWeightExtra -= missingWeightExtra;
            if(!currentNode.taken) {
                modifiedNodes.add(currentNode);
                currentNode.taken = true;
            }
            currentNode = currentNode.parent;
        }
    }

    private void resetState() {
        Node currentNode = Util.removeLast(modifiedNodes);
        while(currentNode != null) {
            currentNode.branchWeight = currentNode.defaultBranchWeight;
            currentNode.branchWeightExtra = currentNode.defaultBranchWeightExtra;
            currentNode.weight = currentNode.defaultWeight;
            currentNode.weightExtra = currentNode.defaultWeightExtra;
            currentNode.taken = false;
            currentNode = Util.removeLast(modifiedNodes);
        }
    }

    // recursively builds the nodes and builds the childnodes before attaching them
    private Node buildNode(int currentIndex, List<Node> items) {
        if(currentIndex > items.size() + 1) return null; // index can be equal to length because we fetch index-1

        Node currentNode = items.get(currentIndex - 2); // counting starts from 1 but array indexes start from 0
        Node leftChild = buildNode(currentIndex * 2, items);
        Node rightChild = buildNode(currentIndex * 2 + 1, items);

        currentNode.leftChild = leftChild;
        currentNode.rightChild = rightChild;
        currentNode.branchWeightExtra = currentNode.weightExtra;
        if(leftChild != null) {
            currentNode.branchWeight += leftChild.branchWeight;
            currentNode.branchWeightExtra += leftChild.branchWeightExtra;
            leftChild.parent = currentNode;
        }
        if(rightChild != null) {
            currentNode.branchWeight += rightChild.branchWeight;
            currentNode.branchWeightExtra += rightChild.branchWeightExtra;
            rightChild.parent = currentNode;
        }

        currentNode.defaultBranchWeight = currentNode.branchWeight;
        currentNode.defaultBranchWeightExtra = currentNode.branchWeightExtra;

        return currentNode;
    }

    public class Node implements Comparable<Node> {
        private Node parent;
        private Node leftChild;
        private Node rightChild;
        public final ExtendedObjectProvider<T> provider;     // the value of this node
        private double weight;
        public final double defaultWeight;
        private double weightExtra;
        private double defaultWeightExtra;
        private double branchWeight; // the weightsum of the children and children's children nodes (weight of the whole branch til the leafnodes)
        private double defaultBranchWeight; // the weightsum before generating items (sampling without replacement temporarily can change the weightsum on the branch)
        private double branchWeightExtra;
        private double defaultBranchWeightExtra;
        private boolean taken = false;  // if the node is selected while sampling K items without replacement then it s put in a list
        // and its taken status indicates that the 'effective weight' is 0 and/or shouldn't be put in the list again
        // (the list is used to restore the nodes' initial values after the sampling is done)

        Node(@NotNull ExtendedObjectProvider<T> provider, double weight, double bonusModifier) {
            this.provider = provider;
            this.defaultWeight = this.weight = weight;
            this.defaultWeightExtra = this.weightExtra = this.weight * bonusModifier;
            this.defaultBranchWeight = this.branchWeight = this.weight;
        }

        Node() { // default conctructor for creating the headnode
            this.provider = null;
            this.defaultWeight = 0.0D;
        }

        @Override
        public int compareTo(@NotNull Node other) {
            return Double.compare(other.weight, this.weight);
        }

        @Override
        public String toString() {
            return "[weight: " + weight +
                    ", defaultWeight: " + defaultWeight +
                    ", weightExtra: " + weightExtra +
                    ", defaultWeightExtra: " + defaultWeightExtra +
                    ", branchWeight: " + branchWeight +
                    ", defaultBranchWeight: " + defaultBranchWeight +
                    ", branchWeightExtra: " + branchWeightExtra +
                    ", defaultBranchWeightExtra: " + defaultBranchWeightExtra + "]";
        }
    }
}
