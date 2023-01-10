package frontend.preprocess.synnodes;

import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.ESymbolTable;

public class PrimaryExpNode extends Token {
    private int dim = 0;
    public PrimaryExpNode() {
    }

    public Value visit() {
        if (getSon(0) instanceof NumberNode){
            int number = ((NumberNode)getSon(0)).getNumber();
            return new Constant.ConstantInt(number);
        }else if(getSon(0) instanceof LvalNode){//lval
            LvalNode lvalNode = (LvalNode) getSon(0);
            lvalNode.setNeedLoad(true);//小心数组
            return lvalNode.visit();
        }else {
            return getSon(1).visit();
        }
    }

    public int getDim() {
        return dim;
    }

    public void check(ESymbolTable eSymbolTable, int loopCycles) {
        int pos = 0;
        if (getSon(pos) instanceof LvalNode) {
            getSon(pos).check(eSymbolTable, loopCycles);
            dim = ((LvalNode) getSon(pos)).getDim();
        } else if (getSon(pos) instanceof NumberNode) {
            dim = 0;
        } else {//(exp)
            pos++;
            getSon(pos).check(eSymbolTable, loopCycles);
            dim = ((AddExpNode) getSon(pos)).getDim();
        }
    }
}
