package frontend.irgenerate.instr;

import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Visitor;

import java.util.HashSet;

public class Load extends Instr {
    public Load(Value pointer) {
        super(((Type.PointerType) pointer.getType()).getInnerType(), Opcode.LOAD, Visitor.curBlock);
        addValueDef(pointer);
    }

    @Override
    public HashSet<Value> getUse4bb(){
        use4bb.addAll(operands);
        return use4bb;
    }

    @Override
    public String toString() {
        return getFullName() + " = load " + type + ", " + getOperand(0).getTypeName();
    }
}
