package backend.instr;

import backend.MSModule;
import backend.operand.MSOperand;
import frontend.irgenerate.irclass.Value;

public class MSStore extends MSInstr {
    private MSOperand src;
    private MSOperand dst;
    private MSOperand off;

    public MSStore(MSOperand src, MSOperand dst, MSOperand off) {
        this.src = src;
        this.dst = dst;
        this.off = off;
        addUse(dst);//dst的值是被使用的
        addUse(src);
        addUse(off);
        if (MSModule.msNowBlock != null)
            MSModule.msNowBlock.addInstr(this);
    }

    public void setOff(MSOperand off) {
        this.off = off;
    }

    private boolean isNeedUpdate = false;

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
        } else {
//            System.err.println("replaceReg! in store");
        }
    }

    @Override
    public String toString() {
        return "sw " + src + ", " + off + "(" + dst + ")";
    }
}
