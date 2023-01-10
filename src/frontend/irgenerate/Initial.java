package frontend.irgenerate;

import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.synnodes.InitValNode;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * 专门为全局变量做的initial, 不作为指令的输出
 */
public class Initial extends Value {
    //    private Type type; //初始值的相关信息
    public Initial(Type type) {
        super(type);
    }

    public Value getValue() {
        return null;
    }

    /**
     * 值的初始化, 包括非数组常量和变量的初始化
     */
    public static class ValueInitial extends Initial {
        private Value value;

        public ValueInitial(Value value) {
            super(value.getType());
            this.value = value;
        }

        @Override
        public Value getValue() {
            return value;
        }

        @Override
        public String getTypeName() {
            return type + " " + value.getFullName();
        }
    }

    /**
     * 数组初值, 一维数组那么Initial就为各组值; 二维数组则
     */
    public static class ArrayInitial extends Initial {
        private ArrayList<Initial> initials = new ArrayList<>();

        public ArrayInitial(Type type) {
            super(type);//初始化器的类型
        }

        public ArrayList<Initial> getInitials() {
            return initials;
        }

        public void addInit(Initial initial) {
            initials.add(initial);
        }

        @Override
        public String getTypeName() {
            StringBuilder sb = new StringBuilder();
            String s2 = initials.stream().map(Value::getTypeName).reduce((s, s1) -> (s + ", " + s1)).orElse("");
            sb.append(type).append(" ").append("[").append(s2).append("]");
            return sb.toString();
        }
    }


    public static class ZeroInitial extends Initial {
        /**
         * 只针对全局数组的0初始化.
         *
         * @param type
         */
        public ZeroInitial(Type type) {
            super(type);
        }

        @Override
        public String getTypeName() {
            return type + " zeroinitializer";
        }
    }
}
