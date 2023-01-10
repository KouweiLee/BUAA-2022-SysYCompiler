package frontend.preprocess.synnodes;

import frontend.irgenerate.instr.Icmp;
import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.ESymbolTable;

import static frontend.irgenerate.instr.Zext.trimToI32;

public class RelExpNode extends Token {
    public RelExpNode() {
    }

    public Value visit() {
        int size = sons.size();
        Value first = sons.get(0).visit();
        for (int i = 1; i < size; i++) {
            Token op = sons.get(i++);
            Value second = sons.get(i).visit();
            first = trimToI32(first);//转换为I32
            second = trimToI32(second);//转换为I32
            first = new Icmp(op.getContent(), first, second);
        }
        return first;
    }

    @Override
    public void check(ESymbolTable ESymbolTable, int loopCycles) {
        for (Token son : sons) {
            if (son instanceof AddExpNode)
                son.check(ESymbolTable, loopCycles);
        }
    }
}
