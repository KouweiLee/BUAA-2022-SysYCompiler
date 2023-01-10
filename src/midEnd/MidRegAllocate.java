package midEnd;

import backend.operand.PhyReg;
import frontend.irgenerate.instr.Alloca;
import frontend.irgenerate.instr.Call;
import frontend.irgenerate.instr.Instr;
import frontend.irgenerate.instr.LoadFromStack;
import frontend.irgenerate.instr.Phi;
import frontend.irgenerate.instr.Store2Stack;
import frontend.irgenerate.irclass.Argument;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.Function;
import frontend.irgenerate.irclass.GlobalValue;
import frontend.irgenerate.irclass.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class MidRegAllocate {

    private List<Function> functions;
    //活跃变量分析
    private boolean flag = false;

    public MidRegAllocate(List<Function> functions) {
        this.functions = functions;
        //活跃变量分析
        analysisForFunction();
        //debug
        if (false)
            outuseAndDef();
        //寄存器分配
        allocateForFunc();
    }

    private void outuseAndDef() {
        for (Function function : functions) {
            LinkedList<BasicBlock> bbs = function.getBasicBlocks();
            for (BasicBlock bb : bbs) {
                HashSet<Value> use = bb.getUse();
                System.out.print("\n" + bb.getFullName() + ": use: ");
                for (Value value : use) {
                    System.out.print(value.getFullName() + " ");
                }
                HashSet<Value> def = bb.getDef();
                System.out.print("\n" + bb.getFullName() + ": def: ");
                for (Value value : def) {
                    System.out.print(value.getFullName() + " ");
                }
            }
        }
    }

    /**
     * 活跃变量分析
     */
    private void analysisForFunction() {
        for (Function function : functions) {
            LinkedList<BasicBlock> bbs = function.getBasicBlocks();
            for (BasicBlock bb : bbs) {
                //为每个基本块计算use和def
                calculateUseAndDef(bb);
            }
            while (true) {
                flag = false;
                HashSet<BasicBlock> visited = new HashSet<>();
                for (int i = 0; i < bbs.size(); i++) {
                    if (bbs.get(i).getSuccBBs().size() == 0) {
                        analysisForBlock(bbs.get(i), visited);
                    }
                }
                //当in改变时，flag变为true
                if (!flag) {
                    break;
                }
            }
        }
    }

    /**
     * 为基本块计算use和def
     *
     * @param bb
     */
    private void calculateUseAndDef(BasicBlock bb) {
        HashSet<Value> use = new HashSet<>();
        HashSet<Value> def = new HashSet<>();
        LinkedList<Instr> instrs = bb.getInstrs();
        for (Instr instr : instrs) {
            HashSet<Value> use4bb = instr.getUse4bb();
            Value def4bb = instr.getDef4bb();
            //增加use
            for (Value value : use4bb) {
                if (value instanceof GlobalValue || value instanceof Constant || value instanceof Alloca) {
                    continue;
                }
                if (def.contains(value)) {
                    continue;
                }
                use.add(value);
            }
            //增加def
            if (!(def4bb == null || use.contains(def4bb))) {
                def.add(def4bb);
            }
        }
        bb.setDef(def);
        bb.setUse(use);
    }

    /**
     * 对基本块进行活跃变量分析
     *
     * @param bb
     */
    private void analysisForBlock(BasicBlock bb, HashSet<BasicBlock> visited) {
        if (visited.contains(bb)) {
            return;
        }
        visited.add(bb);
        HashSet<Value> in = bb.getIn();
        HashSet<Value> out = bb.getOut();
        for (BasicBlock succBB : bb.getSuccBBs()) {
            out.addAll(succBB.getIn());
        }
        int presize = in.size();
        if (in.size() == 0) {
            in.addAll(bb.getUse());
        }
        for (Value value : out) {
            if (bb.getDef().contains(value)) {
                continue;
            }
            in.add(value);
        }
        if (in.size() != presize) {
            flag = true;
        }
        for (BasicBlock precBB : bb.getPrecBBs()) {
            analysisForBlock(precBB, visited);
        }
    }

    //寄存器分配
    private HashMap<Value, Integer> value2reg;
    private HashMap<Integer, Value> reg2Value = new HashMap<>();
    //保存空闲寄存器
    private List<Integer> freeRegsPool = new ArrayList<>();
    //    private List<Integer> allocedRegs = new ArrayList<>();
    private HashSet<Value> notAllocas = new HashSet<>();
    private HashSet<Integer> temps = new HashSet<>();
    private BasicBlock nowBlock;
    private LinkedList<Instr> nowInstrs;
    private int nowPos;

    private void allocateForFunc() {
        for (Function function : functions) {
            freeRegsPool.clear();
            freeRegsPool.addAll(PhyReg.getRegIndexes());
            reg2Value.clear();
            notAllocas.clear();
            value2reg = new HashMap<>();
            List<Argument> arguments = function.getArguments();
            nowBlock = function.getFirstBlock();
            nowInstrs = nowBlock.getInstrs();
            nowPos = 0;
            for (Argument argument : arguments) {
                allocateReg(argument, false);
            }
            BasicBlock firstBlock = function.getFirstBlock();
            allocateForBlock(firstBlock);
            function.setGlobalRegsMap(value2reg);
        }
    }

    private HashSet<Value> nowAllocs;

    private void allocateForBlock(BasicBlock bb) {
        nowBlock = bb;
        LinkedList<Instr> instrs = bb.getInstrs();
        nowInstrs = instrs;
        HashSet<Integer> defRegs = new HashSet<>();
        //nowPos指向当前指令
        for (nowPos = 0; nowPos < instrs.size(); nowPos++) {
            if (instrs.get(nowPos) instanceof LoadFromStack || instrs.get(nowPos) instanceof Store2Stack) {
                continue;
            }
            if (instrs.get(nowPos) instanceof Call && !((Call) instrs.get(nowPos)).isExternal()) {
                for (Integer reg : reg2Value.keySet()) {
                    instrs.add(nowPos, new Store2Stack(reg, reg2Value.get(reg), nowBlock));
                    nowPos++;
                }
            }
            HashSet<Value> use4bb = instrs.get(nowPos).getUse4bb();
            Value def4bb = instrs.get(nowPos).getDef4bb();
            HashSet<Value> allocs = new HashSet<>();
            if (!(instrs.get(nowPos) instanceof Phi))
                allocs.addAll(use4bb);
            if (def4bb != null) {
                allocs.add(def4bb);
            }
            nowAllocs = allocs;
            for (Value value : allocs) {
                if (value instanceof GlobalValue || value instanceof Constant || value instanceof Alloca) {
                    continue;
                }
                allocateReg(value, use4bb.contains(value));
            }
            for (Integer temp : temps) {
                reg2Value.remove(temp);
                freeRegsPool.add(temp);
            }
            temps.clear();
            if (instrs.get(nowPos) instanceof Call && !((Call) instrs.get(nowPos)).isExternal()) {
                for (Integer reg : reg2Value.keySet()) {
                    if (def4bb != null && reg2Value.get(reg).equals(def4bb)) {
                        continue;
                    }
                    if (isValueUseless(reg2Value.get(reg))) {
                        continue;
                    }
                    nowPos++;
                    nowInstrs.add(nowPos, new LoadFromStack(reg, reg2Value.get(reg), nowBlock));
                }
            }
        }
        //获取支配的基本块
        HashSet<BasicBlock> idoms = bb.getIdoms();
        for (BasicBlock idom : idoms) {
            HashMap<Integer, Value> tmp = new HashMap<>(reg2Value);
            List<Integer> tmpRegs = new ArrayList<>(freeRegsPool);
            HashSet<Integer> remove = new HashSet<>();
            for (Integer reg : reg2Value.keySet()) {
                //在下面的基本块中不会被使用
                if (!idom.getIn().contains(reg2Value.get(reg))) {
                    remove.add(reg);
                }
            }
            for (Integer reg : remove) {
                reg2Value.remove(reg);
                freeRegsPool.add(reg);
            }
            allocateForBlock(idom);
            reg2Value.clear();
            freeRegsPool.clear();
            reg2Value.putAll(tmp);
            freeRegsPool.addAll(tmpRegs);
        }
    }

    private void restoreFreeRegs() {
        reg2Value.clear();
        freeRegsPool.addAll(PhyReg.getRegIndexes());
    }

    private void mapRegAndValue(Integer reg, Value value) {
        value2reg.put(value, reg);
        reg2Value.put(reg, value);
    }

    /**
     * 将Reg对应的变量解除映射关系，并且该value将被保存到栈上，不使用寄存器。
     *
     * @param reg
     */
    private void reduceMap(Integer reg) {
        Value value = reg2Value.get(reg);
        notAllocas.add(value);
        value2reg.remove(value);
        reg2Value.remove(reg);
    }

    /**
     * 给定value，将其对应的reg和它映射，并将reg从寄存器池移去。返回reg。
     *
     * @param value
     */
    private Integer restoreValue(Value value) {
        Integer reg = value2reg.get(value);
        reg2Value.put(reg, value);
        for (int i = 0; i < freeRegsPool.size(); i++) {
            if (freeRegsPool.get(i).equals(reg)) {
                freeRegsPool.remove(i);
                break;
            }
        }
        return reg;
    }

    private void allocateReg(Value value, boolean isUse) {
        //TODO: 检查每个分支。
        if (notAllocas.contains(value) || value instanceof Phi) {
            return;
        }
        //不存在映射关系
        else if (!value2reg.containsKey(value)) {
            if (!freeRegsPool.isEmpty()) {
                Integer reg = freeRegsPool.remove(0);
                if (value instanceof Argument)
                    nowInstrs.add(nowPos++, new LoadFromStack(reg, value, nowBlock));
                mapRegAndValue(reg, value);
                return;
            } else {
                //不分配寄存器
                notAllocas.add(value);
                return;
            }
        }
        //如果存在映射关系,但reg没对上号。应该不会出现该问题， 如果出现则错误
        else if (!value.equals(reg2Value.get(value2reg.get(value)))) {
//            System.out.println(nowAllocs);
//            if(value.getFullName().equals("%f10")){
//                System.out.println(true);
//            }
            System.out.println(value.getFullName() + " " + nowBlock.getFullName() + " " + nowInstrs.get(nowPos));
            System.out.println("reg" + value2reg.get(value));
//            不存在这个reg
            System.out.println(reg2Value.get(value2reg.get(value)));
//            Integer reg = restoreValue(value);
//            nowInstrs.add(nowPos++, new LoadFromStack(reg, value, nowBlock));
//            reg2Value.put()
        }
        //如果对上号，那就不管了，进行下一步。
        //如果out中没有这个变量，而且是最后一次使用，释放reg
        boolean isUseless = isValueUseless(value);
        if (isUseless) {
            Integer reg = value2reg.get(value);
            temps.add(reg);
        }
    }

    private boolean isValueUseless(Value value) {
        boolean isUseless = true;
        if (!nowBlock.getOut().contains(value)) {
            for (int i = nowPos + 1; i < nowInstrs.size(); i++) {
                if (nowInstrs.get(i).getUse4bb().contains(value) ||
                        value.equals(nowInstrs.get(i).getDef4bb())) {
                    isUseless = false;
                    break;
                }
            }
        } else {
            isUseless = false;
        }
        return isUseless;
    }

}

