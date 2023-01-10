package backend.instr;

import backend.MSModule;
import backend.operand.MSOperand;
import frontend.irgenerate.irclass.Value;

public class MSLoad extends MSInstr {
    private MSOperand dst;
    private MSOperand src;
    private MSOperand off;
    private boolean isNeedUpdate = false;

    public MSLoad(MSOperand dst, MSOperand src, MSOperand off) {
        this.dst = dst;
        this.src = src;
        this.off = off;
        addDef(dst);
        addUse(src);
        addUse(off);
        if (MSModule.msNowBlock != null)
            MSModule.msNowBlock.addInstr(this);
    }

    public void setOff(MSOperand off) {
        this.off = off;
    }

    public boolean isNeedUpdate() {
        return isNeedUpdate;
    }

    public void setNeedUpdate(boolean needUpdate) {
        isNeedUpdate = needUpdate;
    }

    private Value updateValue;

    public Value getUpdateValue() {
        return updateValue;
    }

    public void setUpdateValue(Value updateValue) {
        this.updateValue = updateValue;
    }

    @Override
    public void replaceReg(MSOperand oldReg, MSOperand newReg) {
        super.replaceReg(oldReg, newReg);
        if (oldReg.equals(dst)) {
            dst = newReg;
        }
        if (oldReg.equals(src)) {
            src = newReg;
        }
        if (oldReg.equals(off)) {
            off = newReg;
            super.replaceReg(oldReg, newReg);
        } else {
//            System.err.println("replaceReg! in load");
        }
    }

    @Override
    public String toString() {
        return "lw " + dst + ", " + off + "(" + src + ")";
    }
}
