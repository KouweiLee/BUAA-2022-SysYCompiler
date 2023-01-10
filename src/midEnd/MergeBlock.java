package midEnd;

import frontend.irgenerate.instr.Branch;
import frontend.irgenerate.instr.Instr;
import frontend.irgenerate.instr.Jump;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Function;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MergeBlock {
    private List<Function> functions;

    public MergeBlock(List<Function> functions) {
        this.functions = functions;
        run();
    }

    /**
     * 合并多余基本块
     */
    public void run() {
        for (Function function : functions) {
            LinkedList<BasicBlock> bbs = function.getBasicBlocks();
            List<BasicBlock> removes = new ArrayList<>();
            //遍历函数的每个基本块, 如果基本块只有一条指令, 且这个指令是无条件跳转, 且不是第一个基本块, 则可以移除.
            for (BasicBlock bb : bbs) {
                if (bb.getInstrs().size() == 1 && bb.getLastInstr() instanceof Jump && !bb.equals(bbs.getFirst())) {
                    removes.add(bb);
                }
            }
            //遍历所有要移除的基本块
            for (BasicBlock remove : removes) {
                Jump jump = (Jump) remove.getLastInstr();
                BasicBlock remove_target = jump.getTarget();
                //找到要移除的基本块的所有前驱
                for (BasicBlock remove_pre : remove.getPrecBBs()) {
                    Instr instr = remove_pre.getLastInstr();
                    //把前驱的跳转指令从bb修改到target
                    instr.replaceValue(remove_target, remove);
                    //target的前驱加入pre
                    remove_target.getPrecBBs().add(remove_pre);
                }
                remove_target.getPrecBBs().remove(remove);
                //移除基本块
                remove.remove();
                bbs.remove(remove);
            }
        }
    }
}
