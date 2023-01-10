package frontend.irgenerate.irclass;

import frontend.irgenerate.instr.Instr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class BasicBlock extends Value {
    private Function function;
    private LinkedList<Instr> instrs;
    public static int BBLOCK_COUNT = 0;
    //前驱基本块和后继基本块
    private List<BasicBlock> precBBs = new ArrayList<>();
    private List<BasicBlock> succBBs = new ArrayList<>();
    //本block所支配的所有基本块
    private HashSet<BasicBlock> doms;
    //本block所直接支配的所有基本块
    private HashSet<BasicBlock> idoms;
    //本block的支配边界, 恰好不被本基本块支配的基本块集合
    private HashSet<BasicBlock> DF;
    //活跃变量分析时的in, out, use和def
    private HashSet<Value> in = new HashSet<>();
    private HashSet<Value> out = new HashSet<>();
    private HashSet<Value> use;
    private HashSet<Value> def;

    public void setUse(HashSet<Value> use) {
        this.use = use;
    }

    public void setDef(HashSet<Value> def) {
        this.def = def;
    }

    public HashSet<Value> getUse() {
        return use;
    }

    public HashSet<Value> getDef() {
        return def;
    }

    public HashSet<Value> getIn() {
        return in;
    }

    public HashSet<Value> getOut() {
        return out;
    }

    public BasicBlock(Function function) {
        this.function = function;
        instrs = new LinkedList<>();
        //获取%bxx: 使用getFullName
        prefix = LOCAL_PREFIX;
        name = "b" + BBLOCK_COUNT++;
        this.function.insertBlockEnd(this);
    }

    /**
     * 不知道所在函数初始化的情况, 用于FuncDef
     */
    public BasicBlock() {
        instrs = new LinkedList<>();
        prefix = LOCAL_PREFIX;
        name = "b" + BBLOCK_COUNT++;
    }

    public HashSet<BasicBlock> getDoms() {
        return doms;
    }

    public void setDoms(HashSet<BasicBlock> doms) {
        this.doms = doms;
    }

    public HashSet<BasicBlock> getIdoms() {
        return idoms;
    }

    public void setIdoms(HashSet<BasicBlock> idoms) {
        this.idoms = idoms;
    }

    public HashSet<BasicBlock> getDF() {
        return DF;
    }

    public void setDF(HashSet<BasicBlock> DF) {
        this.DF = DF;
    }

    public List<BasicBlock> getPrecBBs() {
        return precBBs;
    }

    public void setPrecBBs(List<BasicBlock> precBBs) {
        this.precBBs = precBBs;
    }

    public List<BasicBlock> getSuccBBs() {
        return succBBs;
    }

    public void setSuccBBs(List<BasicBlock> succBBs) {
        this.succBBs = succBBs;
    }

    public void setFunction(Function function) {
        this.function = function;
        function.insertBlockEnd(this);
    }

    public Function getFunction() {
        return function;
    }

    public Instr getFirstInstr() {
        return instrs.get(0);
    }

    public LinkedList<Instr> getInstrs() {
        return instrs;
    }

    public void insertHead(Instr instr) {
        instrs.addFirst(instr);
    }

    /**
     * 在终结指令之前加入instr
     */
    public void insertBeforeEnd(Instr instr) {
        instrs.add(instrs.size() - 1, instr);
    }

    public void insertEnd(Instr instr) {
        assert !isTerminated();
        instrs.add(instr);
    }

    public String getBlockLabel() {
        return name;
    }

    @Override
    public String getTypeName() {
        return "label " + getFullName();
    }

    //在MakeDFG的过程中, 不应该有empty
    public Instr getLastInstr() {
        if (instrs.isEmpty()) {
            return null;
        }
        return instrs.getLast();
    }

    public boolean isTerminated() {
        return getLastInstr() != null && getLastInstr().isTerminator();
    }

    public void removeInstr(Instr instr) {
        instrs.remove(instr);
    }

    @Override
    public void remove() {
        super.remove();
//        function.getBasicBlocks().remove(this);
        while (instrs.size() > 0) {
            instrs.get(0).remove();
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
