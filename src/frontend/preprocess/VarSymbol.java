package frontend.preprocess;

public class VarSymbol extends ESymbol {
    private boolean isConst;
    private int n1;
    private int n2;
    private int dim;//-1表示无维度, 为void; 0表示一个数, 1表示一维数组, 2表示二维数组
    public VarSymbol(String name, String type, boolean isConst, int n1, int n2, int dim, int defline){
        super(name, type, defline);
        this.isConst = isConst;
        this.n1 = n1;
        this.n2 = n2;
        this.dim = dim;
    }
    public boolean isConst(){
        return isConst;
    }
    public int getDim(){
        return dim;
    }
}
