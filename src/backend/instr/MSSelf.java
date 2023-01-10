package backend.instr;

import backend.MSModule;

public class MSSelf extends MSInstr{
    private String instr;

    public MSSelf(String instr) {
        this.instr = instr;
        MSModule.msNowBlock.addInstr(this);
    }

    @Override
    public String toString() {
        return instr;
    }
}
