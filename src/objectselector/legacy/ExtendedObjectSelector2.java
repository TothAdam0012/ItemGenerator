package objectselector.legacy;

import objectselector.Util;
import objectselector.objectprovider.ExtendedObjectProvider;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Consumer;

// This class was supposed to replace the ExtendedObjectSelector but wasn't quicker.
// In this class instead of calculating every node's effective weight and branchweight at runtime we have a separate tree for nodes
// which have chancebonus. Before sampling the method decides which tree to sample from based on their weightsums and
// based on the current droprate (which decides the chance at which the bonus tree is picked).

public class ExtendedObjectSelector2<T> {
    private final SplittableRandom r = new SplittableRandom();
    private final ArrayList<Node> modifiedNodes;
    public final ArrayList<Node> items;
    public final ArrayList<Node> bonusItems;
    private int size = 0;
    public final Node head;
    public final Node bonusHead;
    public double weightSum;
    public double bonusWeightSum;


    public ExtendedObjectSelector2() {
        this.items = new ArrayList<>();
        this.bonusItems = new ArrayList<>();
        this.modifiedNodes = new ArrayList<>();
        this.head = new Node();
        this.bonusHead = new Node();
    }

    public ExtendedObjectSelector2(int initialCapacity) {
        this.items = new ArrayList<>(initialCapacity);
        this.bonusItems = new ArrayList<>(initialCapacity);
        this.modifiedNodes = new ArrayList<>(initialCapacity);
        this.head = new Node();
        this.bonusHead = new Node();
    }

    public void add(ExtendedObjectProvider<T> provider, double weight, double bonusModifier) {
        Node normalNode = new Node(provider, weight);
        items.add(normalNode);
        if(bonusModifier > 0.0D) {
            Node bonusNode = new Node(provider, bonusModifier*weight);
            bonusItems.add(bonusNode);

            normalNode.bonusRef = bonusNode;
            bonusNode.bonusRef = normalNode;
        }
    }

    public void build() {
        if(items.isEmpty()) throw new IllegalArgumentException("Collection size must be 1 or more.");
        Collections.sort(items);
        Collections.sort(bonusItems);
        this.head.leftChild = buildNode(2, items); // recursively builds the nodes
        this.head.rightChild = buildNode(3, items);

        this.bonusHead.leftChild = buildNode(2, bonusItems);
        this.bonusHead.rightChild = buildNode(3, bonusItems);

        this.head.branchWeight = this.head.leftChild.branchWeight;
        this.head.leftChild.parent = this.head;

        if(this.bonusHead.leftChild == null) {
            this.bonusHead.branchWeight = 0.0D;
        } else {
            this.bonusHead.branchWeight = this.bonusHead.leftChild.branchWeight;
            this.bonusHead.leftChild.parent = this.bonusHead;
        }

        if(this.head.rightChild != null) {
            this.head.rightChild.parent = this.head;
            this.head.branchWeight += this.head.rightChild.branchWeight;
        }

        if(this.bonusHead.rightChild != null) {
            this.bonusHead.rightChild.parent = this.bonusHead;
            this.bonusHead.branchWeight += this.bonusHead.rightChild.branchWeight;
        }

        this.weightSum = this.head.branchWeight;
        this.bonusWeightSum = this.bonusHead.branchWeight;
        this.size = items.size();
    }

    public int size() {
        return size;
    }

    public List<Node> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void weightedSample(Consumer<T> consumer) {
        weightedSample(consumer, 0, 1);
    }

    public void weightedSample(Consumer<T> consumer, double amountBonus, double dropRate) {
        if(size == 0) throw new IllegalStateException("Selector must be built!");

        double chanceBonus = dropRate - 1;
        weightedNode(r.nextDouble(head.branchWeight + bonusHead.branchWeight*chanceBonus) < head.branchWeight ? head : bonusHead,false).provider.get(consumer, amountBonus, dropRate);
    }

    public void weightedSample(Consumer<T> consumer, int k) {
        weightedSample(consumer, k, 0, 1);
    }

