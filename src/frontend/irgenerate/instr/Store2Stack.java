package frontend.irgenerate.instr;

import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Value;

public class Store2Stack extends Instr {
    private int reg;
    private Value value;

    public Store2Stack(Integer reg, Value value, BasicBlock parent) {
        //注意，不会让其插入到基本块中，需要自行插入
        super(Type.VoidType.VOID_TYPE, parent);
        this.reg = reg;
        this.value = value;
    }

    public int getReg() {
        return reg;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "store reg" + reg + " to stack as " + value.getFullName();
    }
}
