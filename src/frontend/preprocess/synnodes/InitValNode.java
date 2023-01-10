package frontend.preprocess.synnodes;

import frontend.irgenerate.Initial;
import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.ESymbolTable;
import frontend.preprocess.Visitor;
import utils.Evaluate;

public class InitValNode extends Token{
    public InitValNode() {
    }

    public Value visit(Type type) {
        boolean eval = Visitor.isGlobal || Visitor.isConst;
        if(eval){
            if(getSon(0) instanceof AddExpNode){
                int val =  Evaluate.evaluateAddExp((AddExpNode) getSon(0));
                return new Initial.ValueInitial(new Constant.ConstantInt(val));
            }
        }
        //不对值进行求值操作
        else {
            if(getSon(0) instanceof AddExpNode){
                Value init = getSon(0).visit();
                return new Initial.ValueInitial(init);
            }
        }
        //数组的初始化
        Initial.ArrayInitial arrayInit = new Initial.ArrayInitial(type);
        for(int i=1;i<sons.size()-1;i++){
            Initial init = (Initial) ((InitValNode)getSon(i))
                    .visit((((Type.ArrayType)type).getInnerType()));
            arrayInit.addInit(init);
        }
        return arrayInit;
    }

    @Override
    public void check(ESymbolTable ESymbolTable, int loopCycles){
        int sz = sons.size();
        if(getSon(0) instanceof AddExpNode){
            getSon(0).check(ESymbolTable, loopCycles);
        }else {
            for(int i=1;i<sz-1;i++){
                getSon(i).check(ESymbolTable, loopCycles);
            }
        }
    }
}
