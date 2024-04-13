package hk.ust.comp3021.query;

import hk.ust.comp3021.expr.*;
import hk.ust.comp3021.misc.*;
import hk.ust.comp3021.stmt.*;
import hk.ust.comp3021.utils.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class QueryOnClass {

    ASTModule module = null;

    public QueryOnClass(ASTModule module) {
        this.module = module;
    }

    /**
     * TODO Given class name `className`, `findSuperClasses` finds all the super classes of
     * it in the current module {@link QueryOnClass#module}
     *
     * @param className the name of class
     * @return results List of strings where each represents the name of a class that satisfy the requirement
     * Hint1: you can implement a helper function which receives the class name and
     * returns the ClassDefStmt object.
     * Hint2: You can first find the direct super classes, and then RECURSIVELY finds the
     * super classes of the direct super classes.
     */
    public Function<String, List<String>> findSuperClasses = (className) -> {
        Set<String> names = new HashSet<>();
        ClassDefStmt classStmt = this.getClassDefStmt.apply(className);
        if (classStmt != null) {
            ArrayList<ASTExpr> bases = classStmt.getBases();
            for (ASTExpr base : bases) {
                names.add(((NameExpr) base).getId());
            }
        }
        // Recursive call
        for (String name : names) {
            names.addAll(this.findSuperClasses.apply(name));
        }
        return new ArrayList<>(names);
    };
    private Function<String, ClassDefStmt> getClassDefStmt = className -> {
        ArrayList<ASTElement> elements = module.filter(astElement -> astElement instanceof ClassDefStmt &&
                ((ClassDefStmt) astElement).getName().equals(className));
        if (!elements.isEmpty())
            return (ClassDefStmt) elements.get(0);
        return null;
    };

    /**
     * TODO Given class name `classA` and `classB` representing two classes A and B,
     *  `haveSuperClass` checks whether B is a super class of A in the current module.
     *  {@link QueryOnClass#module}
     *
     * @param classA the name of class A.
     * @param classB the name of class B
     * @return returns true if B is A's super class, otherwise false.
     * Hint1: you can just reuse {@link QueryOnClass#findSuperClasses}
     */
    public BiFunction<String, String, Boolean> haveSuperClass = (classA, classB) -> {
        for (String name : this.findSuperClasses.apply(classA)) {
            if (name.equals(classB))
                return true;
        }
        return false;
    };


    /**
     * TODO Returns all the overriding methods within the current module
     * {@link QueryOnClass#module}
     *
     * @return results List of strings of the names of overriding methods.
     * Note: If there are multiple overriding functions with the same name, please include name
     * in the result list for MULTIPLE times. You can refer to the test case.
     * Hint1: you can implement a helper function that first finds the methods that a class
     * directly contains.
     * Hint2: you can reuse the results of {@link QueryOnClass#findSuperClasses}
     */
    public Supplier<List<String>> findOverridingMethods = () -> {
        List<String> names = new ArrayList<>();
        // Get all the classes
        ArrayList<ASTElement> classDefStmts = module.filter(astElement -> astElement instanceof ClassDefStmt);
        // Each class find it's super class
        for (ASTElement classDefStmt : classDefStmts) {
            List<String> curClassFuncs = this.getMethods.apply(((ClassDefStmt) classDefStmt).getName());
//            System.out.println("curClassFuncs: " + curClassFuncs);
            List<String> superClassNames = ((ClassDefStmt) classDefStmt).getBases()
                    .stream()
                    .map(astExpr -> ((NameExpr) astExpr).getId()).toList();
//            System.out.println("superClassNames: " + superClassNames);
            for (String superClassName : superClassNames) {
                List<String> parentFuncs = this.getMethods.apply(superClassName);
//                System.out.println("parentFunc: " + parentFuncs);
                parentFuncs.forEach(name -> {
                    if (curClassFuncs.contains(name)) {
                        names.add(name);
                    }
                });
            }
//            System.out.println("names: " + names);
//            System.out.println();
        }
//        System.out.println(names);
        return names;

    };
    private Function<String, List<String>> getMethods = className ->
            module.filter(astElement -> astElement instanceof ClassDefStmt &&
                            ((ClassDefStmt) astElement).getName().equals(className))
                    .get(0).filter(stmt -> stmt instanceof FunctionDefStmt)
                    .stream().map(func -> ((FunctionDefStmt) func).getName())
                    .collect(Collectors.toList());


    /**
     * TODO Returns all the methods that a class possesses in the current module
     * {@link QueryOnClass#module}
     *
     * @param className the name of the class
     * @return results List of strings of names of the methods it possesses
     * Note: the same function name should appear in the list only once, due to overriding.
     * Hint1: you can implement a helper function that first finds the methods that a class
     * directly contains.
     * Hint2: you can reuse the results of {@link QueryOnClass#findSuperClasses}
     */
    public Function<String, List<String>> findAllMethods = (className) -> {
        Set<String> names = new HashSet<>();
        this.findSuperClasses.apply(className).forEach(name -> names.addAll(this.getMethods.apply(name)));
        names.addAll(this.getMethods.apply(className));
        return new ArrayList<>(names);
    };


    /**
     * TODO Returns all the classes that possesses a main function in the current module
     * {@link QueryOnClass#module}
     *
     * @return results List of strings of names of the classes
     * Hint1: You can reuse the results of {@link QueryOnClass#findAllMethods}
     */
    public Supplier<List<String>> findClassesWithMain = () ->
            module.filter(astElement -> astElement instanceof ClassDefStmt)
                    .stream()
                    .map(classDefStmt -> ((ClassDefStmt) classDefStmt).getName())
                    .filter(name -> this.findAllMethods.apply(name).contains("main"))
                    .collect(Collectors.toList());
//        List<String> names = new ArrayList<>();
//        ArrayList<ASTElement> classDefStmts = module.filter(astElement -> astElement instanceof ClassDefStmt);
//        for (ASTElement classDefStmt : classDefStmts) {
//            String name = ((ClassDefStmt) classDefStmt).getName();
//            List<String> curMethods = this.findAllMethods.apply(name);
//            if (curMethods.contains("main"))
//                names.add(name);
//        }
//        return names;

}

