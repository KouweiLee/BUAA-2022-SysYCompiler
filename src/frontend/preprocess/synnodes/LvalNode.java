package frontend.preprocess.synnodes;

import frontend.irgenerate.Symbol;
import frontend.irgenerate.Type;
import frontend.irgenerate.instr.GetElementPtr;
import frontend.irgenerate.instr.Load;
import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.ESymbolTable;
import frontend.preprocess.VarSymbol;
import frontend.preprocess.Visitor;

import java.util.ArrayList;
import java.util.List;

public class LvalNode extends Token {
    //这个lval真实的dim
    private int dim = 0;
    //这个lval对应的varSymbol
    VarSymbol varSymbol = null;
    private boolean needLoad = false;

    public LvalNode() {
    }

    public Value visit() {
        String name = getSon(0).getName();
        Symbol symbol = Visitor.visitor.getSymbol(name);
        int size = sons.size();
        if (symbol.isConstant() && symbol.getType() instanceof Type.BasicType) {//非数组常量, 一定作为右值出现
            return symbol.getInit().getValue();
        }
        Value pointer = symbol.getValue();
        Type innerType = ((Type.PointerType)pointer.getType()).getInnerType();
        List<Value> indexes = new ArrayList<>();
        indexes.add(Constant.ConstantInt.ZERO);
        boolean flag = false;
        for(int i=1; i< sons.size(); i++){
            if(getSon(i) instanceof AddExpNode){
                Value offset = getSon(i).visit();
                if(innerType instanceof Type.PointerType){//左值为函数的参数
                    Value instr = new Load(pointer);
                    innerType = ((Type.PointerType) innerType).getInnerType();
                    pointer = instr;
                    indexes.clear();
                    indexes.add(offset);
                    flag = true;
                }else if(innerType instanceof Type.ArrayType){
                    innerType = ((Type.ArrayType) innerType).getInnerType();
                    indexes.add(offset);
                    flag = true;
                }else {
                    assert false;
                }
            }
        }
        if(flag) pointer = new GetElementPtr(innerType.toPointerType(), pointer, indexes);
        if(!needLoad){
            return pointer;
        }else {
            if(innerType instanceof Type.BasicType || innerType instanceof Type.PointerType){
                return new Load(pointer);
            }else {//为数组类型, 表示传参, 需要解到下一层
                return new GetElementPtr(pointer, Constant.ConstantInt.ZERO);
            }
        }
    }

    public void setNeedLoad(boolean needLoad) {
        this.needLoad = needLoad;
    }

    public int getDim() {
        return dim;
    }

    public VarSymbol getVarSymbol() {
        return varSymbol;
    }

    public void check(ESymbolTable eSymbolTable, int loopCycles) {
        int pos = 0;
        Object obj = eSymbolTable.getSymbolInAll(getSon(0));
        if (obj instanceof ErrorNode) {//不存在该ident
            errors.add((ErrorNode) obj);
        } else {
            VarSymbol symbol = (VarSymbol) obj;
            varSymbol = symbol;
//            if(symbol.isConst()){
//                errors.add(new ErrorNode(getSon(0).getLine(), ErrorNode.ECode.h, "cannot change const in line"+getSon(0).getLine()));
//            }
            int varDim = symbol.getDim();
            pos++;
            while (pos < sons.size() && "[".equals(getSon(pos).getName())) {
                varDim--;
                pos++;
                getSon(pos).check(eSymbolTable, loopCycles);
                pos += 2;
            }
            dim = varDim;
        }
    }
}
