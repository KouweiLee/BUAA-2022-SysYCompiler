package frontend.preprocess.synnodes;

import frontend.irgenerate.instr.BinaryOperator;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.ESymbolTable;
import utils.Evaluate;

public class AddExpNode extends Token {
    int dim = 0;

    public AddExpNode() {
    }

    public Value visit() {
        int size = sons.size();
        Value first = sons.get(0).visit();
        for (int i = 1; i < size; i++) {
            Token op = sons.get(i++);
            Value second = sons.get(i).visit();
            if(first.isConstantInt() && second.isConstantInt()){
                first = Evaluate.caculate(op, first, second);
            }else {
                first = new BinaryOperator(op.getContent(), first, second);
            }
        }
        return first;
    }

    public int getDim() {
        return dim;
    }

    @Override
    public void check(ESymbolTable ESymbolTable, int loopCycles) {
        for (Token son : sons) {
            if (son instanceof MulExpNode)
                son.check(ESymbolTable, loopCycles);
        }
        dim = ((MulExpNode) getSon(0)).getDim();
    }

}
