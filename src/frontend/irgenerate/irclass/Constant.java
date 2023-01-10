package frontend.irgenerate.irclass;

import frontend.irgenerate.Type;

public class Constant extends Value {

    public static class ConstantInt extends Constant {
        public static final ConstantInt ZERO = new ConstantInt(0);
        private final int num;

        public ConstantInt(int num) {
            type = Type.BasicType.I32;
            name = String.valueOf(num);
            this.num = num;
        }

        public int getNum() {
            return num;
        }
//
//        public void turn2neg() {
//            System.out.println("asdasd");
//            num = -num;
//        }

        @Override
        public String getFullName() {
            return String.valueOf(num);
        }
    }

    public static class ConstantStr extends Constant {
        private String str;

        public ConstantStr(String str) {
            type = new Type.ArrayType(Type.BasicType.I8, str.length() + 1);
            this.str = str;
        }

        public String getStr() {
            return str;
        }

        @Override
        public String getFullName() {
            return "c\"" + str + "\\00" + "\"";
        }
    }
}
