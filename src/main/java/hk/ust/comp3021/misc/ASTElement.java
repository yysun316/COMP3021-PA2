package hk.ust.comp3021.misc;

import hk.ust.comp3021.query.QueryOnNode;
import hk.ust.comp3021.utils.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ASTElement {
    private int lineno;
    private int colOffset;
    private int endLineno;
    private int endColOffset;

    public ASTElement() {
        this.lineno = -1;
        this.colOffset = -1;
        this.endLineno = -1;
        this.endColOffset = -1;
    }

    public ASTElement(int lineno, int colOffset, int endLineno, int endColOffset) {
        this.lineno = lineno;
        this.colOffset = colOffset;
        this.endLineno = endLineno;
        this.endColOffset = endColOffset;
    }

    public ASTElement(XMLNode node) {
        if (node.hasAttribute("lineno")) {
            this.lineno = Integer.parseInt(node.getAttribute("lineno"));
        }
        if (node.hasAttribute("col_offset")) {
            this.colOffset = Integer.parseInt(node.getAttribute("col_offset"));
        }
        if (node.hasAttribute("end_lineno")) {
            this.endLineno = Integer.parseInt(node.getAttribute("end_lineno"));
        }
        if (node.hasAttribute("end_col_offset")) {
            this.endColOffset = Integer.parseInt(node.getAttribute("end_col_offset"));
        }
    }

    public int getLineNo() {
        return this.lineno;
    }

    public int getColOffset() {
        return this.colOffset;
    }

    public int getEndLineNo() {
        return this.endLineno;
    }

    public int getEndColOffset() {
        return this.endColOffset;
    }

    public abstract String getNodeType();

    /*
     * Return direct children of current node, which are fields whose type is `ASTElement`.
     * Noticed that field whose class type is `ASTEnumOp` should not be regarded as children.
     */
    public abstract ArrayList<ASTElement> getChildren();

    /**
     * TODO `filter` mimic {@link java.util.stream.Stream#filter(Predicate)} but operates on AST tree structure instead of List
     * TODO please design the function by yourself to pass complication and the provided test cases
     *
     * @param predicate representing a boolean-valued function that takes ASTElement as input parameter and returns a bool result
     * @return an ArrayList of ASTElement where predicate returns true
     * <p>
     * Hints: traverse the tree and put those satisfy predicates into array list
     */
    public ArrayList<ASTElement> filter(Predicate<ASTElement> predicate) {
        ArrayList<ASTElement> result = new ArrayList<>();
        if (predicate.test(this)) {
            result.add(this);
        }
        for (ASTElement child : this.getChildren()) { // recursive call
            if (child != null)
                result.addAll(child.filter(predicate));
        }
        return result;
    }

    /**
     * TODO `forEach` mimic {@link Iterable#forEach(Consumer)} but operates on AST tree structure instead of List
     * TODO please design the function by yourself to pass complication and the provided test cases
     *
     * @param action representing an operation that accepts ASTElement as input and performs some action
     *               on it without returning any result.
     * @return null
     * <p>
     * Hints: traverse the tree and perform the action on every node in the tree
     */
    public void forEach(Consumer<ASTElement> action) {
        action.accept(this);
        for (ASTElement child : this.getChildren()) {
            if (child != null)
                child.forEach(action);
        }
    }

    /**
     * TODO `groupingBy` mimic {@link java.util.stream.Collectors#groupingBy(Function, Collector)} )} but operates on AST tree structure instead of List
     * TODO please design the function by yourself to pass complication and the provided test cases
     *
     * @param classifier representing a function that classifies an ASTElement argument and produces the classification result with generic type
     * @param collector  representing a collector used to accumulate the ASTElement object into results
     * @return a map whose key and value are all generic types
     * <p>
     * Hints: traverse the tree and group them if they belong to the same categories
     * Hints: please refer to the usage of {@link java.util.stream.Collectors#groupingBy(Function, Collector)}} to learn more about this method
     */

    public <K, V, A> Map<K, V> groupingBy(Function<ASTElement, K> classifier, Collector<ASTElement, A, V> collector) {
        // collector: supplier, accumulator, combiner, finisher
        // K: key, A: container, V: result

        // Intermediate container.
        Map<K, A> map = new HashMap<>();
        // accept ASTElement object and accumulate it into results
        forEach(element -> {
            // 1. apply classifier to element to get key
            K key = classifier.apply(element);
            // 2. get container from map by key, if not exist, create a new key-value pair
            map.computeIfAbsent(key, k -> collector.supplier().get()); // putIfAbsent
            // 3. accumulate ASTElement object into results
            collector.accumulator().accept(map.get(key), element);
        });

        return new HashMap<>(map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> collector.finisher().apply(entry.getValue()))));
    }
}
