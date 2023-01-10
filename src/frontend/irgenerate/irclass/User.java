package frontend.irgenerate.irclass;

import frontend.irgenerate.Type;

import java.util.ArrayList;
import java.util.List;

public class User extends Value {
    //使用的value列表
    protected List<Value> operands = new ArrayList<>();

    public User(Type type) {
        super(type);
    }

    public Value getOperand(int pos) {
        return operands.get(pos);
    }

    public List<Value> getOperands() {
        return operands;
    }

    public void addValueDef(Value value) {
        operands.add(value);
        value.addUser(this);
    }

    public void addValueDef(Value value1, Value value2) {
        operands.add(value1);
        operands.add(value2);
        value1.addUser(this);
        value2.addUser(this);
    }

    public void addValueDef(List<Value> values) {
        if (values == null) {
            return;
        }
        operands.addAll(values);
        for (Value value : values) {
            if(value == null){
                System.out.println("aweawe");
            }
            value.addUser(this);
        }
    }

    /**
     * 将第index个操作数替换为newValue, 要将本User加入到newValue的User中
     * @param newValue
     * @param index
     */
    public void replaceValue(Value newValue, int index){
        operands.set(index, newValue);
        newValue.addUser(this);
    }

    public void replaceValue(Value newValue, Value oldValue) {
        int pos;
        for(pos=0; pos < operands.size(); pos++){
            if(operands.get(pos).equals(oldValue)){
                replaceValue(newValue, pos);
                break;
            }
        }
    }
}
