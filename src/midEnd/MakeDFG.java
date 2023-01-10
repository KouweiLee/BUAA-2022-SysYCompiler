package midEnd;

import frontend.irgenerate.instr.Branch;
import frontend.irgenerate.instr.Instr;
import frontend.irgenerate.instr.Jump;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * 进行支配图的分析
 */
public class MakeDFG {
    private List<Function> functions;

    public MakeDFG(List<Function> functions) {
        this.functions = functions;
    }

    public void run() {
        removeDeadBB();
        makeCFG();
        makeDoms();
        makeIDoms();
        makeDF();
    }

    //移除死代码块, 从函数开始出发到达不了的基本块
    private void removeDeadBB() {
        for (Function function : functions) {
            removeFuncDeadBB(function);
        }
    }

    private void removeFuncDeadBB(Function function) {
        HashMap<BasicBlock, List<BasicBlock>> preMap = new HashMap<>();
        HashMap<BasicBlock, List<BasicBlock>> succMap = new HashMap<>();
        findPreAndSucc(function, preMap, succMap);
        BasicBlock firstBlock = function.getFirstBlock();
        HashSet<BasicBlock> records = new HashSet<>();
        dfsForDeadBBSearch(firstBlock, records, succMap);
        LinkedList<BasicBlock> basicBlocks = function.getBasicBlocks();
        for (int i = 0; i < basicBlocks.size(); i++) {
            if (!records.contains(basicBlocks.get(i))) {
                BasicBlock needRemove = basicBlocks.get(i);
                needRemove.remove();
                basicBlocks.remove(i);
                i--;
            }
        }
    }

    private void dfsForDeadBBSearch(BasicBlock first, HashSet<BasicBlock> records,
                                    HashMap<BasicBlock, List<BasicBlock>> succMap) {
        if (records.contains(first)) {
            return;
        }
        records.add(first);
        for (BasicBlock succ : succMap.get(first)) {
            dfsForDeadBBSearch(succ, records, succMap);
        }
    }

    private void makeDF() {
        for (Function function : functions) {
            makeFuncDF(function);
        }
    }

    private void makeFuncDF(Function function) {
        LinkedList<BasicBlock> basicBlocks = function.getBasicBlocks();
        for (BasicBlock bb : basicBlocks) {
            HashSet<BasicBlock> DF = new HashSet<>();
            for (BasicBlock bb2 : basicBlocks) {
                if (isDF(bb, bb2)) {
                    DF.add(bb2);
                }
            }
            bb.setDF(DF);
        }
    }

    /**
     * 判断基本块n的支配边界, 是否包含x
     */
    private boolean isDF(BasicBlock n, BasicBlock x) {
        // n支配x的前驱节点, 但n不严格支配x. 不严格支配的意思是: n要么是x. 要么不支配x
        //如果n严格支配x, 则返回false
        if (!(n.equals(x) || !n.getDoms().contains(x))) {
            return false;
        }
        //n支配x的一个前驱节点即可返回真
        for (BasicBlock precBB : x.getPrecBBs()) {
            if (n.getDoms().contains(precBB)) {
                return true;
            }
        }
        return false;
    }

    private void makeIDoms() {
        for (Function function : functions) {
            makeFuncIDoms(function);
        }
    }

    private void makeFuncIDoms(Function function) {
        LinkedList<BasicBlock> basicBlocks = function.getBasicBlocks();
        for (BasicBlock bb : basicBlocks) {
            HashSet<BasicBlock> idoms = new HashSet<>();
            for (BasicBlock domee : bb.getDoms()) {
                if (isIDom(bb, domee)) {
                    idoms.add(domee);
                }
            }
            bb.setIdoms(idoms);
        }
    }

    /**
     * 判断基本块A是否直接支配B.
     * 如果A 直接支配 B, 那么A首先严格支配B : A支配B且A不等于B
     * , 并且不严格支配任何严格支配n的节点的节点: A支配的所有基本块, 其中不能有严格支配B的
     *
     * @param A
     * @param B
     */
    private boolean isIDom(BasicBlock A, BasicBlock B) {
        if (!A.getDoms().contains(B)) {
            return false;
        }
        if (A.equals(B)) {
            return false;
        }
        for (BasicBlock domee : A.getDoms()) {
            if (!domee.equals(A) && !domee.equals(B) && domee.getDoms().contains(B)) {
                return false;
            }
        }
        return true;
    }

    private void makeDoms() {
        for (Function function : functions) {
            makeFuncDoms(function);
        }
    }

    /**
     * 得到函数内所有基本块之间的支配关系
     */
    private void makeFuncDoms(Function function) {
        LinkedList<BasicBlock> basicBlocks = function.getBasicBlocks();
        BasicBlock firstBlock = function.getFirstBlock();
        for (BasicBlock bb : basicBlocks) {
            HashSet<BasicBlock> records = new HashSet<>();
            HashSet<BasicBlock> doms = new HashSet<>();
            dfs(firstBlock, bb, records);
            for (BasicBlock block : basicBlocks) {
                if (!records.contains(block)) {
                    doms.add(block);
                }
            }
            //设定bb支配的blocks集合.
//            System.out.println(doms.size());
            bb.setDoms(doms);
        }
    }

    /**
     * 从first出发, 不经过exclude基本块, 进行dfs. records记录已经搜索到了哪些节点.
     */
    private void dfs(BasicBlock first, BasicBlock exclude, HashSet<BasicBlock> records) {
        if (first.equals(exclude)) {
            return;
        }
//        if(records.contains(first)){
//            return;
//        }
        records.add(first);
        for (BasicBlock succBB : first.getSuccBBs()) {
            if (!succBB.equals(exclude) && !records.contains(succBB)) {
                dfs(succBB, exclude, records);
            }
        }

    }

    private void makeCFG() {
        for (Function function : functions) {
            makeFuncCFG(function);
        }
    }

    private void findPreAndSucc(Function function, HashMap<BasicBlock, List<BasicBlock>> preMap, HashMap<BasicBlock, List<BasicBlock>> succMap) {
        //初始化
        for (BasicBlock bb : function.getBasicBlocks()) {
            preMap.put(bb, new ArrayList<>());
            succMap.put(bb, new ArrayList<>());
        }
        //寻找前驱和后继
        for (BasicBlock bb : function.getBasicBlocks()) {
            Instr lastInstr = bb.getLastInstr();
            if (lastInstr instanceof Branch) {
                BasicBlock trueTarget = ((Branch) lastInstr).getTrueTarget();
                BasicBlock falseTarget = ((Branch) lastInstr).getFalseTarget();
                succMap.get(bb).add(trueTarget);
                succMap.get(bb).add(falseTarget);
                preMap.get(trueTarget).add(bb);
                preMap.get(falseTarget).add(bb);
            } else if (lastInstr instanceof Jump) {
                BasicBlock target = ((Jump) lastInstr).getTarget();
                succMap.get(bb).add(target);
                preMap.get(target).add(bb);
            }
        }
    }

    /**
     * 为一个函数分析控制流图CFG
     *
     * @param function
     */
    private void makeFuncCFG(Function function) {
        HashMap<BasicBlock, List<BasicBlock>> preMap = new HashMap<>();
        HashMap<BasicBlock, List<BasicBlock>> succMap = new HashMap<>();
        findPreAndSucc(function, preMap, succMap);
        //将数据流信息写入基本块中
        for (BasicBlock bb : function.getBasicBlocks()) {
            bb.setPrecBBs(preMap.get(bb));
            bb.setSuccBBs(succMap.get(bb));
        }
        //TODO: 是否需要写入函数
    }
}
