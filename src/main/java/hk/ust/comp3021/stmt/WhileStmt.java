package hk.ust.comp3021.stmt;

import hk.ust.comp3021.expr.*;
import hk.ust.comp3021.misc.*;
import hk.ust.comp3021.utils.*;
import java.util.ArrayList;

public class WhileStmt extends ASTStmt {
    // While(expr test, stmt* body, stmt* orelse)
    private ASTExpr test;
    private ArrayList<ASTStmt> body = new ArrayList<>();
    private ArrayList<ASTStmt> orelse = new ArrayList<>();

    public WhileStmt(XMLNode node) {
        super(node);
        this.stmtType = ASTStmt.StmtType.While;
        this.test = ASTExpr.createASTExpr(node.getChildByIdx(0));

        for (XMLNode bodyNode : node.getChildByIdx(1).getChildren()) {
            body.add(ASTStmt.createASTStmt(bodyNode));
        }

        for (XMLNode orelseNode : node.getChildByIdx(2).getChildren()) {
            orelse.add(ASTStmt.createASTStmt(orelseNode));
        }
    }

    @Override
    public ArrayList<ASTElement> getChildren() {
        ArrayList<ASTElement> children = new ArrayList<>();
        children.add(test);
        children.addAll(body);
        children.addAll(orelse);
        return children;
    }
}
