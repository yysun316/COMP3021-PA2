package hk.ust.comp3021.query;

import hk.ust.comp3021.expr.ASTExpr;
import hk.ust.comp3021.expr.CallExpr;
import hk.ust.comp3021.expr.CompareExpr;
import hk.ust.comp3021.expr.NameExpr;
import hk.ust.comp3021.misc.*;
import hk.ust.comp3021.stmt.*;
import hk.ust.comp3021.utils.*;

import javax.naming.Name;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class QueryOnMethod {
    /**
     * IMPORTANT: for all test cases for QueryOnMethod, we would not involve class
     */
    ASTModule module = null;

    public QueryOnMethod(ASTModule module) {
        this.module = module;
    }

    /**
     * TODO `findEqualCompareInFunc` find all comparison expression with operator \"==\" in current module {@link QueryOnMethod#module}
     *
     * @param funcName the name of the function to be queried
     * @return results List of strings where each represents a comparison expression, in format, lineNo:colOffset-endLineNo:endColOffset
     * Hints1: if func does not exist in current module, return empty list
     * Hints2: use {@link ASTElement#filter(Predicate)} method to implement the function
     */
    public Function<String, List<String>> findEqualCompareInFunc = (funcName) -> {
        List<String> res = new ArrayList<>();
        module.filter(astElement -> astElement instanceof FunctionDefStmt && funcName.equals(((FunctionDefStmt) astElement).getName())).forEach(func -> func.filter(el -> el instanceof CompareExpr).forEach(compareExpr -> ((CompareExpr) compareExpr).getOps().forEach(op -> {
            if ("Eq".equals(op.getOperatorName()))
                res.add(compareExpr.getLineNo() + ":" + compareExpr.getColOffset() + "-" + compareExpr.getEndLineNo() + ":" + compareExpr.getEndColOffset());
        })));
        return res;
    };

    /**
     * TODO `findFuncWithBoolParam` find all functions that use boolean parameter as if condition in current module {@link QueryOnMethod#module}
     *
     * @param null
     * @return List of strings where each represents the name of function that satisfy the requirements
     * Hints1: the boolean parameter is annotated with type bool
     * Hints2: as long as the boolean parameter shown in the {@link IfStmt#getTest()} expression, we say it's used
     * Hints3: use {@link ASTElement#filter(Predicate)} method to implement the function
     */
    public Supplier<List<String>> findFuncWithBoolParam = () -> {
        List<String> names = new ArrayList<>();
        List<String> tmp = new ArrayList<>();
        // Find all the parameter name in test
        module.filter(astElement -> astElement instanceof FunctionDefStmt).forEach(func -> func.filter(child -> child instanceof IfStmt).forEach(ifStmt -> {
            ASTExpr test = ((IfStmt) ifStmt).getTest();
            if (test instanceof NameExpr) {
                String id = ((NameExpr) test).getId();
                tmp.add(id);
            }
        }));
//        System.out.println(tmp);
        // Find all the arg id that has annotation bool and check if it's used in ifstmt
        module.filter(astElement -> astElement instanceof FunctionDefStmt).forEach(func -> func.filter(child -> child instanceof ASTArguments).forEach(args -> args.filter(child -> child instanceof ASTArguments.ASTArg && ((ASTArguments.ASTArg) child).getAnnotation() != null).forEach(arg -> {
            ASTArguments.ASTArg astArg = (ASTArguments.ASTArg) arg;
            ASTExpr expr = astArg.getAnnotation();
            if (expr instanceof NameExpr && "bool".equals(((NameExpr) expr).getId())) {
                String id = ((ASTArguments.ASTArg) arg).getArg();
                if (tmp.contains(id)) names.add(((FunctionDefStmt) func).getName());
            }
        })));
        return names;
    };


    /**
     * TODO Given func name `funcName`, `findUnusedParamInFunc` find all unused parameter in current module {@link QueryOnMethod#module}
     *
     * @param funcName to be queried function name
     * @return results List of strings where each represents the name of an unused parameter
     * Hints1: if a variable is read, the ctx is `Load`, otherwise `Store` if written
     * Hints2: for the case where variable is written before read, we use line number and col offset to
     * check if the write operation is conducted before the first place where the parameter is read
     * Hints3: use {@link ASTElement#filter(Predicate)} method to implement the function
     * Hints4: if func does not exist in current module, return empty list
     */
    public Function<String, List<String>> findUnusedParamInFunc = (funcName) -> {
        List<String> names = new ArrayList<>();
        List<String> loadNames = new ArrayList<>(); // Store all the variable ctx is load and not load after store
        HashMap<String, NameExpr> storeNames = new HashMap<>(); // Store all the variable ctx is store
        // Get all the variables that load only
        module.filter(astElement -> astElement instanceof FunctionDefStmt && funcName.equals(((FunctionDefStmt) astElement).getName())) // get the function
                .forEach(func -> func.filter(child -> child instanceof NameExpr) // get NameExpr
                        .forEach(e -> {
                            NameExpr expr = ((NameExpr) e);
                            String type = expr.getCtx().getOp().name().substring(4); // ctx type
                            String id = expr.getId(); // name of the variable
                            if ("Load".equals(type)) {
                                if (storeNames.containsKey(id)) {
                                    int lineNo = storeNames.get(id).getLineNo();
                                    int colOffset = storeNames.get(id).getColOffset();
//                                            System.out.println("StoreNames has the key: " + id + " lineNo: " + lineNo + " coloffset: " + colOffset);
//                                            System.out.println("Current expr: " + "lineNo: " + expr.getLineNo() + " coloffset: " + expr.getColOffset());
                                    if (lineNo > expr.getLineNo() || lineNo == expr.getLineNo() && colOffset > expr.getColOffset())
                                        loadNames.add(id);
                                } else {
//                                            System.out.println("StoreNames doesn't have the key: " + id + " adding it to loadNames.");
                                    loadNames.add(id);
                                }
                            } else if ("Store".equals(type)) {
//                                        System.out.println("Adding to storeNames: " + id);
                                storeNames.put(id, expr);
                            }
//                                    System.out.print("StoreNames: ");
//                                    storeNames.forEach((k,v) -> System.out.print(k));
//                                    System.out.println();
//                                    System.out.println("loadNames: " + loadNames);
//                                    System.out.println();
                        }));
//        System.out.println("adsf");
//        System.out.println(loadNames);
        // Find all the arg id that has annotation bool and check if it's used in ifstmt
        module.filter(astElement -> astElement instanceof FunctionDefStmt && funcName.equals(((FunctionDefStmt) astElement).getName())) // get the function
                .forEach(func -> func.filter(child -> child instanceof ASTArguments).forEach(args -> args.filter(child -> child instanceof ASTArguments.ASTArg).forEach(arg -> {
                    ASTArguments.ASTArg astArg = (ASTArguments.ASTArg) arg;
                    if (!loadNames.contains(astArg.getArg())) names.add(((ASTArguments.ASTArg) arg).getArg());
                })));
        return names;
    };


    /**
     * TODO Given func name `funcName`, `findDirectCalledOtherB` find all functions being direct called by functions other than B in current module {@link QueryOnMethod#module}
     *
     * @param funcName the name of function B
     * @return results List of strings where each represents the name of a function that satisfy the requirement
     * Hints1: there is no class in the test cases for this code pattern, thus, no function names such as a.b()
     * Hints2: for a call expr foo(), we can directly use the called function name foo to location the implementation
     * Hints3: use {@link ASTElement#filter(Predicate)} method to implement the function
     * Hints4: if func does not exist in current module, return empty list
     */
    public Function<String, List<String>> findDirectCalledOtherB = (funcName) -> {
        List<String> names = new ArrayList<>();
        List<String> tmp = new ArrayList<>();
        ArrayList<ASTElement> functions = module.filter(astElement -> astElement instanceof FunctionDefStmt && !funcName.equals(((FunctionDefStmt) astElement).getName()));
        functions.forEach(astElement -> tmp.add(((FunctionDefStmt) astElement).getName()));
        functions.forEach(astElement -> {
            ArrayList<CallExpr> allCalledFunc = ((FunctionDefStmt) astElement).getAllCalledFunc();
            allCalledFunc.forEach(func -> {
                if (tmp.contains(func.getCalledFuncName())) names.add(func.getCalledFuncName());
            });
        });
        return names;
    };

    /**
     * TODO Given func name `funcNameA` and `funcNameB`, `answerIfACalledB` checks if A calls B directly or transitively in current module {@link QueryOnMethod#module}
     *
     * @param funcNameA the name of function A
     * @param funcNameB the name of function B
     * @return a boolean return value to answer yes or no
     * Hints1: there is no class in the test cases for this code pattern, thus, no function names such as a.b()
     * Hints2: for a call expr foo(), we can directly use the called function name foo to location the implementation
     * Hints3: use {@link ASTElement#filter(Predicate)} method to implement the function
     */

    public BiPredicate<String, String> answerIfACalledB = (funcNameA, funcNameB) -> {
        // Get funcA
        ArrayList<ASTElement> funcA = module.filter((astElement -> astElement instanceof FunctionDefStmt && funcNameA.equals(((FunctionDefStmt) astElement).getName())));
        if (funcA.isEmpty())
            return false;

        Set<String> calledFuncNames = new HashSet<>();
        // Get all the called func names in funcA
        funcA.get(0).filter(astElement -> astElement instanceof CallExpr).forEach(callExpr ->
            calledFuncNames.add(((CallExpr) callExpr).getCalledFuncName())
        );

        // Check if funcA calls funcB transitively
        for (String name : calledFuncNames) {
            if (name.equals(funcNameB) || this.answerIfACalledB.test(name, funcNameB)) return true;
            ArrayList<ASTElement> curFunc = module.filter(astElement -> astElement instanceof FunctionDefStmt && name.equals(((FunctionDefStmt) astElement).getName()));
            if (!curFunc.isEmpty())
                curFunc.get(0).filter(astElement -> astElement instanceof CallExpr).forEach(callExpr ->
                        calledFuncNames.add(((CallExpr) callExpr).getCalledFuncName())
                );
        }

        return false;
    };

}
