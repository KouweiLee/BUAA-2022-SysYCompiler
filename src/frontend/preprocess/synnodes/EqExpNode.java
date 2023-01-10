package frontend.preprocess.synnodes;

import frontend.irgenerate.Type;
import frontend.irgenerate.instr.Icmp;
import frontend.irgenerate.instr.Zext;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.ESymbolTable;
import frontend.preprocess.Visitor;

import static frontend.irgenerate.instr.Zext.trimToI32;

public class EqExpNode extends Token implements SynNode {
    public EqExpNode() {
    }

    public Value visit() {
        int size = sons.size();
        Value first = sons.get(0).visit();//RelExpNode
        for (int i = 1; i < size; i++) {
            Token op = sons.get(i++);
            Value second = sons.get(i).visit();
            first = trimToI32(first);//转换为I32
            second = trimToI32(second);
            first = new Icmp(op.getContent(), first, second);
        }
        if (!(first instanceof Icmp)) {//转化为Icmp
            first = new Icmp("!=", first, Constant.ConstantInt.ZERO);
        }
        return first;
    }

//    public static Value trimToI32(Value value) {
//        Type type = value.getType();
//        if(type instanceof Type.BasicType && !type.equals(Type.BasicType.I32)){
//            return new Zext(value, Type.BasicType.I32);
//        }else {
//            return value;
//        }
//    }

    @Override
    public void check(ESymbolTable ESymbolTable, int loopCycles) {
        for (Token son : sons) {
            if (son instanceof RelExpNode)
                son.check(ESymbolTable, loopCycles);
        }
    }
}
