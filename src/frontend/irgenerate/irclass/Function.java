package frontend.irgenerate.irclass;

import frontend.irgenerate.Type;
import frontend.irgenerate.instr.Instr;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Function extends Value {
    private LinkedList<BasicBlock> basicBlocks = new LinkedList<>();
    private List<Argument> arguments;
    private HashMap<Value, Integer> globalRegsMap;
    private boolean isExternal = false;

    public HashMap<Value, Integer> getGlobalRegsMap() {
        return globalRegsMap;
    }

    public void setGlobalRegsMap(HashMap<Value, Integer> globalRegsMap) {
        this.globalRegsMap = globalRegsMap;
    }

    public Function(Type retType, List<Argument> arguments, String f_name) {
        type = retType;
        this.arguments = arguments;
        name = f_name;
        prefix = GLOBAL_PREFIX;
    }
    public LinkedList<BasicBlock> getBasicBlocks(){
        return basicBlocks;
    }
    public List<Argument> getArguments() {
        return arguments;
    }

    public void addArguments(List<Argument> arguments) {
        this.arguments.addAll(arguments);
    }

    public BasicBlock getFirstBlock() {
        return basicBlocks.getFirst();
    }

    public boolean isExternal() {
        return isExternal;
    }

    public void setExternal() {
        isExternal = true;
    }

    public void insertBlockEnd(BasicBlock basicBlock) {
        basicBlocks.add(basicBlock);
    }

    public BasicBlock getLastBasicBlock() {
        return basicBlocks.getLast();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define ").append(type).append(" ").append(getFullName()).append("(");
//        if (arguments.size() != 0) {
        //首先得到一个流, 之后对每个元素进行map中的函数操作, 得到新的集合流, 执行reduce函数, 将集合缩减成一个, orElse则当流为空时执行的操作.
        String argumentsString = arguments.stream().
                map(Value::getTypeName).reduce((s, s2) -> s + ", " + s2).orElse("");
        sb.append(argumentsString);
//        }
        sb.append(") {\n");
        for (BasicBlock basicBlock : basicBlocks) {
            sb.append(basicBlock.getBlockLabel()).append(":\n");
            for (Instr instr : basicBlock.getInstrs()) {
                sb.append("\t").append(instr).append("\n");
            }
        }
        sb.append("}\n");
        return sb.toString();
    }
}
