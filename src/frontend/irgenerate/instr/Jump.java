package frontend.irgenerate.instr;

import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Visitor;

public class Jump extends Instr {
    public Jump(Value value, BasicBlock parent){
        super(Type.VoidType.VOID_TYPE, Opcode.JUMP, parent);
        addValueDef(value);
    }

    public Jump(Value value) {
        super(Type.VoidType.VOID_TYPE, Opcode.JUMP, Visitor.curBlock);
        addValueDef(value);
    }

    @Override
    public Value getDef4bb(){
        return null;
    }
    public BasicBlock getTarget() {
        return (BasicBlock) getOperand(0);
    }

    @Override
    public String toString() {
        return "br " + getOperand(0).getTypeName();
    }


}
