package backend.instr;

import backend.MSModule;
import backend.operand.Imm;
import backend.operand.MSLabel;
import backend.operand.MSOperand;

public class MSMove extends MSInstr {
    private MSOperand dst;
    private MSOperand src;
    private MSOperand offset = null;// la $t0, label($t0)

    public MSMove(MSOperand dst, MSOperand src) {
        this.dst = dst;
        this.src = src;
        addDef(dst);
        addUse(src);
        MSModule.msNowBlock.addInstr(this);
    }

    public MSMove(MSOperand lval, MSOperand rval, MSOperand offset) {
        this.dst = lval;
        this.src = rval;
        this.offset = offset;
        addDef(dst);
        addUse(src);
        addUse(this.offset);
        MSModule.msNowBlock.addInstr(this);

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
        if (oldReg.equals(offset)) {
            offset = newReg;
        } else {
//            System.out.println("replaceReg! in move");
        }
    }

    @Override
    public String toString() {
        if (src instanceof MSLabel && offset == null) {
            return "la " + dst + ", " + src;
        } else if (src instanceof MSLabel) {
            return "la " + dst + ", " + src + "(" + offset + ")";
        } else if (src instanceof Imm) {
            return "li " + dst + ", " + src;
        } else {
            return "move " + dst + ", " + src;
        }
    }
}
