package midEnd;

import frontend.irgenerate.instr.Branch;
import frontend.irgenerate.instr.Instr;
import frontend.irgenerate.instr.Jump;
import frontend.irgenerate.instr.Move;
import frontend.irgenerate.instr.Pcopy;
import frontend.irgenerate.instr.Phi;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Function;
import frontend.irgenerate.irclass.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class RemovePhi {
    private List<Function> functions;

    public RemovePhi(List<Function> functions) {
        this.functions = functions;
        run();
    }

    public void run() {
        removePhiAddPcopy();
        replacePcopyWithMove();
    }

    /**
     * 用move指令将所有的pcopy指令进行替换
     */
    private void replacePcopyWithMove() {
        for (Function function : functions) {
            replacePcopyWithMoveInFunc(function);
        }
    }

    private void replacePcopyWithMoveInFunc(Function function) {
        for (BasicBlock bb : function.getBasicBlocks()) {
            List<Pcopy> pcopies = new ArrayList<>();
//            List<Move> moves = new ArrayList<>();
            LinkedList<Instr> instrs = bb.getInstrs();
            for (int k = 0; k < instrs.size(); k++) {
                Instr instr = instrs.get(k);
                if (!(instr instanceof Pcopy)) {
                    continue;
                }
                Pcopy pcopy = (Pcopy) instr;
                pcopies.add(pcopy);
                List<Value> dsts = pcopy.getLeftOps();
                List<Value> srcs = pcopy.getRightOps();
//                HashSet<Value> dstSet = new HashSet<>(dsts);
                HashSet<Value> srcSet = new HashSet<>(srcs);
                while (!pcopy.isUseless()) {
                    boolean isHaveSecure = false;
                    for (int i = 0; i < dsts.size(); i++) {
                        //如果move是安全的
                        if (!srcSet.contains(dsts.get(i))) {
                            Move move = new Move(dsts.get(i), srcs.get(i), bb);
                            bb.insertBeforeEnd(move);
//                            dstSet.remove(dsts.get(i));
                            srcSet.remove(srcs.get(i));
                            dsts.remove(i);
                            srcs.remove(i);
                            isHaveSecure = true;
                            break;
                        }
                    }
                    //如果全部是不安全的
                    if (!isHaveSecure) {
                        for (int i = 0; i < dsts.size(); i++) {
                            if (dsts.get(i).equals(srcs.get(i))) {
                                continue;
                            }
                            Value.VirtualValue virtual = new Value.VirtualValue(dsts.get(i).getType());
                            Move move = new Move(virtual, srcs.get(i), bb);
                            bb.insertBeforeEnd(move);
                            srcSet.remove(srcs.get(i));
//                            srcSet.add(virtual);
                            srcs.set(i, virtual);
                        }
                    }
                }
            }
            for (Pcopy pcopy : pcopies) {
                pcopy.remove();
            }
        }
    }

    /**
     * 消除phi, 加入pcopy命令
     */
    private void removePhiAddPcopy() {
        for (Function function : functions) {
            removePhiAddPcopyInFunc(function);
        }
    }

    private void removePhiAddPcopyInFunc(Function function) {
        LinkedList<BasicBlock> bbs = function.getBasicBlocks();
        int size = bbs.size();
        for (int i = 0; i < size; i++) {
            BasicBlock bb = bbs.get(i);
            LinkedList<Instr> instrs1 = bb.getInstrs();
            boolean flag = false;
            for (Instr instr : instrs1) {
                if (instr instanceof Phi) {
                    flag = true;
                    break;
                }
            }
            if (!flag)
                continue;
            //按前驱基本块的顺序存储pcopy
            List<Pcopy> pcopys = new ArrayList<>();
            List<BasicBlock> precs = new ArrayList<>(bb.getPrecBBs());
            for (int j = 0; j < precs.size(); j++) {
                if (precs.get(j).getSuccBBs().size() == 1) {
                    Pcopy pcopy = new Pcopy(precs.get(j));
                    precs.get(j).insertBeforeEnd(pcopy);
                    pcopys.add(pcopy);
                } else {// 有多个后继, 为了防止插入较多,降低效率, 因此有针对性地插入
                    BasicBlock mid = new BasicBlock(function);
                    Pcopy pcopy = new Pcopy(mid);
                    pcopys.add(pcopy);
                    addMidBlock(precs.get(j), mid, bb);
                }
            }
            //初始化pcopy之后, 就要对这些 pcopy进行真正地赋值
            LinkedList<Instr> instrs = bb.getInstrs();
            int index = 0;
            while (index < instrs.size()) {
                Instr instr = instrs.get(index);
                if (!(instr instanceof Phi)) {
                    index++;
                    continue;
                }
                List<Value> phiRVs = instr.getOperands();
                for (int j = 0; j < phiRVs.size(); j++) {
                    pcopys.get(j).addOps(instr, phiRVs.get(j));
                }
                //删除Phi指令
                instrs.remove(index);
            }
        }
    }

    /**
     * 向前驱基本块prec和succ之间插入mid基本块
     *
     * @param prec
     * @param mid
     * @param succ
     */
    private void addMidBlock(BasicBlock prec, BasicBlock mid, BasicBlock succ) {
        prec.getSuccBBs().add(mid);
        prec.getSuccBBs().remove(succ);
        mid.getPrecBBs().add(prec);
        mid.getSuccBBs().add(succ);
        succ.getPrecBBs().remove(prec);
        succ.getPrecBBs().add(mid);
        //接下来对prec进行处理
        Instr lastInstr = prec.getLastInstr();
        assert lastInstr instanceof Branch;
        Branch branch = (Branch) lastInstr;
        BasicBlock trueTarget = branch.getTrueTarget();
        BasicBlock falseTarget = branch.getFalseTarget();
        if (trueTarget.equals(succ)) {
            branch.setTrueTarget(mid);
        } else if (falseTarget.equals(succ)) {
            branch.setFalseTarget(mid);
        } else {
            assert false;
        }
        new Jump(succ, mid);
    }

}
