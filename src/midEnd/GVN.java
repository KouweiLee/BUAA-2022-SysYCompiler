package midEnd;

import frontend.irgenerate.instr.BinaryOperator;
import frontend.irgenerate.instr.GetElementPtr;
import frontend.irgenerate.instr.Icmp;
import frontend.irgenerate.instr.Instr;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Function;
import frontend.irgenerate.irclass.Value;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class GVN {
    private List<Function> functions;
    /**
     * 存储Instr对应的hash值以及instr的对应关系, 用于全局公共表达式的替换.
     */
    private HashMap<String, Instr> gvnMap = new HashMap<>();

    public GVN(List<Function> functions) {
        this.functions = functions;
        run();
    }

    public void run() {
        for (Function function : functions) {
            LinkedList<BasicBlock> basicBlocks = function.getBasicBlocks();
            gvnForBlock(basicBlocks.get(0));
        }
    }

    /**
     * 对单个基本块进行gvn
     *
     * @param basicBlock
     */
    private void gvnForBlock(BasicBlock basicBlock) {
        LinkedList<Instr> instrs = basicBlock.getInstrs();
        //保存赋值的定义点.
        HashSet<Instr> defSet = new HashSet<>();
        for (int i = 0; i < instrs.size(); i++) {
            Instr instr = instrs.get(i);
            //如果为赋值指令
            if (instr instanceof GetElementPtr || instr instanceof Icmp || instr instanceof BinaryOperator) {
                if (instrGetGvn(instr)) {
//                    i--;
                } else {
                    defSet.add(instr);
                }
            }
        }

        for (BasicBlock idom : basicBlock.getIdoms()) {
            gvnForBlock(idom);
        }
        for (Instr instr : defSet) {
            removeGvnMap(getHash(instr));
        }
    }

    private void removeGvnMap(String hash) {
        gvnMap.remove(hash);
    }

    /**
     * 对指令进行gvn观察, 其实是在做公共子表达式删除
     *
     * @param instr
     * @return 如果该指令可以被消除, 那么返回true.
     */
    private boolean instrGetGvn(Instr instr) {
        String hash = getHash(instr);
        if (gvnMap.containsKey(hash)) {
            instr.replaceAllUsesWith(gvnMap.get(hash));
            instr.remove();
            return true;
        } else {
            addGvnMap(hash, instr);
            return false;
        }
    }

    private String getHash(Instr instr) {
        String hash = "";
        if (instr instanceof Icmp) {
            if (instr.getOpcode().equals(Instr.Opcode.EQ) || instr.getOpcode().equals(Instr.Opcode.NE)) {
                if (instr.getOperand(0).getFullName().compareTo(instr.getOperand(1).getFullName()) > 0) {
                    hash = instr.getOpcodeName() + " " + instr.getOperand(0).getFullName() + " " +
                            instr.getOperand(1).getFullName();
                } else {
                    hash = instr.getOpcodeName() + " " + instr.getOperand(1).getFullName() + " " +
                            instr.getOperand(0).getFullName();
                }
            } else {
                hash = instr.getOpcodeName() + " " + instr.getOperand(0).getFullName() + " " +
                        instr.getOperand(1).getFullName();
            }
        } else if (instr instanceof GetElementPtr) {
            for (Value operand : instr.getOperands()) {
                hash = hash + operand.getFullName() + " ";
            }
        } else if (instr instanceof BinaryOperator) {
            if (instr.getOpcode().equals(Instr.Opcode.ADD) || instr.getOpcode().equals(Instr.Opcode.MUL)) {
                if (instr.getOperand(0).getFullName().compareTo(instr.getOperand(1).getFullName()) > 0) {
                    hash = instr.getOpcodeName() + " " + instr.getOperand(0).getFullName() + " " +
                            instr.getOperand(1).getFullName();
                } else {
                    hash = instr.getOpcodeName() + " " + instr.getOperand(1).getFullName() + " " +
                            instr.getOperand(0).getFullName();
                }
            } else {
                hash = instr.getOpcodeName() + " " + instr.getOperand(0).getFullName() + " " +
                        instr.getOperand(1).getFullName();
            }
        }
        return hash;
    }

    private void addGvnMap(String hash, Instr instr) {
        gvnMap.put(hash, instr);
    }
}
