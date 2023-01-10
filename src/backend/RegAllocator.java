package backend;

import backend.instr.MSBinary;
import backend.instr.MSInstr;
import backend.instr.MSJump;
import backend.instr.MSLoad;
import backend.instr.MSStore;
import backend.operand.Imm;
import backend.operand.PhyReg;
import backend.operand.Reg;
import backend.operand.VirReg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class RegAllocator {
    private List<PhyReg> phyRegs = new ArrayList<>();

    private List<MSFunction> functions;
    /**
     * 临时寄存器池
     */
    private List<PhyReg> tempRegPool = new ArrayList<>();
    /**
     * 虚拟寄存器到物理寄存器的映射.
     */
    private HashMap<VirReg, PhyReg> vir2phy = new HashMap<>();
    private List<VirReg> virRegPool = new ArrayList<>();

    private HashSet<VirReg> defRegs = new HashSet<>();
    private MSFunction nowFunction;
    private MSBlock nowBlock;
    private int nowOff2Sp = 0;
    public RegAllocator() {
        functions = MSModule.msModule.getMsfunctions();
    }

    /**
     * 临时寄存器分配策略: 遇到寄存器, 如果有对应的全局寄存器, 那么不分配(目前省略);
     * 如果没有对应的, 那么从栈上相应的溢出区来lw, 分配给一个临时寄存器, 产生映射关系.
     * 如果临时寄存器不够了, 那么将一个临时寄存器放回到栈上, 再来分配. 每个基本块退出时,
     * 将用了的临时寄存器放回栈上,
     */
    public void allocTempRegs() {
        //进行临时寄存器的分配
        for (MSFunction function : functions) {
            allocTempRegsInFunc(function);
        }
    }

    public void allocTempRegsInFunc(MSFunction function) {
        nowFunction = function;
        for (MSBlock block : function.getBlocks()) {
            nowBlock = block;
            allocTempRegsInBlock(block);
        }
    }

    public void allocTempRegsInBlock(MSBlock block) {
        restoreTempRegPool();//恢复寄存器池
        LinkedList<MSInstr> instrs = block.getInstrs();
        int i, j;
        for (i = 0; i < instrs.size(); i++) {
            MSInstr instr = instrs.get(i);
            if(instr instanceof MSLoad){
                MSLoad load = (MSLoad) instr;
                if(load.isNeedUpdate()){
                    load.setOff(new Imm(nowFunction.getOff(load.getUpdateValue(), nowOff2Sp)));
                    continue;
                }
            } else if (instr instanceof MSStore){
                MSStore store = (MSStore) instr;
                if(store.isNeedUpdate()){
                    store.setOff(new Imm(nowFunction.getOff(store.getUpdateValue(), nowOff2Sp)));
                    continue;
                }
            }else if(instr instanceof MSBinary){
                MSBinary mb = (MSBinary) instr;
                if(mb.isBeforeCall()){
                    //增减栈操作
                    nowOff2Sp += mb.getOff2Sp();
                }
            }else if (instr instanceof MSJump && ((MSJump) instr).isJal()) {
                saveAllTempRegs(instrs, i, true);
                nowOff2Sp = 0;
            }
            ArrayList<Reg> regUses = instr.getRegUse();
            for (j = 0; j < regUses.size(); j++) {
                Reg use = regUses.get(j);
                if (use.needsAllocate()) {
                    Reg phy = tempRegAllocate(use, instrs, i, false);
                    if (phy != null) {
                        instr.replaceReg(use, phy);
                    }
                }
            }
            //大小应该是1
            ArrayList<Reg> regDefs = instr.getRegDef();
            for (j = 0; j < regDefs.size(); j++) {
                Reg def = regDefs.get(j);
                if (def.needsAllocate()) {
                    defRegs.add((VirReg) def);
                    Reg phy = tempRegAllocate(def, instrs, i, true);
                    if (phy != null)
                        instr.replaceReg(def, phy);
                }
            }
        }
        if(instrs.getLast() instanceof MSJump && ((MSJump)instrs.getLast()).isReturn()){
            return;
        }
        for (i = instrs.size() - 1; i >= 0; i--) {
            if (!(instrs.get(i) instanceof MSJump)) {
                break;
            }
        }
        saveAllTempRegs(instrs, i + 1, false);
    }

    /**
     * 为虚拟寄存器分配临时物理寄存器, 返回分配的物理寄存器.
     * 返回null表示不需要分配寄存器
     *
     * @param reg    虚拟寄存器
     * @param instrs 指令序列
     * @param pos    虚拟寄存器所在指令在序列中的index
     */
    private PhyReg tempRegAllocate(Reg reg, LinkedList<MSInstr> instrs, int pos, boolean isDst) {
        if (reg.needsAllocate()) {
            //如果全局寄存器有映射
            PhyReg globalReg = nowFunction.getGlobalReg(reg);
            if(globalReg != null){
                return globalReg;
            }
            //如果已经为虚拟寄存器分配物理物理寄存器, 则返回
            if (vir2phy.containsKey(reg)) {
                return vir2phy.get(reg);
            }
            //如果还没有为虚拟寄存器分配物理寄存器, 则
            if (!tempRegPool.isEmpty()) {
                PhyReg phy = tempRegPool.remove(0);
                //只有使用方才会load
                if (!isDst) {
                    MSLoad msLoad = new MSLoad(phy, PhyReg.sp, new Imm(nowFunction.getOff((VirReg) reg, nowOff2Sp)));
                    instrs.add(pos, msLoad);
                }
                addVir2Phy((VirReg) reg, phy);
                return phy;
            } else { //临时寄存器用完了
                VirReg vir = virRegPool.remove(0);
                PhyReg phy = vir2phy.remove(vir);
                //如果虚拟寄存器在栈上有空间, 需要写回
                if (nowFunction.includeVirReg(vir) && defRegs.contains(vir)) {
                    MSStore msStore = new MSStore(phy, PhyReg.sp, new Imm(nowFunction.getOff(vir, nowOff2Sp)));
                    instrs.add(pos++, msStore);
                }
                if (!isDst) {
                    MSLoad msLoad = new MSLoad(phy, PhyReg.sp, new Imm(nowFunction.getOff((VirReg) reg, nowOff2Sp)));
                    instrs.add(pos, msLoad);
                }
                addVir2Phy((VirReg) reg, phy);
                return phy;
            }
        }
        return null;
    }

    private void addVir2Phy(VirReg vir, PhyReg phy) {
        vir2phy.put(vir, phy);
        virRegPool.add(vir);
    }

    /**
     * 恢复临时寄存器池, 每次进入基本块都要进行该操作.
     */
    public void restoreTempRegPool() {
        tempRegPool.clear();
        tempRegPool.addAll(PhyReg.getTempRegs());
        virRegPool.clear();
        vir2phy.clear();
        defRegs.clear();
    }

    /**
     * 调用函数时, 保存所有临时寄存器的值到栈上
     *
     * @param instrs
     * @param pos
     */
    private void saveAllTempRegs(LinkedList<MSInstr> instrs, int pos, boolean isCall) {
        for (VirReg virReg : vir2phy.keySet()) {
            PhyReg phy = vir2phy.get(virReg);
            if (nowFunction.includeVirReg(virReg) && defRegs.contains(virReg)) {//如果有必要保存(非一次性使用的常量), 才保存
                instrs.add(pos++, new MSStore(phy, PhyReg.sp, new Imm(nowFunction.getOff(virReg, nowOff2Sp))));
            }
        }
        //这样如果之后有寄存器还会使用, 那么会自动复原
//        virRegPool.clear();
//        vir2phy.clear();
        restoreTempRegPool();
    }

    private boolean include(List<Reg> includes, Reg obj) {
        for (Reg include : includes) {
            if (obj.equals(include)) {
                return true;
            }
        }
        return false;
    }
}
