package midEnd;

import frontend.irgenerate.instr.Alloca;
import frontend.irgenerate.instr.Instr;
import frontend.irgenerate.instr.Load;
import frontend.irgenerate.instr.Phi;
import frontend.irgenerate.instr.Store;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.Function;
import frontend.irgenerate.irclass.User;
import frontend.irgenerate.irclass.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class Mem2Reg {
    private List<Function> functions;

    public Mem2Reg(List<Function> functions) {
        this.functions = functions;
        run();
    }

    private void run() {
        for (Function function : functions) {
            BasicBlock firstBlock = function.getFirstBlock();
            LinkedList<Instr> instrs = firstBlock.getInstrs();
            for (int i = 0; i < instrs.size(); i++) {
                Instr instr = instrs.get(i);
                if (instr instanceof Alloca && !((Alloca) instr).isArrayAlloca()) {
                    ssaConstruction(instr);
                    i--;
                }
            }
        }
    }

    /**
     * 构造ssa算法, 包括插入phi, 变量重命名, 移除alloca, store, load. 输入指令是一个alloca指令
     */
    private void ssaConstruction(Instr alloca) {
        HashSet<Instr> defInstrs = new HashSet<>();
        HashSet<Instr> useInstrs = new HashSet<>();
        HashSet<BasicBlock> useBBs = new HashSet<>();
        HashSet<BasicBlock> defBBs = new HashSet<>();
        //初始化
        for (User user : alloca.getUsers()) {
            Instr instr = (Instr) user;
            if (instr instanceof Store) {
                defInstrs.add(instr);
                defBBs.add(instr.getBasicBlock());
            } else if (instr instanceof Load) {
                useInstrs.add(instr);
                useBBs.add(instr.getBasicBlock());
            }
        }
        //首先对几种特殊情况判断, 进行剪枝.
        if (useInstrs.isEmpty()) {
            for (Instr defInstr : defInstrs) {
                defInstr.remove();
            }
            alloca.remove();
            return;
        } else if (defBBs.size() == 1) {
            //首先对这个基本块的所有指令遍历
            BasicBlock defBB = defBBs.iterator().next();
            Instr reachDef = null;
            LinkedList<Instr> instrs = defBB.getInstrs();
            for (Instr instr : instrs) {
                if (defInstrs.contains(instr)) {
                    reachDef = instr;
                } else if (useInstrs.contains(instr)) {
                    if (reachDef == null) {
                        instr.replaceAllUsesWith(Constant.ConstantInt.ZERO);
                    } else {
                        instr.replaceAllUsesWith(((Store) reachDef).getValue());
                    }
                }
            }
            //排除未初始化就使用的情况
            Value defValue = null;
            if (reachDef == null) {
                defValue = Constant.ConstantInt.ZERO;
            } else {
                defValue = ((Store) reachDef).getValue();
            }
            for (Instr useInstr : useInstrs) {
                if (!useInstr.getBasicBlock().equals(defBB)) {
                    assert reachDef != null;
                    useInstr.replaceAllUsesWith(defValue);
                }
            }
        }
        //剪枝结束, 插入phi和rename
        else {
            //F为对于该变量, 加入phi指令的basicblock
            HashSet<BasicBlock> F = new HashSet<>();
            //W为包含v定义点的block
            HashSet<BasicBlock> W = new HashSet<>(defBBs);
//            System.out.println("W"+W.size());
            while (!W.isEmpty()) {
                BasicBlock nowBB = null;// nowBB一定是有定义的基本块
                for (BasicBlock basicBlock : W) {
                    nowBB = basicBlock;
                    break;
                }
                W.remove(nowBB);
                for (BasicBlock dfBB : nowBB.getDF()) {
                    if (!F.contains(dfBB)) {
                        F.add(dfBB);
                        if (!defBBs.contains(dfBB)) {
                            W.add(dfBB);
                        }
                    }
                }
            }

            //对于支配边界中的所有基本块, 都要加入phi指令
            for (BasicBlock bb : F) {
//                System.out.println(F.size());
                Instr phi = null;
                List<Value> values = new ArrayList<>();
                for (int i = 0; i < bb.getPrecBBs().size(); i++) {
                    values.add(new Instr());
                }
                //按前驱基本块的顺序, 依次添加
                phi = new Phi(values, bb);
                useInstrs.add(phi);
                defInstrs.add(phi);
            }

            Stack<Value> stack = new Stack<>();
            //避免出现未定义情况
            stack.add(Constant.ConstantInt.ZERO);
            DFSForRename(stack, alloca.getBasicBlock().getFunction().getFirstBlock(), defInstrs, useInstrs);
        }
        alloca.remove();
        for (Instr useInstr : useInstrs) {
            if (!(useInstr instanceof Phi)) {
                useInstr.remove();
            }
        }
        for (Instr defInstr : defInstrs) {
            if (!(defInstr instanceof Phi)) {
                defInstr.remove();
            }
        }
    }

    //对支配图进行DFS遍历, 在基本块X中的所有使用全部替换, 后继基本块中所有phi指令进行更新;
    private void DFSForRename(Stack<Value> defStack, BasicBlock V,
                              HashSet<Instr> defInstrs, HashSet<Instr> useInstrs) {
//        System.out.println(V.getOnlyName());
        int defCnt = 0;
        for (Instr instr : V.getInstrs()) {
            if (defInstrs.contains(instr)) {
                  if (instr instanceof Store) {
                    defStack.add(((Store) instr).getValue());
                    defCnt++;
                } else if (instr instanceof Phi) {
                    defStack.add(instr);
                    defCnt++;
                }
            }
            //load指令
            else if (useInstrs.contains(instr) && !(instr instanceof Phi)) {
                instr.replaceAllUsesWith(defStack.peek());
            }
        }
        //进行后继基本块phi指令的更新
        for (BasicBlock succBB : V.getSuccBBs()) {
            for (Instr instr : succBB.getInstrs()) {
                //只处理phi
                if (!(instr instanceof Phi)) {
                    break;
                }
                if (useInstrs.contains(instr)) {
                    instr.replaceValue(defStack.peek(), succBB.getPrecBBs().indexOf(V));
                }
            }
        }
        for (BasicBlock idomV : V.getIdoms()) {
            DFSForRename(defStack, idomV, defInstrs, useInstrs);
        }
        for (int i = 0; i < defCnt; i++) {
            defStack.pop();
        }
    }

}
