package backend.instr;

import backend.MSBlock;
import backend.MSModule;
import backend.operand.MSOperand;
import frontend.irgenerate.irclass.Value;

/**
 * 跳转指令
 */
public class MSJump extends MSInstr {
    private String condType;// bne, bnez, j等均可
    private MSOperand value1;
    private MSOperand value2;
    private MSOperand target;

    public MSJump(String condType, MSOperand target) {
        this.condType = condType;//无条件跳转, 有j和jal
        this.target = target;
        MSModule.msNowBlock.addInstr(this);
    }

    public MSJump(String condType, MSOperand value1, MSOperand target) {
        this.condType = condType;
        this.value1 = value1;
        addUse(value1);
        this.target = target;
        MSModule.msNowBlock.addInstr(this);
    }

    public boolean isJal() {
        return condType.equals("jal");
    }

    public boolean isReturn(){
        return condType.equals("jr");
    }
    @Override
    public void replaceReg(MSOperand oldReg, MSOperand newReg) {
        super.replaceReg(oldReg, newReg);
        if (oldReg.equals(value1)) {
            value1 = newReg;
        }
        if (oldReg.equals(value2)) {
            value2 = newReg;
        } else {
//            System.err.println("replaceReg! in jump");
        }
    }

    @Override
    public String toString() {
        //无条件跳转
        if (value1 == null) {
            return condType + " " + target;
        } else if (value2 == null) {
            return condType + " " + value1 + ", " + target;
        } else {
            return condType + " " + value1 + ", " + value2 + ", " + target;
        }
    }

}
