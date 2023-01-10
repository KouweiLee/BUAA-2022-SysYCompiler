package backend.instr;

import backend.operand.MSOperand;
import backend.operand.Reg;

import java.util.ArrayList;

public class MSInstr {
    private final ArrayList<Reg> regDef = new ArrayList<>();
    private final ArrayList<Reg> regUse = new ArrayList<>();

    public void addDef(MSOperand operand) {
        if (operand != null && operand instanceof Reg) {
            regDef.add((Reg) operand);
        }
    }

    public void addUse(MSOperand operand) {
        if (operand != null && operand instanceof Reg) {
            regUse.add((Reg) operand);
        }
    }

    public ArrayList<Reg> getRegDef() {
        return regDef;
    }

    public ArrayList<Reg> getRegUse() {
        return regUse;
    }

    public void replaceReg(MSOperand oldReg, MSOperand newReg) {
        for (int i = 0; i < regUse.size(); i++) {
            if (regUse.get(i).equals(oldReg)) {
                regUse.set(i, (Reg) newReg);
            }
        }
        for (int i = 0; i < regDef.size(); i++) {
            if (regDef.get(i).equals(oldReg)) {
                regDef.set(i, (Reg) newReg);
            }
        }
    }
}
