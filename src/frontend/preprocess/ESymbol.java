package frontend.preprocess;

public class ESymbol {
    //以下仅用于错误处理
    private String name;//变量名字
    private String type;//在错误处理中, 为返回值或值类型;
    private int defline;

    public ESymbol(String name, String type, int defline){
        this.name = name;
        this.type = type;
        this.defline = defline;
    }

    public ESymbol(){}

    public int getDefline() {
        return defline;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
