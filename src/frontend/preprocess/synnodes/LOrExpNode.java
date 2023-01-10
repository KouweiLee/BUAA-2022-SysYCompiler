package frontend.preprocess.synnodes;

import frontend.irgenerate.instr.Branch;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Def;
import frontend.preprocess.ESymbolTable;
import frontend.preprocess.Visitor;

public class LOrExpNode extends Token {
    public LOrExpNode() {
    }

    public Value visitLOrExpNode(BasicBlock trueBlock, BasicBlock falseBlock) {
        int pos = 0, size = sons.size();
        BasicBlock nextBlock = null;
        while (pos + 1 < size) {
            nextBlock = new BasicBlock(Visitor.curFunc);
            ((LAndExpNode) getSon(pos)).visitLAndExp(trueBlock, nextBlock);
            Visitor.curBlock = nextBlock;
            pos += 2;
        }
        ((LAndExpNode) getSon(pos)).visitLAndExp(trueBlock, falseBlock);
        return null;
    }

    public void check(ESymbolTable ESymbolTable, int loopCycles) {
        for (Token son : sons) {
            if (Def.Or.equals(son.getType())) continue;
            son.check(ESymbolTable, loopCycles);
        }
    }
}
