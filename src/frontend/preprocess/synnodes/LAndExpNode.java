package frontend.preprocess.synnodes;

import frontend.irgenerate.instr.Branch;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Def;
import frontend.preprocess.ESymbolTable;
import frontend.preprocess.Visitor;

public class LAndExpNode extends Token {
    public LAndExpNode() {
    }

    public Value visitLAndExp(BasicBlock trueBlock, BasicBlock falseBlock){
        int pos = 0, size = sons.size();
        BasicBlock nextBlock = null;
        while (pos+1<size){//pos指向exp
            nextBlock = new BasicBlock(Visitor.curFunc);
            new Branch(getSon(pos).visit(), nextBlock, falseBlock);
            Visitor.curBlock = nextBlock;
            pos+=2;
        }
        nextBlock = trueBlock;
        new Branch(getSon(pos).visit(), nextBlock, falseBlock);
        return null;
    }
    @Override
    public void check(ESymbolTable ESymbolTable, int loopCycles) {
        for (Token son : sons) {
            if (Def.And.equals(son.getType())) continue;
            son.check(ESymbolTable, loopCycles);
        }
    }
}
