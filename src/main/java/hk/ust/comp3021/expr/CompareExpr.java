package hk.ust.comp3021.expr;

import hk.ust.comp3021.misc.*;
import hk.ust.comp3021.utils.*;

import java.util.*;


public class CompareExpr extends ASTExpr {
    // Compare(expr left, cmpop* ops, expr* comparators)
    private ASTExpr left;
    private ArrayList<ASTEnumOp> ops = new ArrayList<>();
    private ArrayList<ASTExpr> comparators = new ArrayList<>();

    public CompareExpr(XMLNode node) {
        super(node);
        this.exprType = ASTExpr.ExprType.Compare;
        this.left = ASTExpr.createASTExpr(node.getChildByIdx(0));
        for (XMLNode opNode : node.getChildByIdx(1).getChildren()) {
            this.ops.add(new ASTEnumOp(opNode));
        }
        for (XMLNode comparatorNode : node.getChildByIdx(2).getChildren()) {
            this.comparators.add(ASTExpr.createASTExpr((comparatorNode)));
        }
    }

    public ArrayList<ASTEnumOp> getOps() {
        return ops;
    }

    @Override
    public ArrayList<ASTElement> getChildren() {
        ArrayList<ASTElement> children = new ArrayList<>();
        children.add(left);
        children.addAll(comparators);
        return children;
    }
}
