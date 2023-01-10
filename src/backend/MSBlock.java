package backend;

import backend.instr.MSMove;
import backend.instr.MSBinary;
import backend.instr.MSInstr;
import backend.instr.MSJump;
import backend.instr.MSLoad;
import backend.instr.MSSelf;
import backend.instr.MSStore;
import backend.operand.Imm;
import backend.operand.MSLabel;
import backend.operand.MSOperand;
import backend.operand.PhyReg;
import backend.operand.Reg;
import frontend.irgenerate.Type;
import frontend.irgenerate.instr.Alloca;
import frontend.irgenerate.instr.BinaryOperator;
import frontend.irgenerate.instr.Branch;
import frontend.irgenerate.instr.Call;
import frontend.irgenerate.instr.GetElementPtr;
import frontend.irgenerate.instr.Icmp;
import frontend.irgenerate.instr.Instr;
import frontend.irgenerate.instr.Jump;
import frontend.irgenerate.instr.Load;
import frontend.irgenerate.instr.LoadFromStack;
import frontend.irgenerate.instr.Move;
import frontend.irgenerate.instr.Return;
import frontend.irgenerate.instr.Store;
import frontend.irgenerate.instr.Store2Stack;
import frontend.irgenerate.irclass.Argument;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.GlobalValue;
import frontend.irgenerate.irclass.Value;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MSBlock {
    private String lable_name;
    private LinkedList<MSInstr> instrs = new LinkedList<>();
    private MSFunction function;

    /**
     * 创建mips基本块
     * @param basicBlock
     * @param isFirstBlock 如果为true, 则为函数的第一个基本块, 进行分配空间等操作
     * @param msFunction
     */
    public MSBlock(BasicBlock basicBlock, boolean isFirstBlock, MSFunction msFunction) {
        MSModule.msNowBlock = this;
        this.function = msFunction;
        this.lable_name = basicBlock.getOnlyName();
        LinkedList<Instr> irInstrs = basicBlock.getInstrs();
        List<Argument> p_arguments = msFunction.getP_arguments();
        int i = 0, j = 0, size = irInstrs.size(), param_size = p_arguments.size();
        //如果是第一个基本块, 则进行地址的分配.
        if (isFirstBlock) {
            List<Alloca> allocas = new ArrayList<>();
            for (i = 0; i < size && (irInstrs.get(i) instanceof Alloca || irInstrs.get(i) instanceof LoadFromStack); i++) {
                if(irInstrs.get(i) instanceof LoadFromStack)
                    continue;
                allocas.add((Alloca) irInstrs.get(i));
            }
            function.allocInitial(p_arguments, allocas);
        }
        for (i=0; i < size; i++) {
            parseIrInstr(irInstrs.get(i));
        }
    }

    public MSFunction getFunction() {
        return function;
    }

    public LinkedList<MSInstr> getInstrs() {
        return instrs;
    }

    public String getLable_name() {
        return lable_name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lable_name).append(":\n");
        for (MSInstr instr : instrs) {
            sb.append(instr).append("\n");
        }
        return sb.toString();
    }

    public void addInstr(MSInstr instr) {
        instrs.add(instr);
    }

    /**
     * llvm中间代码转后端的主函数
     *
     * @param irinstr
     */
    private void parseIrInstr(Instr irinstr) {
        if (irinstr instanceof Store) {
            parseStore(irinstr);
        } else if (irinstr instanceof Load) {
            parseLoad(irinstr);
        } else if (irinstr instanceof Alloca) {
            return;
        } else if (irinstr instanceof BinaryOperator) {
            parseBinaryOp(irinstr);
        } else if (irinstr instanceof Call) {
            parseCall(irinstr);
        } else if (irinstr instanceof Return) {
            parseReturn(irinstr);
        } else if (irinstr instanceof GetElementPtr) {
            parseGep(irinstr);
        } else if (irinstr instanceof Icmp) {
            parseIcmp(irinstr);
        } else if (irinstr instanceof Branch) {
            parseBranch(irinstr);
        } else if (irinstr instanceof Jump) {
            parseJump(irinstr);
        } else if (irinstr instanceof Move) {
            parseMove(irinstr);
        } else if (irinstr instanceof LoadFromStack){
            parseLoadFromStack(irinstr);
        } else if (irinstr instanceof Store2Stack){
            parseStore2Stack(irinstr);
        }
    }

    private void parseStore2Stack(Instr irInstr){
        Store2Stack s2t = (Store2Stack) irInstr;
        PhyReg reg = PhyReg.getGlobalReg(s2t.getReg());
        Value value = s2t.getValue();
        MSStore msStore = new MSStore(reg, PhyReg.sp, Imm.ZERO);
        msStore.setNeedUpdate(true);
        msStore.setUpdateValue(value);
    }

    private void parseLoadFromStack(Instr irInstr){
        LoadFromStack lfs = (LoadFromStack) irInstr;
        Reg reg = PhyReg.getGlobalReg(lfs.getReg());
        Value value = lfs.getValue();
        MSLoad msLoad = new MSLoad(reg, PhyReg.sp, Imm.ZERO);
        msLoad.setNeedUpdate(true);
        msLoad.setUpdateValue(value);
    }

    private void parseMove(Instr irInstr) {
        Move move = (Move) irInstr;
        Reg dst = function.getReg(move.getDst());
        if (move.getSrc() instanceof Constant.ConstantInt) {
            int num = ((Constant.ConstantInt) move.getSrc()).getNum();
            new MSMove(dst, new Imm(num));
        } else {
            Reg src = function.getReg(move.getSrc());
            new MSMove(dst, src);
        }
    }

    private void parseJump(Instr irInstr) {
        Jump jump = (Jump) irInstr;
        new MSJump("j", new MSLabel(jump.getTarget().getOnlyName()));
    }

    private void parseBranch(Instr irInstr) {
        Branch branch = (Branch) irInstr;
        Reg cond = function.getReg(branch.getOperand(0));
        String trueName = branch.getTrueTarget().getOnlyName();
        String falseName = branch.getFalseTarget().getOnlyName();
        new MSJump("bnez", cond, new MSLabel(trueName));
        new MSJump("j", new MSLabel(falseName));
    }

    private void parseIcmp(Instr irInstr) {
        Icmp icmp = (Icmp) irInstr;
        Reg reg0 = function.getReg(icmp);
        new MSBinary(icmp.getOpcodeName(), reg0, function.getReg(icmp.getOperand(0)),
                function.getReg(icmp.getOperand(1)));
    }

    private void parseReturn(Instr irinstr) {
        if (MSModule.msNowFunc.isMainFunc()) {
            new MSSelf("li $v0, 10\nsyscall");
        } else {
            Return ret = (Return) irinstr;
            if (!ret.getType().equals(Type.VoidType.VOID_TYPE)) {
                Value retValue = ret.getOperand(0);
                new MSMove(PhyReg.v0, function.getReg(retValue));
            }
            new MSBinary("add", PhyReg.sp, PhyReg.sp, new Imm(function.getStackTop()));
            new MSJump("jr", PhyReg.ra);
        }
    }

    /**
     * 专门处理外部函数的调用
     */
    private void parseExternalCall(Call call) {
        String name = call.getFuncName();
        List<Value> params = call.getOperands();
        if (name.equals("getint")) {
            new MSSelf("li $v0, 5\nsyscall\n");
            new MSMove(function.getReg(call), PhyReg.v0);
            return;
        } else if (name.equals("putch")) {
            new MSMove(PhyReg.a0, new Imm(Integer.parseInt(params.get(0).getFullName())));
            new MSSelf("li $v0, 11\nsyscall\n");
            return;
        } else if (name.equals("putstr")) {
            new MSMove(PhyReg.a0, function.getReg(call.getOperand(0)));
            new MSSelf("li $v0, 4\nsyscall");
            return;
        } else if (name.equals("putint")) {
            if (params.get(0) instanceof Constant.ConstantInt) {
                new MSMove(PhyReg.a0, new Imm(Integer.parseInt(params.get(0).getFullName())));
            } else {
                new MSMove(PhyReg.a0, function.getReg(params.get(0)));
            }
            new MSSelf("li $v0, 1\nsyscall\n");
            return;
        }
    }

    /**
     * 在调用函数前, sp需要移动虚拟寄存器顶, 之后保存返回值$ra和下一个函数的参数之后, sp再移到此时的栈顶;
     * 函数返回后, sp需要将栈顶的$ra保存回$ra中, 并移动到虚拟寄存器区和数组区的交界处.
     *
     * @param irinstr
     */
    private void parseCall(Instr irinstr) {
        Call call = (Call) irinstr;
        String name = call.getFuncName();
        List<Value> params = call.getOperands();
        if (call.isExternal()) {
            parseExternalCall(call);
            return;
        }
        name = "func_" + name;
        //将栈顶移到虚拟寄存器区顶部
        function.spMoveBeforeCall(params.size() * 4 + 4);
//        function.moveSp(params.size() * 4 + 4);
        //保存参数
        int i;
        for (i = 0; i < params.size(); i++) {
            Value param = params.get(i);
            if (param instanceof Constant.ConstantInt) {
                Reg reg = function.allocVirReg(true);
                new MSMove(reg, new Imm(Integer.parseInt(param.getFullName())));
                new MSStore(reg, PhyReg.sp, new Imm(4 * i));
            } else {
                //TODO: 这里有问题, 栈已经移动了. 参数不会存在溢出区里
//                Reg param_reg = function.getReg(param);
//                new MSLoad()
                new MSStore(function.getReg(param), PhyReg.sp, new Imm(4 * i));
            }
        }
        new MSStore(PhyReg.ra, PhyReg.sp, new Imm(4 * i));
        new MSJump("jal", new MSLabel(name));
//        new MSSelf("nop");
        //恢复返回值
        new MSLoad(PhyReg.ra, PhyReg.sp, new Imm(0));
        function.moveSp(-4);
        function.restore2VirtualDown();
        if (!call.getType().equals(Type.VoidType.VOID_TYPE)) {
            new MSMove(function.getReg(call), PhyReg.v0);
        }
    }

    private void parseBinaryOp(Instr irinstr) {
        BinaryOperator lvop = (BinaryOperator) irinstr;
        Value op1 = lvop.getOperand(0);
        Value op2 = lvop.getOperand(1);
        Reg reg1 = function.getReg(op1);
        MSOperand reg2;
        if(op2 instanceof Constant.ConstantInt){
            reg2 = new Imm(((Constant.ConstantInt) op2).getNum());
        }else {
            reg2 = function.getReg(op2);
        }
        new MSBinary(lvop.getOpcodeName(), function.getReg(lvop), reg1, reg2);
    }

    private void parseLoad(Instr irinstr) {
        Load load = (Load) irinstr;
        Value op1 = load.getOperand(0);
        //如果是全局变量, 不是数组
        if (op1 instanceof GlobalValue) {
            new MSLoad(function.getReg(load), PhyReg.zero, new MSLabel(op1.getOnlyName()));
        } else if (op1 instanceof GetElementPtr) {
            new MSLoad(function.getReg(load), function.getReg(op1), Imm.ZERO);
        }
    }

    /**
     * 获取相对数组基址的offset字节数
     */
    public boolean getOffset(GetElementPtr gep, Reg offReg) {
        Type innerType = ((Type.PointerType) gep.getOperand(0).getType()).getInnerType();
        int offset = 0, index;
        List<Value> operands = gep.getOperands();
        boolean flag = false;//flag为true, 表示为offReg赋了初值
        boolean first = true;
        for (int i = 1; i < operands.size(); i++) {
            if (operands.get(i) instanceof Constant.ConstantInt) {
                index = Integer.parseInt(operands.get(i).getFullName());
                offset += index * innerType.getSize();
            } else {
                flag = true;//如果offset为0则不需要补语句
                Reg index_reg = function.getReg(operands.get(i));
                Reg tmp_reg = function.allocVirReg(true);
                new MSBinary("mul", tmp_reg, index_reg,
                        new Imm(4 * innerType.getSize()));
                if (first) {
                    new MSBinary("add", offReg, tmp_reg, Imm.ZERO);
                    first = false;
                } else {
                    new MSBinary("add", offReg, offReg, tmp_reg);
                }
            }
            innerType = innerType.getInnerType();
        }
        if (offset != 0) {
            flag = true;
            if(first)
                new MSMove(offReg, new Imm(offset * 4));
            else
                new MSBinary("add", offReg, offReg, new Imm(offset*4));
        }
        return flag;
    }

    /**
     * 需要为gep出来的地址分配一个虚拟寄存器, 来保存数组对应元素的地址
     *
     * @param instr
     */
    private void parseGep(Instr instr) {
        GetElementPtr gep = (GetElementPtr) instr;
        Reg addr = function.getReg(instr);
        boolean needInit = !getOffset(gep, addr);//是否需要为addr初始化
        Value arr = gep.getOperand(0);
        //全局数组
        if (arr instanceof GlobalValue) {
            if (needInit) {
                new MSMove(addr, Imm.ZERO);
                new MSMove(addr, new MSLabel(arr.getOnlyName()), null);
            }
            else {
                new MSMove(addr, new MSLabel(arr.getOnlyName()), addr);
            }
        }
        //局部数组
        else if (arr instanceof Alloca) {
            int off2Stack = function.getOff(arr);
            if (off2Stack == 0 && needInit) {//偏移量为0
                new MSBinary("add", addr, PhyReg.sp, Imm.ZERO);
            } else {
                if (off2Stack != 0 && needInit) {//数组基址在栈上有偏移量
                    new MSBinary("add", addr, PhyReg.sp, new Imm(off2Stack));
                } else if (off2Stack == 0 && !needInit) {//数组有偏移
                    new MSBinary("add", addr, PhyReg.sp, addr);
                } else {//数组基址和数组都有偏移
                    new MSBinary("add", addr, addr, new Imm(off2Stack));
                    new MSBinary("add", addr, PhyReg.sp, addr);
                }
            }
        }
        //参数, 获取参数的虚拟寄存器, 作为数组基址.
        else if (arr instanceof Argument || arr instanceof GetElementPtr) {
            if(needInit)
                new MSBinary("add", addr, function.getReg(arr), Imm.ZERO);
            else
                new MSBinary("add", addr, function.getReg(arr), addr);
        }
        else {
            assert false;
        }
//        function.addValue2Reg(gep, addr);
    }

    private void parseStore(Instr irinstr) {
        Store store = (Store) irinstr;
        Value op1 = store.getOperand(0);
        Value op2 = store.getOperand(1);
        //分配虚拟寄存器
        Reg reg = function.getReg(op1);
        //只会存两个, 第一: 全局变量; 第二, gep后的指令.
        if (op2 instanceof GlobalValue) {
            new MSStore(reg, PhyReg.zero, new MSLabel(op2.getOnlyName()));
        } else {
            new MSStore(reg, function.getReg(op2), Imm.ZERO);
        }
    }
}
