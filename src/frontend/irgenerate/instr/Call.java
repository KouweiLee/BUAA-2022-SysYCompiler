package frontend.irgenerate.instr;

import frontend.irgenerate.IrModule;
import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.Function;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Visitor;

import java.util.HashSet;
import java.util.List;

public class Call extends Instr {
    private Function function;

    public Call(String name, List<Value> arguments) {
        super(IrModule.module.getFunction(name).getType(), Opcode.CALL, Visitor.curBlock);
        addValueDef(arguments);
        this.function = IrModule.module.getFunction(name);
    }

    public Call(Function function, List<Value> arguments) {
        super(function.getType(), Opcode.CALL, Visitor.curBlock);
        addValueDef(arguments);
        this.function = function;
    }
    @Override
    public HashSet<Value> getUse4bb(){
        use4bb.addAll(operands);
        return use4bb;
    }

    @Override
    public Value getDef4bb(){
        if(getType().equals(Type.VoidType.VOID_TYPE)){
            return null;
        }
        return this;
    }

    @Override
    public String toString() {
        String operandsString = operands.stream().map(Value::getTypeName).reduce((s, s1) -> s + ", " + s1).orElse("");
        String ret = "call " + type + " " + function.getFullName() + "(" + operandsString + ")";
        if(type.equals(Type.VoidType.VOID_TYPE)){
            return ret;
        }else {
            return getFullName() + " = " + ret;
        }
    }

    public String getFuncName(){
        return function.getOnlyName();
    }

    public boolean isExternal(){
        return function.isExternal();
    }
}
