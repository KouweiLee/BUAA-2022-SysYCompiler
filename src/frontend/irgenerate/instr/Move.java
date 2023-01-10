package frontend.irgenerate.instr;

import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Value;

import java.util.HashSet;

public class Move extends Instr {
    private Value dst;
    private Value src;

    public Move(Value dst, Value src, BasicBlock parent) {
        super(Type.VoidType.VOID_TYPE, Opcode.MOVE, parent);
        this.dst = dst;
        this.src = src;

    }

    @Override
    public HashSet<Value> getUse4bb(){
        use4bb.add(src);
        return use4bb;
    }

    @Override
    public Value getDef4bb(){
        return dst;
    }
    @Override
    public String toString() {
        return "move " + dst.getFullName() + " <-- " + src.getFullName();
    }

    public Value getDst() {
        return dst;
    }

    public Value getSrc() {
        return src;
    }
}
