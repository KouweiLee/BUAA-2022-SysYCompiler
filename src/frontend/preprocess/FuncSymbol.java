package frontend.preprocess;

import java.util.List;

public class FuncSymbol extends ESymbol {
    private List<VarSymbol> params;
    private int paramsNum;
    public FuncSymbol(String name, String type, int defline, List<VarSymbol> params) {
        //函数名, 函数返回值类型, 函数定义行
        super(name, type, defline);
        this.params = params;
        this.paramsNum = params.size();
    }
    public VarSymbol getParam(int pos){
        return params.get(pos);
    }
    public int getParamsNum(){
        return paramsNum;
    }
}
