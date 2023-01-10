package frontend.preprocess.synnodes;

import frontend.irgenerate.irclass.Value;
import frontend.preprocess.ESymbolTable;
import frontend.preprocess.Visitor;

public class DeclNode extends Token implements SynNode {
    private boolean isConstDecl;

    public DeclNode() {
    }

    public void setConstDecl(boolean constDecl) {
        isConstDecl = constDecl;
    }

    @Override
    public Value visit() {
        Visitor.isConst = isConstDecl;
        for (int i = 1; i < sons.size(); i++) {
            sons.get(i).visit();
        }
        Visitor.isConst = false;
        return null;
    }

    @Override
    public void check(ESymbolTable eSymbolTable, int loopCycles) {
        for (int i = 1; i < sons.size(); i++) {
            sons.get(i).check(eSymbolTable, loopCycles);
        }
    }
}
