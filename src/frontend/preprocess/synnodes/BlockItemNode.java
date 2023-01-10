package frontend.preprocess.synnodes;

import frontend.irgenerate.irclass.Value;
import frontend.preprocess.ESymbolTable;

public class BlockItemNode extends Token {
    public BlockItemNode() {
    }

    @Override
    public Value visit(){
        getSon(0).visit();
        return null;
    }

    public void check(ESymbolTable ESymbolTable, int loopCycles, boolean isCheck) {
        Token token = getSon(0);
        if (token instanceof DeclNode) {
            token.check(ESymbolTable, loopCycles);
        } else {
            ((StmtNode) token).check(ESymbolTable, loopCycles, isCheck);
        }
    }
}