package backend.operand;

public class VirReg implements Reg{
    private static int index = 0;
    private String name;

    public VirReg(){
        this.name = "v" + index++;
    }

    public String toString(){
        return this.name;
    }

    @Override
    public boolean needsAllocate() {
        return true;
    }
}
