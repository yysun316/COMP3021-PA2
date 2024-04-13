package hk.ust.comp3021.query;

import hk.ust.comp3021.expr.*;
import hk.ust.comp3021.misc.*;
import hk.ust.comp3021.stmt.*;
import hk.ust.comp3021.utils.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class BugDetector {
    ASTModule module = null;

    public BugDetector(ASTModule module) {
        this.module = module;
    }

    /**
     * TODO Returns all the functions that contains a bug of unclosed files in the current module
     * {@link QueryOnClass#module}
     *
     * @return results List of strings of names of the functions
     */
    public Supplier<List<String>> detect = () -> {
        // Get all the functions
        ArrayList<ASTElement> functions = module.filter(astElement -> astElement instanceof FunctionDefStmt);
        // Create a table which stores all the variable -> callExpr
        // (if it's not a call expr, we need to find it in the table)
        // If variable is assigned to others before close called on it, or before it's assigned to another variable,
        // it has bug
        // If a callExpr is called on the variable, we remove the variable from table.
        Set<String> bugFunc = new HashSet<>();
        for (ASTElement function : functions) {
            // Create table
            HashMap<String, CallExpr> map = new HashMap<>();
//            System.out.println("Function is: " + ((FunctionDefStmt) function).getName());
            function.forEach(astElement -> {

                if (astElement instanceof AssignStmt && !addEntry(map, (AssignStmt) astElement)) {
//                    System.out.println("Function " + ((FunctionDefStmt) function).getName() + " has bug");
                    bugFunc.add(((FunctionDefStmt) function).getName());
                } else if (astElement instanceof CallExpr) {
                    removeEntry(map, ((CallExpr) astElement));
                }

//                System.out.println("map now is: ");
//                map.forEach((key, value) -> System.out.println(key + " and " + value.getCalledFuncName()));
//                System.out.println();
            });
            if (!map.isEmpty()) bugFunc.add(((FunctionDefStmt) function).getName());
//            System.out.println("bugFunc: " + bugFunc);
//            System.out.println("------------------------------------------");
        }

        return new ArrayList<>(bugFunc);
    };

    private void removeEntry(HashMap<String, CallExpr> map, CallExpr expr) {
//        System.out.println("Start Removal");
        String calledFuncName = expr.getCalledFuncName();
//        System.out.println("calledFuncName: " + calledFuncName);
        String[] splitName = calledFuncName.split("\\.");
        if (splitName.length < 2) return;
        String id = splitName[0];
//        System.out.println("id: " + id);
        CallExpr callExpr = map.get(id);
        if (callExpr != null) {
            Iterator<Map.Entry<String, CallExpr>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, CallExpr> next = iter.next();
                if (next.getValue() == callExpr) iter.remove();
            }
        }
//        System.out.println("Finished Removal");
    }

    private boolean addEntry(HashMap<String, CallExpr> map, AssignStmt stmt) {
        List<NameExpr> targets = stmt.getTargets().stream().map(target -> (NameExpr) target).toList();
//        System.out.print("targets: ");
//        targets.forEach(x -> System.out.print(x.getId()));
//        System.out.println();
        ASTExpr value = stmt.getValue();
        for (NameExpr target : targets) {
            String id = target.getId();
            if (!map.containsKey(id)) {
                // xxx = open()
                if (value instanceof CallExpr) {
                    map.put(id, (CallExpr) value);
                } else { // xxx = yyy
                    NameExpr name = (NameExpr) value;
                    map.put(id, map.get(name.getId())); // xxx = map.get(yyy) = open()
                }
            } else {
                // find assigning bug (premature assignment)
                CallExpr expr = map.get(id);
                Map<String, CallExpr> filteredMap = map.entrySet().stream().filter(e -> !e.getKey().equals(id))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                if (!filteredMap.containsValue(expr)) return false;
                NameExpr name = (NameExpr) value;
                map.put(id, map.get(name.getId())); // xxx = map.get(yyy) = open()
            }
        }
        return true;
    }

}
