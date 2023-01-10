package frontend.preprocess.synnodes;

import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.Argument;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.ESymbolTable;
import frontend.preprocess.VarSymbol;
import utils.Evaluate;

public class FuncFParamNode extends Token{
    private VarSymbol param;
    public FuncFParamNode() {
    }

    @Override
    public Value visit(){
        String name = getSon(1).getName();
        int size = sons.size();
        Type type = null;
        if(size == 4){//一维数组
            type = new Type.PointerType(Type.BasicType.I32);
        }else if(size == 7){
            int lens = Evaluate.evaluateAddExp((AddExpNode) getSon(5));
            type = new Type.ArrayType(Type.BasicType.I32, lens).toPointerType();
        }else {
            type = Type.BasicType.I32;
        }
        return new Argument(type, name, 0);
    }

    public VarSymbol getParam(){
        return param;
    }
    @Override
    public void check(ESymbolTable ESymbolTable, int loopCycles) {
        int pos = 0;
        int dim = 0;
        String type = getSon(pos).getType();
        pos++;
        int line = sons.get(pos).getLine();
        String name = sons.get(pos++).getName();
        if (pos < sons.size() && "[".equals(getSon(pos).getName())) {
            dim++;
            pos+=2;
        }
        while (pos < sons.size() && "[".equals(getSon(pos).getName())) {
            dim++;
            pos++;
            ((AddExpNode) getSon(pos)).check(ESymbolTable, loopCycles);
            pos+=2;
        }

        VarSymbol sym = new VarSymbol(name, type, false, 0, 0, dim, line);
        ErrorNode err = ESymbolTable.addSymbol(sym);
        if(err!=null) errors.add(err);
        param = sym;
    }
}
