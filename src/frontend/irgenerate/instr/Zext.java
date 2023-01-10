package frontend.irgenerate.instr;

import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Visitor;

public class Zext extends Instr{
    public Zext(Value value, Type targetType){
        super(targetType, Opcode.ZEXT, Visitor.curBlock);
        addValueDef(value);
    }

    @Override
    public String toString() {
        return getFullName() + " = zext " + getOperand(0).getTypeName() + " to " + type.toString();
    }

    public static Value trimToI32(Value value) {
//        Type type = value.getType();
//        if(type instanceof Type.BasicType && !type.equals(Type.BasicType.I32)){
//            return new Zext(value, Type.BasicType.I32);
//        }else {
            return value;
//        }
    }
}
