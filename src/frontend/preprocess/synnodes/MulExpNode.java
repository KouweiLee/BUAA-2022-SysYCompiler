package frontend.preprocess.synnodes;

import frontend.irgenerate.instr.BinaryOperator;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.ESymbolTable;
import utils.Evaluate;

import static frontend.irgenerate.instr.Zext.trimToI32;

public class MulExpNode extends Token {
    int dim = 0;

    public MulExpNode() {
    }

    public int getDim() {
        return dim;
    }

    public Value visit() {
        int size = sons.size();
        Value first = sons.get(0).visit();
        for (int i = 1; i < size; i++) {
            Token op = sons.get(i++);
            Value second = sons.get(i).visit();
            first = trimToI32(first);
            second = trimToI32(second);
            if(first.isConstantInt() && second.isConstantInt()){
                first = Evaluate.caculate(op, first, second);
            }else {
                first = new BinaryOperator(op.getContent(), first, second);
            }
        }
        return first;
    }

    @Override
    public void check(ESymbolTable eSymbolTable, int loopCycles) {
        for (Token son : sons) {
            if (son instanceof UnaryExpNode)
                son.check(eSymbolTable, loopCycles);
        }
        dim = ((UnaryExpNode) getSon(0)).getDim();
    }

}
