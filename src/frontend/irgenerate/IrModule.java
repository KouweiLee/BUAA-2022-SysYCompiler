package frontend.irgenerate;

import frontend.irgenerate.irclass.Argument;
import frontend.irgenerate.irclass.Function;
import frontend.irgenerate.irclass.GlobalValue;
import utils.IOhelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class IrModule {
    public static final IrModule module = new IrModule();
    private List<GlobalValue> globalValues = new ArrayList<>();
    //TODO:如果出错, 考虑functions的输出顺序.不应该出错
    private HashMap<String, Function> functions = new HashMap<>();
    private Function mainFunction;

    private IrModule() {
        addFunction(new Function(Type.BasicType.I32, new ArrayList<>(), "getint"));
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument(Type.BasicType.I32, "out", 0));
        addFunction(new Function(Type.VoidType.VOID_TYPE, arguments, "putch"));
        addFunction(new Function(Type.VoidType.VOID_TYPE, arguments, "putint"));
        arguments.clear();
        arguments.add(new Argument(new Type.PointerType(Type.BasicType.I8), "out", 0));
        addFunction(new Function(Type.VoidType.VOID_TYPE, arguments, "putstr"));
        functions.get("getint").setExternal();
        functions.get("putch").setExternal();
        functions.get("putint").setExternal();
        functions.get("putstr").setExternal();
    }

    /**
     * 获取定义了的函数, 不包括外部函数, 包括main函数
     */
    public List<Function> getDefFunctions(){
        List<Function> res = new ArrayList<>();
        for (Function function : functions.values()) {
            if(!function.isExternal()){
                res.add(function);
            }
        }
        res.add(mainFunction);
        return res;
    }

    public Function getMainFunction() {
        return mainFunction;
    }

    public List<Function> getFunctions() {
        return new ArrayList<>(functions.values());
    }

    public List<GlobalValue> getGlobalValues() {
        return globalValues;
    }

    public void setMainFunction(Function function) {
        mainFunction = function;
    }

    public void addFunction(Function function) {
        functions.put(function.getOnlyName(), function);
    }

    public Function getFunction(String name) {
        return functions.get(name);
    }

    public void addGlobalValue(GlobalValue globalValue) {
        globalValues.add(globalValue);
    }

    public void outputLLVM(String filename) throws IOException {
        IOhelper.clear();
        //增加外部函数
        IOhelper.addOutput("declare i32 @getint()");
        IOhelper.addOutput("declare void @putch(i32)");
        IOhelper.addOutput("declare void @putstr(i8*)");
        IOhelper.addOutput("declare void @putint(i32)");
        for (GlobalValue globalValue : globalValues) {
            IOhelper.addOutput(globalValue.toString());
        }
        for (Function function : functions.values()) {
            if (!function.isExternal())
                IOhelper.addOutput(function.toString());
        }
        IOhelper.addOutput(mainFunction.toString());
        IOhelper.output(filename);
    }
}
