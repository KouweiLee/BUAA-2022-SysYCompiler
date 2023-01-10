package frontend.irgenerate.instr;

import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Visitor;

public class Alloca extends Instr {
    private Type innerType;

    public Alloca(Type type) {
        super(type.toPointerType(), Opcode.ALLOCA, Visitor.curBlock.getFunction().getFirstBlock(), true);
        this.innerType = type;
    }

    /**
     * 不是分配的空间大小, 而是分配的元素个数
     * @return
     */
    public int getSize() {
        return innerType.getSize();
    }

    public boolean isArrayAlloca() {
        return innerType instanceof Type.ArrayType;
    }

    @Override
    public String toString() {
        return getFullName() + " = alloca " + innerType;
    }

    @Override
    public Value getDef4bb(){
        return null;
    }
}
