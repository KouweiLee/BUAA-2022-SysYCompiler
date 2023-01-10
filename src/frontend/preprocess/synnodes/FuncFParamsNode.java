package frontend.preprocess.synnodes;

import frontend.irgenerate.irclass.Argument;
import frontend.preprocess.ESymbolTable;
import frontend.preprocess.VarSymbol;

import java.util.ArrayList;
import java.util.List;

public class FuncFParamsNode extends Token {
    private List<VarSymbol> params = new ArrayList<>();

    public FuncFParamsNode() {
    }

    public List<Argument> getArguments(){
        List<Argument> arguments = new ArrayList<>();
        for (Token son : sons) {
            arguments.add((Argument) son.visit());
        }
        return arguments;
    }

    public List<VarSymbol> getParams() {
        return params;
    }

    public void check(ESymbolTable ESymbolTable, int loopCycles) {
        for (Token funcFParam : sons) {
            funcFParam.check(ESymbolTable, loopCycles);
            params.add(((FuncFParamNode) funcFParam).getParam());
        }
    }
}
