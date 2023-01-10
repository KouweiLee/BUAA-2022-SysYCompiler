package frontend.irgenerate.instr;

import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Branch extends Instr {
//    private List<Value> uses = new ArrayList<>();
    private static int ZEXT_COUNT = 0;

    public Branch(Value cond, Value value1, Value value2) {
        super(Type.VoidType.VOID_TYPE, Opcode.BRANCH, Visitor.curBlock);
        addValueDef(cond);
        addValueDef(value1, value2);
    }

    @Override
    public HashSet<Value> getUse4bb(){
        use4bb.add(getOperand(0));
        return use4bb;
    }

    @Override
    public Value getDef4bb(){
        return null;
    }
    public void setTrueTarget(BasicBlock basicBlock) {
        operands.set(1, basicBlock);
    }

    public void setFalseTarget(BasicBlock basicBlock) {
        operands.set(2, basicBlock);
    }

    public BasicBlock getTrueTarget() {
        return (BasicBlock) getOperand(1);
    }

    public BasicBlock getFalseTarget() {
        return (BasicBlock) getOperand(2);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String pre = "br i1 ";
        String behi = ", " + getOperand(1).getTypeName() + ", " + getOperand(2).getTypeName();
//        if (!(getOperand(0) instanceof Icmp)) {
//            String zext_tmp = LOCAL_PREFIX + "zex" + ZEXT_COUNT++;
//            sb.append(zext_tmp).append(" = zext ")
//                    .append(getOperand(0).getTypeName()).append(" to i1\n\t");
//            sb.append(pre).append(zext_tmp).append(behi);
//        } else {
        sb.append(pre).append(getOperand(0).getFullName()).append(behi);
//        }
        return sb.toString();
    }
}
