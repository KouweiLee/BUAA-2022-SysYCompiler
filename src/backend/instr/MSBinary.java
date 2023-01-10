package backend.instr;

import backend.MSModule;
import backend.operand.Imm;
import backend.operand.MSOperand;

public class MSBinary extends MSInstr {
    private MSOperand dst;//从左到右
    private MSOperand src1;
    private MSOperand src2;
    private String type;// 包括slt等
    private boolean isBeforeCall = false;

    public MSOperand getDst() {
        return dst;
    }

    public MSOperand getSrc1() {
        return src1;
    }

    public MSOperand getSrc2() {
        return src2;
    }

    public MSBinary(String type, MSOperand dst, MSOperand src1, MSOperand src2) {
        this.dst = dst;
        this.src1 = src1;
        this.src2 = src2;
        if (type.equals("add")) {
            this.type = "add";
        } else if (type.equals("sub")) {
            this.type = "sub";
        } else {
            this.type = type;
        }
        addDef(dst);
        addUse(src1);
        addUse(src2);
        MSModule.msNowBlock.addInstr(this);
    }

    public void setBeforeCall() {
        isBeforeCall = true;
    }

    public boolean isBeforeCall() {
        return isBeforeCall;
    }

    public int getOff2Sp() {
        Imm imm = (Imm) src2;
        return imm.getNum();
    }

    @Override
    public void replaceReg(MSOperand oldReg, MSOperand newReg) {
        super.replaceReg(oldReg, newReg);
        if (oldReg.equals(dst)) {
            dst = newReg;
        }
        if (oldReg.equals(src1)) {
            src1 = newReg;
        }
        if (oldReg.equals(src2)) {
            src2 = newReg;
        } else {
//            System.err.println("replaceReg! in binary");
        }
    }

    @Override
    public String toString() {
        if ("sub".equals(type)  && src2 instanceof Imm) {
            return "add" + " " + dst + ", " + src1 + ", " + ((Imm) src2).getNumSub();
        }
        return type + " " + dst + ", " + src1 + ", " + src2;
    }
}
