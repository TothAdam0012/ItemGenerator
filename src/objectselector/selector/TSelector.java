package objectselector.selector;

import objectselector.Util;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TSelector<T> {
    private final SplittableRandom r = new SplittableRandom();
    private final ArrayList<Node> modifiedNodes;
    private final ArrayList<Node> items;
    private int size = 0;
    private final Node head;
    private double weightSum;

    public TSelector() {
        this.items = new ArrayList<>();
        this.modifiedNodes = new ArrayList<>();
        this.head = new Node();
    }

    public TSelector(int initialCapacity) {
        this.items = new ArrayList<>();
        this.modifiedNodes = new ArrayList<>();
        this.head = new Node();
    }

    public void add(T item, double weight) {
        items.add(new Node(item, weight));
    }

    public void build() {
        if(items.isEmpty()) throw new IllegalArgumentException("Collection size must be 1 or more.");
        Collections.sort(items);
        this.head.leftChild = buildNode(2, items); // recursively builds the nodes
        this.head.rightChild = buildNode(3, items);

        this.weightSum = this.head.leftChild.branchWeight;

        this.head.leftChild.parent = this.head;
        if(this.head.rightChild != null) {
            this.head.rightChild.parent = this.head;
            this.weightSum += this.head.rightChild.branchWeight;
        }

        this.size = items.size();
    }

    public int size() {
        return size;
    }

    public List<Node> getItems() {
        return Collections.unmodifiableList(items);
    }

    public T weightedSample() {
        if(size == 0) throw new IllegalStateException("Selector must be built!");
        return weightedNode(false).item;
    }

    public List<T> weightedSample(int k) {
        if(size == 0) throw new IllegalStateException("Selector must be built!");
        if(k < 1) throw new IllegalArgumentException("K must be 1 or higher!");
        if(k > size) throw new IllegalArgumentException("K can't be higher than amount of elements in generator; amount: " + size + ", requested amount: " + k);
        if(k == 1) return Collections.singletonList(weightedSample());

        double temp = weightSum;
        List<T> result = new ArrayList<>();
        for(int i = 0; i < k-1; i++) {
            result.add(weightedNode(true).item);
        }
        result.add(weightedNode(false).item);
        resetState();
        weightSum = temp;
        return result;
    }

    // magic
    private Node weightedNode(boolean remove) {
        double number = r.nextDouble(weightSum);
        Node currentNode = head;
        double temp;
        do {
            if(currentNode.leftChild != null) {
                temp = number - currentNode.leftChild.branchWeight;

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

            number -= currentNode.weight;

        } while(number > 0.0D);
        if(remove) zeroNode(currentNode);
        return currentNode;
    }

    private void zeroNode(Node node) {
        double missingWeight = node.weight;
        weightSum -= missingWeight;
        node.weight = 0.0D;
        Node currentNode = node;
        while(currentNode.parent != null) {
            currentNode.branchWeight -= missingWeight;
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
            currentNode.weight = currentNode.defaultWeight;
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
        if(leftChild != null) {
            currentNode.branchWeight += leftChild.branchWeight;
            leftChild.parent = currentNode;
        }
        if(rightChild != null) {
            currentNode.branchWeight += rightChild.branchWeight;
            rightChild.parent = currentNode;
        }

        currentNode.defaultBranchWeight = currentNode.branchWeight;

        return currentNode;
    }

    public class Node implements Comparable<Node> {
        private Node parent;
        private Node leftChild;
        private Node rightChild;
        public final T item;     // the value of this node
        private double weight;
        public final double defaultWeight;
        private double branchWeight; // the weightsum of the children and children's children nodes (weight of the whole branch til the leafnodes)
        private double defaultBranchWeight; // the weightsum before generating items (sampling without replacement temporarily can change the weightsum on the branch)
        private boolean taken = false;  // if the node is selected while sampling K items without replacement then it s put in a list
        // and its taken status indicates that the 'effective weight' is 0 and/or shouldn't be put in the list again
        // (the list is used to restore the nodes' initial values after the sampling is done)

        Node(@NotNull T item, double weight) {
            this.item = item;
            this.defaultWeight = this.weight = weight;
            this.defaultBranchWeight = this.branchWeight = this.weight;
        }

        Node() { // default conctructor for creating the headnode
            this.item = null;
            this.defaultWeight = 0.0D;
        }

        @Override
        public int compareTo(@NotNull Node other) {
            return Double.compare(other.weight, this.weight);
        }
    }
}
