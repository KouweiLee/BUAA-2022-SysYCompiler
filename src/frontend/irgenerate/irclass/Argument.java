package frontend.irgenerate.irclass;

import frontend.irgenerate.Type;

public class Argument extends Value{
    private int idx;
    private String code_name;//代码中的名字
    public static int ARGU_COUNT = 0;

    public Argument(Type type, String code_name, int idx){
        super(type);
        this.idx = idx;
        super.name = FPARAM_NAME_PREFIX + ARGU_COUNT++;
        prefix = LOCAL_PREFIX;
        this.code_name = code_name;
    }

    public String getCode_name(){
        return code_name;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
