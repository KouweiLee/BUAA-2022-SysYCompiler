package frontend.preprocess.synnodes;

public class NumberNode extends Token{
    private int number;
    public NumberNode(int number) {
        this.number = number;
    }
    public int getNumber(){
        return number;
    }
}
