package frontend.irgenerate.instr;

import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Visitor;

import java.util.HashSet;

public class Return extends Instr {
    public Return() {
        super(Type.VoidType.VOID_TYPE, Opcode.RETURN, Visitor.visitor.curBlock);
    }

    public Return(Value value) {
        super(value.getType(), Opcode.RETURN, Visitor.visitor.curBlock);
        addValueDef(value);
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
        if (type.equals(Type.VoidType.VOID_TYPE)) {
            return "ret void";
        } else {
            return "ret " + getOperand(0).getTypeName();
        }
    }
}
