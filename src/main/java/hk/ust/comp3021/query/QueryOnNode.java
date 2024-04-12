package hk.ust.comp3021.query;

import hk.ust.comp3021.expr.BinOpExpr;
import hk.ust.comp3021.expr.BoolOpExpr;
import hk.ust.comp3021.expr.CompareExpr;
import hk.ust.comp3021.expr.UnaryOpExpr;
import hk.ust.comp3021.misc.*;
import hk.ust.comp3021.stmt.AugAssignStmt;
import hk.ust.comp3021.stmt.FunctionDefStmt;
import hk.ust.comp3021.utils.*;

import javax.sound.sampled.AudioFileFormat;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;


public class QueryOnNode {

    private HashMap<String, ASTModule> id2ASTModules;

    public QueryOnNode(HashMap<String, ASTModule> id2ASTModules) {
        this.id2ASTModules = id2ASTModules;
    }

    /**
     * TODO `findFuncWithArgGtN` find all functions whose # arguments > given `paramN` in all modules {@link QueryOnNode#id2ASTModules}
     *
     * @param paramN the number of arguments user expects
     * @return null as PA1, simply print out all functions that satisfy the requirements with format ModuleID_FuncName_LineNo
     * Hints1: use {@link ASTElement#filter(Predicate)} method to implement the function
     */
    public Consumer<Integer> findFuncWithArgGtN = (paramN) -> {
        id2ASTModules.forEach((moduleID, module) -> {
            module.getChildren().forEach(tree -> {
                tree.filter(el -> el instanceof FunctionDefStmt && ((FunctionDefStmt) el).getParamNum() >= paramN) // filter out FunctionDefStmt with # arguments >= paramN in PA1
                        .forEach(el -> {
                            String output = moduleID + "_" + ((FunctionDefStmt) el).getName() + "_" + el.getLineNo();
                            System.out.println(output);
                        });
            });
        });
    };


    /**
     * TODO `calculateOp2Nums` count the frequency of each operator in all modules {@link QueryOnNode#id2ASTModules}
     *
     * @param null
     * @return op2Num as PA1,the key is operator name, and value is the frequency
     * Hints1: use {@link ASTElement#forEach(Consumer)} method to implement the function
     */
    public Supplier<HashMap<String, Integer>> calculateOp2Nums = () -> {
        HashMap<String, Integer> op2Num = new HashMap<>();
        id2ASTModules.forEach((moduleID, module) -> {
            module.getChildren().forEach(tree -> {
                tree.forEach(el -> {
                    if (el instanceof BinOpExpr) {
                        ASTEnumOp op = ((BinOpExpr) el).getOp();
                        op2Num.put(op.getOperatorName(), op2Num.getOrDefault(op.getOperatorName(), 0) + 1);
                    } else if (el instanceof BoolOpExpr) {
                        ASTEnumOp op = ((BoolOpExpr) el).getOp();
                        op2Num.put(op.getOperatorName(), op2Num.getOrDefault(op.getOperatorName(), 0) + 1);
                    } else if (el instanceof CompareExpr) {
                        ArrayList<ASTEnumOp> op = ((CompareExpr) el).getOps();
                        op.forEach(o -> op2Num.put(o.getOperatorName(), op2Num.getOrDefault(o.getOperatorName(), 0) + 1));
                    } else if (el instanceof UnaryOpExpr) {
                        ASTEnumOp op = ((UnaryOpExpr) el).getOp();
                        op2Num.put(op.getOperatorName(), op2Num.getOrDefault(op.getOperatorName(), 0) + 1);
                    } else if (el instanceof AugAssignStmt) {
                        ASTEnumOp op = ((AugAssignStmt) el).getOp();
                        op2Num.put(op.getOperatorName(), op2Num.getOrDefault(op.getOperatorName(), 0) + 1);
                    }
                });
            });
        });
        return op2Num;
    };
    /**
     * TODO `calculateNode2Nums` count the frequency of each node in all modules {@link QueryOnNode#id2ASTModules}
     *
     * @param astID, a number to represent a specific AST or -1 for all
     * @return node2Nums as PA1,the key is node type, and value is the frequency
     * Hints1: use {@link ASTElement#groupingBy(Function, Collector)} method to implement the function
     * Hints2: if astID is invalid, return empty map
     */
    public Function<String, Map<String, Long>> calculateNode2Nums = (astID) -> {
        HashMap<String, Long> node2Nums = new HashMap<>();
        if ("-1".equals(astID)) {
            id2ASTModules.forEach((moduleID, module) -> {
                module.getChildren().forEach(astElement ->
                        astElement.groupingBy(ASTElement::getNodeType, Collectors.counting()).forEach((key, value) ->
                                node2Nums.put(key, node2Nums.getOrDefault(key, 0L) + value.intValue())
                        )
                );
                node2Nums.put("Module", node2Nums.getOrDefault("Module", 0L) + 1L);
            });
            return node2Nums;
        }
        if (!id2ASTModules.containsKey(astID)) return new HashMap<>();
        id2ASTModules.get(astID).getChildren().forEach(astElement ->
                astElement.groupingBy(ASTElement::getNodeType, Collectors.counting()).forEach((key, value) ->
                        node2Nums.put(key, node2Nums.getOrDefault(key, 0L) + value.intValue())
                )
        );
        node2Nums.put("Module", node2Nums.getOrDefault("Module", 0L) + 1L);
        return node2Nums;

    };


    /**
     * TODO `processNodeFreq` sort all functions in all modules {@link QueryOnNode#id2ASTModules} based on the number of nodes in FunctionDefStmt subtree
     *
     * @param null
     * @return a list of entries sorted in descending order where the key is function name
     * with format ModuleID_FuncName_LineNo, and value is the # nodes
     * Hints1: use {@link ASTElement#forEach(Consumer)} method to implement the function
     * Hint2: note that `countChildren` method is removed, please do not use this method
     */
    public Supplier<List<Map.Entry<String, Integer>>> processNodeFreq = () -> {
        List<Map.Entry<String, Integer>> funcName2NodeNum = new ArrayList<>();
        id2ASTModules.forEach((moduleID, module) ->
                module.getChildren().forEach(astElement ->
                        astElement.filter(el -> el instanceof FunctionDefStmt)
                                .forEach(func ->
                                        funcName2NodeNum.add(new AbstractMap.SimpleEntry<>(moduleID + "_" + ((FunctionDefStmt) func).getName() + "_" + func.getLineNo(), countNodes(func)))
                        )
            )
        );
//        System.out.println(funcName2NodeNum.size());
        funcName2NodeNum.sort((a, b) -> b.getValue() - a.getValue());
//        funcName2NodeNum.forEach(entry -> System.out.println(entry.getKey() + " " + entry.getValue()));
        return funcName2NodeNum;
    };

    private int countNodes(ASTElement astElement) {
        int i = 1;
        for (ASTElement child : astElement.getChildren())
            i += countNodes(child);
        return i;
    }


}
