package frontend.irgenerate.instr;

import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Visitor;

import java.util.HashSet;

public class Icmp extends Instr {
    public Icmp(String op, Value value1, Value value2) {
        //icmp的返回值定为i1
        super(Type.BasicType.I1, Visitor.curBlock);
        switch (op) {
            case "==":
                opcode = Opcode.EQ;
                break;
            case "!=":
                opcode = Opcode.NE;
                break;
            case ">":
                opcode = Opcode.SGT;
                break;
            case ">=":
                opcode = Opcode.SGE;
                break;
            case "<":
                opcode = Opcode.SLT;
                break;
            case "<=":
                opcode = Opcode.SLE;
                break;
            default:
                assert false;
        }
        addValueDef(value1, value2);
    }

    @Override
    public HashSet<Value> getUse4bb() {
        use4bb.addAll(operands);
        return use4bb;
    }

    @Override
    public String toString() {
        return getFullName() + " = " + "icmp " + opcode + " " +
                getOperand(0).getTypeName() + ", " + getOperand(1).getFullName();
    }

    public String getOpcodeName() {
        return opcode.getmips();
    }
}
