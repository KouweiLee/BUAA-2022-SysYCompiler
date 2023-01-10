package frontend.irgenerate.instr;

import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.User;
import frontend.irgenerate.irclass.Value;

//Instr子类指令可以不用管名称等
public class Instr extends User {
    protected BasicBlock basicBlock;

    public enum Opcode {
        ADD("add", "add"),
        SUB("sub", "sub", "add"),//减法如果要逆转, 那么立即数要变为原来的负数
        MUL("mul", "mul"),
        DIV("sdiv", "div"),
        SREM("srem", "rem"),
        EQ("eq", "seq"),
        NE("ne", "sne"),
        SGT("sgt", "sgt", "slt"),//大于
        SGE("sge", "sge", "sle"),//大于等于
        SLT("slt", "slt", "sgt"),
        SLE("sle", "sle", "sge"),
        ZEXT("zext"),
        GEP("getelementptr"),
        CALL("call"),
        ALLOCA("alloca"),
        LOAD("load"),
        STORE("store"),
        BRANCH("br"),
        JUMP("br"),
        PHI("phi"),
        RETURN("ret"),
        PCOPY("pcopy"),
        MOVE("move");
        private String name;
        private String mips_name;
        private String antiMips_name;

        Opcode(String name) {
            this.name = name;
        }

        Opcode(String name, String mips_name) {
            this.name = name;
            this.mips_name = mips_name;
        }

        Opcode(String name, String mips_name, String antiMips_name) {
            this.name = name;
            this.mips_name = mips_name;
            this.antiMips_name = antiMips_name;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getmips() {
            return mips_name;
        }

        public String getAntiMips() {
            if(antiMips_name != null)
                return antiMips_name;
            else
                return mips_name;
        }
    }

    protected Opcode opcode;
    public static int LOCAL_COUNT = 0;

    public Opcode getOpcode() {
        return opcode;
    }

    public Instr() {
        super(null);
    }

    public Instr(Type type, BasicBlock parent) {
        super(type);
        this.basicBlock = parent;
        prefix = LOCAL_PREFIX;
        name = LOCAL_NAME_PREFIX + LOCAL_COUNT++;
        if (!basicBlock.isTerminated()) {
            this.basicBlock.insertEnd(this);
        }
    }

    public Instr(Type type, Opcode opcode, BasicBlock parent) {
        super(type);
        this.opcode = opcode;
        this.basicBlock = parent;
        prefix = LOCAL_PREFIX;
        name = LOCAL_NAME_PREFIX + LOCAL_COUNT++;
        if (!basicBlock.isTerminated()) {
            this.basicBlock.insertEnd(this);
        }
    }

    public Instr(Type type, Opcode opcode, BasicBlock parent, boolean insertHead) {
        super(type);
        this.opcode = opcode;
        this.basicBlock = parent;
        prefix = LOCAL_PREFIX;
        name = LOCAL_NAME_PREFIX + LOCAL_COUNT++;
        this.basicBlock.insertHead(this);
    }

    public boolean isTerminator() {
        return opcode.equals(Opcode.RETURN) || opcode.equals(Opcode.BRANCH) ||
                opcode.equals(Opcode.JUMP);
    }

    public String getOpcodeName() {
        return opcode.toString();
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

    /**
     * 从原基本块中移除本指令, 关键是要把本指令操作数的users除去本指令. 关键是其他指令.
     */
    public void remove() {
        basicBlock.removeInstr(this);
        basicBlock = null;
        users.clear();
        //双向的
        for (Value operand : operands) {
            operand.removeUser(this);
        }
        operands.clear();
    }
}
