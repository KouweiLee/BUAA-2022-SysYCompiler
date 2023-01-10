package frontend.irgenerate;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Symbol> symbols = new HashMap<>();
    private SymbolTable father;

    public SymbolTable() {

    }

    public SymbolTable newSon() {
        SymbolTable son = new SymbolTable();
        son.setFather(this);
        return son;
    }

    public void setFather(SymbolTable father) {
        this.father = father;
    }

    public SymbolTable getFather() {
        return father;
    }

    private Symbol getSymbolInThis(String name) {
        return symbols.get(name);
    }

    public Symbol getSymbolInAll(String name) {
        SymbolTable now = this;
        while (now!=null){
            if(now.containsSymInNow(name)) return now.getSymbolInThis(name);
            now = now.getFather();
        }
        return null;
    }

    public boolean containsSymInNow(String name) {
        return symbols.containsKey(name);
    }

    //加符号时在当前作用域下查找是否有重名
    public void addSymbol(Symbol symbol) {
        symbols.put(symbol.getName(), symbol);
    }
}
