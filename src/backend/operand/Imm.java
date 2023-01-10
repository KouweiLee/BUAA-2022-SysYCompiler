package backend.operand;

public class Imm implements MSOperand {
    private int num;
    private Imm anotherImm = null;
    public static Imm ZERO = new Imm(0);

    public Imm(Imm imm, int num) {
        this.num = num;
        anotherImm = imm;
    }

    public Imm(int num) {
        this.num = num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public boolean needsAllocate() {
        return false;
    }

    @Override
    public String toString() {
        if(anotherImm == null){
            return String.valueOf(num);
        }
        return String.valueOf(anotherImm.getNum() + num);
    }

    public String getNumSub(){
        if(anotherImm == null){
            return String.valueOf(-num);
        }
        return String.valueOf(-(anotherImm.getNum() + num));
    }

    public int getNum() {
        if(anotherImm == null)
            return num;
        return anotherImm.num + num;
    }
}
