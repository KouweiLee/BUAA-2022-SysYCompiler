package frontend.irgenerate;

public class Type {

    public enum Dtype {//Datetype
        I32("i32"),
        I8("i8"),
        I1("i1"),
        Void("void");

        private final String name;

        private Dtype(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }


    }
    public Type getInnerType(){
        return this;
    }
    /**
     * 获取该类型所包含的元素数量
     */
    public int getSize(){
        return 1;
    }
    public String toString() {
        return "wrong";
    }

    public Type toPointerType() {
        return new PointerType(this);
    }

    public static class BasicType extends Type {
        private Dtype type;
        public static final BasicType I32 = new BasicType(Dtype.I32);
        public static final BasicType I8 = new BasicType(Dtype.I8);
        public static final BasicType I1 = new BasicType(Dtype.I1);

        private BasicType(Dtype dtype) {
            type = dtype;
        }

        public String toString() {
            return type.getName();
        }
    }

    public static class VoidType extends Type {
        public static final VoidType VOID_TYPE = new VoidType();

        private VoidType() {
        }

        public String toString() {
            return "void";
        }
    }

    public static class ArrayType extends Type {
        private Type innerType;//保留下一个维度的type
        private int dim_lens;//该维长度
        private int size = 0; // 总的元素个数
        //是否为二维数组
        private boolean dim2array = false;

        /**
         * 不是获取大小, 而是总的元素个数
         * @return
         */
        public int getSize() {
//            System.out.println("size"+size);
            return size;
        }

        public ArrayType(Type innerType, int dim_lens) {
            this.innerType = innerType;
            size = dim_lens;
            if (innerType instanceof ArrayType) {
                dim2array = true;
                size *= ((ArrayType) innerType).getSize();
            }
            this.dim_lens = dim_lens;
        }

        public boolean isdim2array() {
            return dim2array;
        }

        public Type getInnerType() {
            return innerType;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(dim_lens).append(" x ").append(innerType).append("]");
            return sb.toString();
        }

        //        private Type elementType;//元素类型
//        private ArrayList<Integer> dims;
//
//        public ArrayType(ArrayList<Integer> dims, Type eletype) {
//            this.dims = dims;
//            this.elementType = eletype;
//        }
//
//        /**
//         * 数组只有一维可以这样初始化
//         *
//         * @param dim1
//         * @param elementType
//         */
//        public ArrayType(int dim1, Type elementType) {
//            this.dims = new ArrayList<>();
//            dims.add(dim1);
//            this.elementType = elementType;
//        }

//        @Override
//        public String toString() {
//            StringBuilder sb = new StringBuilder();
//            sb.append("[")
//            for (int i = 0; i < dims.size(); i++) {
//                sb.append(dims.get(i));
//                sb.append("x");
//                if (i == (dims.size() - 1)){//i为最后一维
//                    sb.append(elementType);
//                }
//            }
//        }
    }

    public static class PointerType extends Type {
        private Type innerType;//指向的type

        public PointerType(Type innerType) {
            this.innerType = innerType;
        }

        public Type getInnerType() {
            return innerType;
        }

        public String toString() {
            return innerType + "*";
        }
    }
//    private Dtype type;
//    private int dim1 = 0;
//    private int dim2 = 0;
//    private boolean isArray;

//    public Type(Dtype type){
//        this.type = type;
//        isArray = false;
//    }
//
//    public boolean isArrayType() {
//        return isArray;
//    }
}
