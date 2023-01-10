package frontend.irgenerate.instr;

import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Value;

import java.util.HashSet;
import java.util.List;

public class Phi extends Instr{
    public Phi(List<Value> values, BasicBlock parent){
        super(Type.BasicType.I32, Opcode.PHI, parent, true);
        addValueDef(values);
    }

    @Override
    public HashSet<Value> getUse4bb(){
        use4bb.addAll(operands);
        return use4bb;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(getFullName()).append(" = ").append("phi ").append(type).append(" ");
        for (int i=0; i<operands.size();i++) {
            sb.append("[").append(operands.get(i).getFullName()).append(", ")
                    .append(basicBlock.getPrecBBs().get(i).getFullName()).append("]");
            if(i!=operands.size()-1){
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