    public void weightedSample(Consumer<T> consumer, int k, double amountBonus, double dropRate) {
        if(size == 0) throw new IllegalStateException("Selector must be built!");
        if(k < 1) throw new IllegalArgumentException("K must be 1 or higher!");
        if(k > size) throw new IllegalArgumentException("K can't be higher than amount of elements in generator; amount: " + size + ", requested amount: " + k);
        if(k == 1) weightedSample(consumer);

        double chanceBonus = dropRate - 1;
        for(int i = 0; i < k-1; i++) {
            weightedNode(r.nextDouble(head.branchWeight + bonusHead.branchWeight*chanceBonus) < head.branchWeight ? head : bonusHead, true).provider.get(consumer, amountBonus, dropRate);
        }
        weightedNode(r.nextDouble(head.branchWeight + bonusHead.branchWeight*chanceBonus) < head.branchWeight ? head : bonusHead,false).provider.get(consumer, amountBonus, dropRate);
        resetState();
    }

    //TODO: ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public List<T> weightedSample() {
        return weightedSample(0, 1);
    }

    public List<T> weightedSample(double amountBonus, double dropRate) {
        List<T> itemList = new ArrayList<>();
        if(size == 0) throw new IllegalStateException("Selector must be built!");

        double chanceBonus = dropRate - 1;
        weightedNode(r.nextDouble(head.branchWeight + bonusHead.branchWeight*chanceBonus) < head.branchWeight ? head : bonusHead,false).provider.get(itemList, amountBonus, dropRate);
        return itemList;
    }

    public List<T> weightedSample(int k) {
        return weightedSample(k, 0, 1);
    }

    public List<T> weightedSample(int k, double amountBonus, double dropRate) {
        if(size == 0) throw new IllegalStateException("Selector must be built!");
        if(k < 1) throw new IllegalArgumentException("K must be 1 or higher!");
        if(k > size) throw new IllegalArgumentException("K can't be higher than amount of elements in generator; amount: " + size + ", requested amount: " + k);
        if(k == 1) return weightedSample();

        double chanceBonus = dropRate - 1;
        List<T> itemList = new ArrayList<>();
        for(int i = 0; i < k-1; i++) {
            weightedNode(r.nextDouble(head.branchWeight + bonusHead.branchWeight*chanceBonus) < head.branchWeight ? head : bonusHead, true).provider.get(itemList, amountBonus, dropRate);
        }
        weightedNode(r.nextDouble(head.branchWeight + bonusHead.branchWeight*chanceBonus) < head.branchWeight ? head : bonusHead,false).provider.get(itemList, amountBonus, dropRate);
        resetState();
        return itemList;
    }

    //TODO: ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // magic
    private Node weightedNode(Node headNode, boolean remove) {
        double number = r.nextDouble(headNode.branchWeight);
        Node currentNode = headNode;
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
        //weightSum -= missingWeight;
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

        currentNode.branchWeight -= missingWeight;
        if(node.bonusRef != null && !node.bonusRef.taken) {
            zeroNode(node.bonusRef);
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

        head.branchWeight = weightSum;
        bonusHead.branchWeight = bonusWeightSum;
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
        private Node bonusRef;
        public final ExtendedObjectProvider<T> provider;     // the value of this node
        private double weight;
        public final double defaultWeight;
        private double branchWeight; // the weightsum of the children and children's children nodes (weight of the whole branch til the leafnodes)
        private double defaultBranchWeight; // the weightsum before generating items (sampling without replacement temporarily can change the weightsum on the branch)
        private boolean taken = false;  // if the node is selected while sampling K items without replacement then it s put in a list
        // and its taken status indicates that the 'effective weight' is 0 and/or shouldn't be put in the list again
        // (the list is used to restore the nodes' initial values after the sampling is done)

        Node(@NotNull ExtendedObjectProvider<T> provider, double weight) {
            this.provider = provider;
            this.defaultWeight = this.weight = weight;
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
                    ", branchWeight: " + branchWeight +
                    ", defaultBranchWeight: " + defaultBranchWeight + "]";
        }
    }
}
