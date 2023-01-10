package frontend.preprocess.synnodes;

import frontend.irgenerate.Initial;
import frontend.irgenerate.IrModule;
import frontend.irgenerate.Symbol;
import frontend.irgenerate.Type;
import frontend.irgenerate.instr.Alloca;
import frontend.irgenerate.instr.GetElementPtr;
import frontend.irgenerate.instr.Store;
import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.GlobalValue;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Def;
import frontend.preprocess.ESymbol;
import frontend.preprocess.ESymbolTable;
import frontend.preprocess.VarSymbol;
import frontend.preprocess.Visitor;
import utils.Evaluate;

import java.util.ArrayList;

public class DefNode extends Token {
    private boolean isConstDef;

    public DefNode(boolean isConstDef) {
        this.isConstDef = isConstDef;
    }

    @Override
    public Value visit() {
        String name = getSon(0).getName();
        Value pointer;
        //进行变量类型的确定
        Type typeNow = Type.BasicType.I32;
        for (int i = sons.size()-1; i >=0 ; i--) {
            if ("[".equals(getSon(i).getName())) {
                int dim = Evaluate.evaluateAddExp((AddExpNode) getSon(i + 1));
                typeNow = new Type.ArrayType(typeNow, dim);
            }
        }

        Initial initial = null;
        if (getSon(-1) instanceof InitValNode) {// 如果有初始值
            initial = (Initial) ((InitValNode) getSon(-1)).visit(typeNow);
        }
        //生成定义代码
        if (Visitor.isGlobal) {//全局变量
            pointer = new GlobalValue(initial, name, typeNow.toPointerType(), false);
        } else {//局部变量,需要根据initial在栈上分配地址,还需进行初始化
            pointer = new Alloca(typeNow);
            if (initial != null) {
                if (!(typeNow instanceof Type.ArrayType)) {
                    new Store(initial.getValue(), pointer);
                } else {
                    Initial.ArrayInitial array_init = (Initial.ArrayInitial) initial;
                    ArrayList<Initial> arrayInitInitials = array_init.getInitials();
                    for (int i = 0; i < arrayInitInitials.size(); i++) {
                        if (((Type.ArrayType) typeNow).isdim2array()) {//如果是二维数组
                            //获取二维数组初始化的里层{}
                            ArrayList<Initial> initials = ((Initial.ArrayInitial) arrayInitInitials.get(i)).getInitials();
                            for (int j = 0; j < initials.size(); j++) {
                                GetElementPtr ptr = new GetElementPtr(pointer, new Constant.ConstantInt(i), new Constant.ConstantInt(j));
                                new Store(initials.get(j).getValue(), ptr);
                            }
                        } else {
                            GetElementPtr ptr = new GetElementPtr(pointer, new Constant.ConstantInt(i));
                            new Store(arrayInitInitials.get(i).getValue(), ptr);
                        }
                    }
                }
            }
        }
        Symbol symbol = new Symbol(name, typeNow, pointer, initial, Visitor.isConst);
        Visitor.visitor.addSymbol(symbol);
        //如果为全局变量, 加入全局区
        if (Visitor.isGlobal) {
            IrModule.module.addGlobalValue((GlobalValue) pointer);
        }
        return null;
    }


    @Override
    public void check(ESymbolTable eSymbolTable, int loopCycles) {
        if (isConstDef) {
            checkForConstDef(eSymbolTable, loopCycles);
        } else {
            checkForVarDef(eSymbolTable, loopCycles);
        }
    }

    private void checkForVarDef(ESymbolTable eSymbolTable, int loopCycles) {
        int dim = 0;
        int pos = 0;
        String name = (sons.get(pos)).getName();
        int line = sons.get(pos).getLine();
        pos++;
        while (pos < sons.size() && "[".equals(getSon(pos).getName())) {
            dim++;
            pos++;
            sons.get(pos).check(eSymbolTable, loopCycles);//ConstExp
            pos += 2;
        }
        if (pos != sons.size()) {
            pos++;
            sons.get(pos).check(eSymbolTable, loopCycles);//constInitval
        }
        ESymbol sym = new VarSymbol(name, Def.Int, false, 0, 0, dim, line);
        ErrorNode err = eSymbolTable.addSymbol(sym);
        if (err != null) {
            errors.add(err);
        }
    }

    private void checkForConstDef(ESymbolTable eSymbolTable, int loopCycles) {
        int dim = 0;
        int pos = 0;
        String name = (sons.get(pos)).getName();
        int line = sons.get(pos).getLine();
        pos++;
        while ("[".equals(getSon(pos).getName())) {
            dim++;
            pos++;
            sons.get(pos).check(eSymbolTable, loopCycles);//ConstExp
            pos++;
            pos++;
        }
        pos++;
        sons.get(pos).check(eSymbolTable, loopCycles);//constInitval
        ESymbol sym = new VarSymbol(name, Def.Int, true, 0, 0, dim, line);
        ErrorNode err = eSymbolTable.addSymbol(sym);
        if (err != null) {
            errors.add(err);
        }
    }
}
