package frontend.irgenerate.irclass;

import frontend.irgenerate.Initial;
import frontend.irgenerate.Type;

public class GlobalValue extends Value {
    private Initial initial;
    private String globalType = "var";

    public GlobalValue(Initial initial, String name, Type type, boolean isStr) {
        super(type);//type为这个GlobalValue的真实type的pointerType
        if (initial == null) {
            if (((Type.PointerType) type).getInnerType() instanceof Type.ArrayType) {
                this.initial = new Initial.ZeroInitial(((Type.PointerType) type).getInnerType());
            } else
                this.initial = new Initial.ValueInitial(new Constant.ConstantInt(0));
        } else {
            this.initial = initial;
        }
        if (isStr) {
            globalType = "string";
        } else if (((Type.PointerType) type).getInnerType() instanceof Type.ArrayType) {
//        }else {
            globalType = "array";
        }
        super.name = GLOBAL_NAME_PREFIX + name;
        prefix = GLOBAL_PREFIX;
    }

    public String getGlobalType() {
        return globalType;
    }

    public GlobalValue() {
    }

    public Initial getInitial() {
        return initial;
    }

    @Override
    public String toString() {
        return getFullName() + " = global " + initial.getTypeName();
    }

//    public static class GlobalStr extends GlobalValue {
//        private String str2print;
//
//        public GlobalStr(String s) {
//            str2print = s;
//        }
//    }
}
