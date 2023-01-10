package frontend.irgenerate.instr;

import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class BinaryOperator extends Instr {
//    private List<Value> uses = new ArrayList<>();
//    private List<Value> defs = new ArrayList<>();

    public BinaryOperator(String op, Value value1, Value value2) {
        super(value1.getType(), Visitor.curBlock);
        switch (op) {
            case "+":
                opcode = Opcode.ADD;
                break;
            case "-":
                opcode = Opcode.SUB;
                break;
            case "*":
                opcode = Opcode.MUL;
                break;
            case "/":
                opcode = Opcode.DIV;
                break;
            case "%":
                opcode = Opcode.SREM;
                break;
            default:
                break;
        }
        addValueDef(value1, value2);
    }

    @Override
    public HashSet<Value> getUse4bb(){
        use4bb.add(getOperand(0));
        use4bb.add(getOperand(1));
        return use4bb;
    }

    @Override
    public String toString() {
        return getFullName() + " = " + opcode.toString() + " " + type + " " + getOperand(0).getFullName() + ", " + getOperand(1).getFullName();
    }
    @Override
    public String getOpcodeName(){
        return opcode.getmips();
    }

//    public List<Value> getUse(){
//        return uses;
//    }

}
