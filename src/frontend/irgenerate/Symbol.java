package frontend.irgenerate;

import frontend.irgenerate.irclass.Value;

public class Symbol {
    private Type type;
    private String name;//符号表中存的, 是源程序中变量的真实名字
    private Initial init;
    private Value value;//局部变量: alloca出来的地址; 全局变量:globalvalue
    private boolean isConstant;
    public Type getType(){
        return type;
    }
    public Initial getInit() {
        return init;
    }

    public String getName() {
        return name;
    }

    public Value getValue() {
        return value;
    }

    public Symbol(String name, Type type, Value value, Initial init, boolean isConstant) {
        this.name = name;
        this.init = init;
        this.value = value;
        this.type = type;
        this.isConstant = isConstant;
    }

    public boolean isConstant() {
        return isConstant;
    }
}
