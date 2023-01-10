package frontend.irgenerate.instr;

import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Visitor;

import java.util.HashSet;
import java.util.List;

public class GetElementPtr extends Instr {
//    private Type addr_type;
    /**
     * 获取地址
     *
     * @param type:    返回值的type
     * @param ptr:     基址
     * @param indexes: 所有的偏移量
     */
    public GetElementPtr(Type type, Value ptr, List<Value> indexes) {
        super(type, Opcode.GEP, Visitor.curBlock);
        addValueDef(ptr);
        addValueDef(indexes);
    }

    /**
     * 获取地址
     *
     * @param ptr:    基址
     * @param index1: 不包括第一个0
     */
    public GetElementPtr(Value ptr, Value index1) {
        super(null, Opcode.GEP, Visitor.curBlock);
        addValueDef(ptr);
        addValueDef(Constant.ConstantInt.ZERO);
        addValueDef(index1);
        Type type = ((Type.PointerType) ptr.getType()).getInnerType();
        type = ((Type.ArrayType) type).getInnerType();
        this.type = type.toPointerType();
    }

    /**
     * 获取地址
     *
     * @param ptr:    基址
     * @param index1: 不包括0的第二个index1
     * @param index2
     */
    public GetElementPtr(Value ptr, Value index1, Value index2) {
        super(null, Opcode.GEP, Visitor.curBlock);
        addValueDef(ptr);
        addValueDef(Constant.ConstantInt.ZERO);
        addValueDef(index1, index2);
        Type type = ((Type.PointerType) ptr.getType()).getInnerType();
        type = ((Type.ArrayType) type).getInnerType();
        type = ((Type.ArrayType) type).getInnerType();
        this.type = type.toPointerType();
    }

    @Override
    public HashSet<Value> getUse4bb(){
        use4bb.addAll(operands);
        return use4bb;
    }
    /**
     * 获取相对数组基址的offset字节数
     */
    public int getOffset() {
        Type innerType = ((Type.PointerType)getOperand(0).getType()).getInnerType();
        int offset = 0, index;
        for (int i = 1; i < operands.size(); i++) {
            //不一定是常量.
            index = Integer.parseInt(operands.get(i).getFullName());
            offset += index * innerType.getSize();
            innerType = innerType.getInnerType();
        }
        return 4 * offset;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getFullName()).append(" = getelementptr inbounds ")
                .append(((Type.PointerType) getOperand(0).getType()).getInnerType()).append(", ")
                .append(getOperand(0).getType()).append(" ").append(getOperand(0).getFullName()).append(", ");
        int i;
        for (i = 1; i < operands.size() - 1; i++) {
            sb.append(getOperand(i).getTypeName()).append(", ");
        }
        sb.append(getOperand(i).getTypeName());
        return sb.toString();
    }
}
