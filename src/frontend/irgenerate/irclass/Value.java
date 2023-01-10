package frontend.irgenerate.irclass;

import backend.operand.VirReg;
import frontend.irgenerate.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Value {
    public static final String GLOBAL_PREFIX = "@";
    public static final String LOCAL_PREFIX = "%";
    public static final String GLOBAL_NAME_PREFIX = "g_";
    public static final String LOCAL_NAME_PREFIX = "v";
    public static final String FPARAM_NAME_PREFIX = "f";
    protected Type type;
    protected String name;//name除了@和%的部分.
    protected String prefix;
    protected HashSet<User> users = new HashSet<>();//使用该值的user存, 自身不存
    protected HashSet<Value> use4bb = new HashSet<>();
    protected Value def4bb;
    private static int hashnum = 0;
    private int hash = hashnum++;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        return hash == value.hash;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }

    /**
     * 获取指令的def
     * @return
     */
    public Value getDef4bb(){
        return this;
    }

    /**
     * 获取指令的use
     * @return
     */
    public HashSet<Value> getUse4bb(){
        return use4bb;
    }

    public HashSet<User> getUsers() {
        return users;
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    /**
     * 将本value的所有user对当前value的使用, 替换为newValue. 注意, newValue必须是新的.
     * 关键是要让newValue有新的User, 本value的所有user全部清空.
     */
    public void replaceAllUsesWith(Value newValue) {
        for (User user : users) {
            int size = user.operands.size();
            for (int i = 0; i < size; i++) {
                if (user.operands.get(i).equals(this)) {
                    user.replaceValue(newValue, i);
                }
            }
        }
        users.clear();
    }

    public Value() {
        this.type = Type.VoidType.VOID_TYPE;
    }

    public Value(Type type) {
        this.type = type;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public Type getType() {
        return type;
    }

    public String getOnlyName() {
        return name;
    }

    public String getFullName() {
        return prefix + name;
    }

    public String getTypeName() {
        return type + " " + getFullName();
    }

    public boolean isConstantInt() {
        return this instanceof Constant.ConstantInt;
    }

    public void remove() {
        users.clear();
    }

    public static class VirtualValue extends Value{
        private static int virtual_index = 0;

        /**
         * 虚拟的值, 以r打头
         * @param type
         */
        public VirtualValue(Type type){
            super(type);
            prefix = LOCAL_PREFIX;
            name = "r" + virtual_index++;
        }
    }
}
