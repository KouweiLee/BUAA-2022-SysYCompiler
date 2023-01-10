package backend;

import backend.instr.MSBinary;
import backend.instr.MSLoad;
import backend.instr.MSMove;
import backend.operand.Imm;
import backend.operand.PhyReg;
import backend.operand.Reg;
import backend.operand.VirReg;
import frontend.irgenerate.instr.Alloca;
import frontend.irgenerate.irclass.Argument;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.Function;
import frontend.irgenerate.irclass.Value;

import java.awt.print.PrinterJob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class MSFunction {
    private String name;
    private ArrayList<MSBlock> blocks;
    //存储value距离当前函数栈底的距离, 包括参数等.
    // 参数距离当前函数栈底的距离是负数, 因为是由调用函数方保存的;
    //其他变量距离栈底的距离是正数
    /**
     * 存储数组value距离当前函数栈底的距离
     */
    private HashMap<Value, Integer> value2Stack;
    //存储value和虚拟寄存器的映射关系
    private HashMap<Value, Reg> value2Reg = new HashMap<>();

    //存储value和reg的映射关系
//    private HashMap<Value, PhyReg> value2Reg;
//    private HashMap<PhyReg, Value> reg2Value;
    //只用于从寄存器池分配寄存器
    private HashSet<Integer> usedRegs;
    private List<Argument> p_arguments;

    //全部统一, 记录离栈底的距离, 均为正数; 要求sp进行偏移的量, 如果是增大栈, 那么是正数; 减小栈, 则是负数
    //注意, 函数初始化之后, stackTop将是一个定值, 指向数组区和溢出区之间的位置
    private int stackTop = 0;
    private boolean isMainFunc = false;
    //虚拟寄存器所占空间的大小字节
    private List<VirReg> virRegs = new ArrayList<>();
    //记录虚拟寄存器距离栈底的距离
    private HashMap<VirReg, Integer> vir2Stack = new HashMap<>();
    //记录虚拟寄存器区的大小字节, 不包括参数的虚拟寄存器
    private Imm virSize = new Imm(0);
    private HashMap<Value, Integer> globalRegsMap;
    private HashMap<VirReg, PhyReg> vir2Phy = new HashMap<>();

    public PhyReg getGlobalReg(Reg virReg) {
        return vir2Phy.get(virReg);
    }

    public ArrayList<MSBlock> getBlocks() {
        return blocks;
    }

    /**
     * 溢出区是否包括虚拟寄存器, 判断虚拟寄存器是否需要保存到栈上
     */
    public boolean includeVirReg(VirReg reg) {
        return vir2Stack.containsKey(reg);
    }

    public int getVirSize() {
        return virSize.getNum();
    }

    /**
     * 将虚拟寄存器映射到栈上, 作为溢出区; 同时设定溢出区的大小字节
     */
    public void setVirSize() {
        int i;
        //参数区的溢出区就保存在参数区
        for (i = 0; i < p_arguments.size(); i++) {
            vir2Stack.put(virRegs.get(i), (p_arguments.size() - i) * 4);
        }
        for (int j = 0; i < virRegs.size(); j++, i++) {
            vir2Stack.put(virRegs.get(i), stackTop + (j + 1) * 4);
        }
        virSize.setNum((virRegs.size() - p_arguments.size()) * 4);
    }

    private Reg allocVirReg(Value value) {
        VirReg reg = new VirReg();
        value2Reg.put(value, reg);
        virRegs.add(reg);
        if(globalRegsMap.get(value) != null)
            vir2Phy.put(reg, PhyReg.getGlobalReg(globalRegsMap.get(value)));
        return reg;
    }

    /**
     * 如果该虚拟寄存器对应一个只会使用一次的变量, 如常量等, 则不为其分配栈空间
     *
     * @param isUseOnlyOnce
     * @return
     */
    public Reg allocVirReg(boolean isUseOnlyOnce) {
        VirReg virReg = new VirReg();
        if (!isUseOnlyOnce) {
            virRegs.add(virReg);
        }
        return virReg;
    }

//    public void addValue2Reg(Value value, Reg reg) {
//        value2Reg.put(value, reg);
//    }

    /**
     * 为数组分配地址空间, allocas数组依次从栈底到栈顶(从上到下)排布, 数组元素是从上到下一次排布.
     * 进入函数时, 参数区已经在栈底了, 因此更新stackTop. 为参数分配虚拟寄存器, 为数组分配空间
     *
     * @param allocas
     */
    public void allocInitial(List<Argument> params, List<Alloca> allocas) {
        stackTop += params.size() * 4;
        for (int i = 0; i < params.size(); i++) {
            //参数区是从下到上依次排布参数
//            new MSLoad(allocVirReg(p_arguments.get(i)), PhyReg.sp, new Imm(i * 4));
            allocVirReg(p_arguments.get(i));
        }
        int alloc_size = 0;
        for (Alloca alloca : allocas) {
            alloc_size += alloca.getSize();
        }

        for (int i = 0, tmp = 0; i < allocas.size(); i++) {
            tmp += 4 * allocas.get(i).getSize();
            value2Stack.put(allocas.get(i), stackTop + tmp);
        }
        moveSp(4 * alloc_size);
        stackTop += 4 * alloc_size;
    }

    /**
     * 连同stackTop一起移动栈指针. 增大栈的空间, 则是正数, 减小栈的空间, 则是负数.
     *
     * @param off
     */
    public void moveSp(int off) {
        if (off == 0) {
            return;
        }
        new MSBinary("add", PhyReg.sp, PhyReg.sp, new Imm(-off));
    }

    public void spMoveBeforeCall(int off) {
        new MSBinary("sub", PhyReg.sp, PhyReg.sp, new Imm(virSize, off)).setBeforeCall();
//        new MSBinary("sub", PhyReg.sp, PhyReg.sp, new Imm(virSize, off)).setBeforeCall();
//        new MSBinary("sub", PhyReg.sp, PhyReg.sp, new Imm(off)).setBeforeCall();
    }

    /**
     * 将栈指针还原回虚拟寄存器区底部(离栈底近的是底部)
     */
    public void restore2VirtualDown() {
        new MSBinary("add", PhyReg.sp, PhyReg.sp, virSize);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\n");
        for (MSBlock block : blocks) {
            sb.append(block.toString());
        }
        return sb.toString();
    }

    /**
     * @param function llvmFunction
     */
    public MSFunction(Function function, boolean isMainFunc) {
        init();
        MSModule.msNowFunc = this;
        this.isMainFunc = isMainFunc;
        this.globalRegsMap = function.getGlobalRegsMap();
        name = "func_" + function.getOnlyName();
        p_arguments = function.getArguments();
        //不为函数参数分配空间, 但是参数区是在函数栈的栈底
//        stackTop = p_arguments.size() * 4;
        LinkedList<BasicBlock> basicBlocks = function.getBasicBlocks();
        blocks.add(new MSBlock(basicBlocks.get(0), true, this));
        for (int i = 1; i < basicBlocks.size(); i++) {
            blocks.add(new MSBlock(basicBlocks.get(i), false, this));
        }
        for (int i = 1; i < basicBlocks.size(); i++) {
            MSModule.lvBB2MSBB.put(basicBlocks.get(i), blocks.get(i));
        }
        //TODO: 设置虚拟寄存器的存储区
        setVirSize();
    }

    public int getStackTop() {
        return stackTop;
    }

    public boolean isMainFunc() {
        return isMainFunc;
    }

    /**
     * 获取value对应的寄存器, 如果是常量, 那么分配一个寄存器
     *
     * @param value
     * @return
     */
    public Reg getReg(Value value) {
        if (value instanceof Constant.ConstantInt) {
            Reg reg = allocVirReg(true);
            new MSMove(reg, new Imm(Integer.parseInt(value.getFullName())));
            return reg;
        }
        Reg reg = value2Reg.get(value);
        if (reg == null) {
            reg = allocVirReg(value);
        }
        return reg;
    }

    public List<Argument> getP_arguments() {
        return p_arguments;
    }

    /**
     * 获取value距离栈顶的距离, 之后可以通过add, $sp, $sp, off来定位到该value
     *
     * @param value
     * @return
     */
    public int getOff(Value value) {
        int dis = value2Stack.get(value);
        return stackTop - dis;
    }

    /**
     * 获取虚拟寄存器对应栈顶的位置, 届时用add定位
     *
     * @param virReg
     * @return
     */
    public int getOff(VirReg virReg) {
        int dis = vir2Stack.get(virReg);
        return stackTop - dis;
    }

    /**
     * 如果为true, 则将stackTop认为在已经分配好的参数区下面. 用于调用函数时, 保存寄存器
     */
    public int getOff(VirReg virReg, int off2Sp) {
        int dis = vir2Stack.get(virReg);
        return stackTop - dis + off2Sp;
    }

    public int getOff(Value value, int off2Sp) {
        Reg reg = value2Reg.get(value);
        return getOff((VirReg) reg, off2Sp);
    }

    /**
     * 返回距离栈顶的距离
     *
     * @param dis 距离栈底的距离
     * @return
     */
    public int getOff(int dis) {
        return stackTop - dis;
    }

    private void init() {
        blocks = new ArrayList<>();
        value2Reg = new HashMap<>();
        value2Stack = new HashMap<>();
        usedRegs = new HashSet<>();
    }
}
