package frontend.preprocess;

import frontend.preprocess.synnodes.ErrorNode;
import frontend.preprocess.synnodes.Token;

import java.util.HashMap;
import java.util.Map;

public class ESymbolTable {
    private Map<String, ESymbol> symbols = new HashMap<>();
    private ESymbolTable father;

    public ESymbolTable() {

    }

    public ESymbolTable newSon() {
        ESymbolTable son = new ESymbolTable();
        son.setFather(this);
        return son;
    }

    public void setFather(ESymbolTable father) {
        this.father = father;
    }

    public ESymbolTable getFather() {
        return father;
    }

    private ESymbol getSymbolInThis(String name) {
        return symbols.get(name);
    }

//    //查找符号时在全局作用域下查找
//    public ErrorNode checkIsDefined(Token ident) {
//        SymbolTable now = this;
//        String name = ident.getName();
//        while (now != null) {
//            if (now.containsSymInNow(name)) return null;
//            now = now.getFather();
//        }
//        //使用了未定义的名字
//        return new ErrorNode(ident.getLine(), ErrorNode.ECode.c, "use undefined ident " + name + " in line " + ident.getLine());
//    }

    public Object getSymbolInAll(Token ident) {
        ESymbolTable now = this;
        String name = ident.getName();
        while (now!=null){
            if(now.containsSymInNow(name)) return now.getSymbolInThis(name);
            now = now.getFather();
        }
        return new ErrorNode(ident.getLine(), ErrorNode.ECode.c, "use undefined ident " + name + " in line " + ident.getLine());
    }

    public boolean containsSymInNow(String name) {
        return symbols.containsKey(name);
    }

    //加符号时在当前作用域下查找是否有重名
    public ErrorNode addSymbol(ESymbol symbol) {
        if (symbols.containsKey(symbol.getName())) {
            return new ErrorNode(symbol.getDefline(), ErrorNode.ECode.b, "symbol defines doubles in line " + symbol.getDefline());
        }else {
            symbols.put(symbol.getName(), symbol);
        }
        return null;
    }
}
