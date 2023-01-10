package frontend.preprocess.synnodes;

import frontend.irgenerate.IrModule;
import frontend.irgenerate.instr.BinaryOperator;
import frontend.irgenerate.instr.Call;
import frontend.irgenerate.instr.Icmp;
import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.Function;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Def;
import frontend.preprocess.FuncSymbol;
import frontend.preprocess.ESymbolTable;

import java.util.List;
import static frontend.irgenerate.instr.Zext.trimToI32;

public class UnaryExpNode extends Token {
    private int dim = 0;
    private int type;

    public UnaryExpNode() {
    }

    public int getDim() {
        return dim;
    }

    public Value visit() {
        if (type == 0) {//primaryExp
            return trimToI32(getSon(0).visit());
        } else if (type == 1) {//调用函数
            String name = getSon(0).getContent();
            List<Value> arguments = null;
            if (getSon(2) instanceof FuncRParamsNode) {
                arguments = ((FuncRParamsNode) getSon(2)).getArguments();
            }
            Function function = IrModule.module.getFunction(name);
            return new Call(function, arguments);
        } else {
            UnaryExpNode unaryExpNode = (UnaryExpNode) getSon(-1);
            Value value = unaryExpNode.visit();
            boolean neg = false;//负号
            boolean not = false;//取反
            int size = sons.size();
            for (int i = 0; i < size - 1; i++) {
                if (sons.get(i).getName().equals("-")) {
                    neg = !neg;
                } else if (sons.get(i).getName().equals("!")) {
                    not = !not;
                }
            }
            if(not){
                if(value.isConstantInt()){
                    int num = ((Constant.ConstantInt) value).getNum();
                    if(num==0){
                        value = new Constant.ConstantInt(1);
                    }else {
                        value =  new Constant.ConstantInt(0);
                    }
                }else {
                    value = new Icmp("==", value, Constant.ConstantInt.ZERO);
                }
            }
            if(neg) {//若为负数
                if (value.isConstantInt()) {
                    int num = ((Constant.ConstantInt) value).getNum();
                    value = new Constant.ConstantInt(-num);
                } else {
                    value = new BinaryOperator("-", Constant.ConstantInt.ZERO, value);
                }
            }
            return value;
        }
    }

    @Override
    public void check(ESymbolTable eSymbolTable, int loopCycles) {
        int pos = 0;
        if (sons.get(pos) instanceof PrimaryExpNode) {
            type = 0;
            sons.get(pos).check(eSymbolTable, loopCycles);
            dim = ((PrimaryExpNode) getSon(pos)).getDim();
        } else if (!sons.get(pos).getType().equals(Def.Ident)) {//为UnaryOp
            type = 2;
            getSon(-1).check(eSymbolTable, loopCycles);
            dim = ((UnaryExpNode) getSon(-1)).getDim();
        } else {//调用参数
            type = 1;
            Token ident = sons.get(pos);//包含函数名
            int line = ident.getLine();
            Object obj = eSymbolTable.getSymbolInAll(ident);
            if (obj instanceof ErrorNode) {
                errors.add((ErrorNode) obj);
            } else {
                FuncSymbol funcSym = (FuncSymbol) obj;
                dim = funcSym.getType().equals(Def.Int) ? 0 : -1;
                pos += 2;
                if (getSon(pos) instanceof FuncRParamsNode) {//检查函数实参
                    ((FuncRParamsNode) getSon(pos)).setArgs(funcSym, line);
                    ((FuncRParamsNode) getSon(pos)).check(eSymbolTable, loopCycles);
                } else if (funcSym.getParamsNum() != 0) {
                    errors.add(new ErrorNode(line, ErrorNode.ECode.d, "function paramsNum not corresponds in line " + line));
                }
            }
        }
    }

}
