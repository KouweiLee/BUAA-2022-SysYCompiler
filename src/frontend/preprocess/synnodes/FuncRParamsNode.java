package frontend.preprocess.synnodes;

import frontend.irgenerate.irclass.Value;
import frontend.preprocess.FuncSymbol;
import frontend.preprocess.ESymbolTable;
import frontend.preprocess.Visitor;

import java.util.ArrayList;
import java.util.List;

public class FuncRParamsNode extends Token {
    private FuncSymbol funcSymbol;
    private int useline;

    public FuncRParamsNode() {
    }

    public List<Value> getArguments() {
        Visitor.isGetRParam = true;
        List<Value> arguments = new ArrayList<>();
        for (Token son : sons) {
            arguments.add(son.visit());
        }
        Visitor.isGetRParam = false;
        return arguments;
    }

    public void setArgs(FuncSymbol funcSymbol, int useline) {
        this.funcSymbol = funcSymbol;
        this.useline = useline;
    }

    public void check(ESymbolTable ESymbolTable, int loopCycles) {
        int realParamsNum = sons.size();
        if (realParamsNum != funcSymbol.getParamsNum()) {
            errors.add(new ErrorNode(useline, ErrorNode.ECode.d, "function paramsNum not corresponds in line " + useline));
        } else {
            for (int i = 0; i < realParamsNum; i++) {
                AddExpNode son = (AddExpNode) sons.get(i);
                son.check(ESymbolTable, loopCycles);
                //检查实参和形参的参数类型是否匹配
                if (son.getDim() != funcSymbol.getParam(i).getDim()) {
                    errors.add(new ErrorNode(useline, ErrorNode.ECode.e, "function param type not corresponds in line " + useline));
                }
            }
        }
    }
}
