package frontend.irgenerate.instr;

import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Value;

import java.util.ArrayList;
import java.util.List;

public class Pcopy extends Instr{
    private static int index = 0;
    private List<Value> leftOps;// dst
    private List<Value> rightOps; // src

    public Pcopy(BasicBlock parent){
        super(Type.VoidType.VOID_TYPE, Opcode.PCOPY, parent);
        leftOps = new ArrayList<>();
        rightOps = new ArrayList<>();
        index++;
    }

    public void addOps(Value left, Value right){
        leftOps.add(left);
        rightOps.add(right);
    }

    public List<Value> getLeftOps(){
        return leftOps;
    }

    public List<Value> getRightOps() {
        return rightOps;
    }

    /**
     * 如果所有左部都依次等于右部, 则这个copy是无用的.
     */
    public boolean isUseless(){
        for(int i=0; i< leftOps.size(); i++){
            if(!leftOps.get(i).equals(rightOps.get(i))){
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return null;
    }
}
