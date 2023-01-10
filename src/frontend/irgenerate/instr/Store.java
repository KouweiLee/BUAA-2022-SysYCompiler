package frontend.irgenerate.instr;

import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Visitor;

import java.util.HashSet;

public class Store extends Instr {
    public Store(Value value, Value pointer) {
        super(Type.VoidType.VOID_TYPE, Opcode.STORE, Visitor.curBlock);
        addValueDef(value, pointer);
    }

    @Override
    public HashSet<Value> getUse4bb(){
        use4bb.addAll(operands);
        return use4bb;
    }

    @Override
    public Value getDef4bb(){
        return null;
    }
    @Override
    public String toString() {
        return "store " + getOperand(0).getTypeName() + ", " + getOperand(1).getTypeName();
    }


    public Value getValue(){
        return getOperand(0);
    }
}
