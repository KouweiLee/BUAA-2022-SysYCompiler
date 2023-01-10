package backend.operand;

public class MSLabel implements MSOperand{
    private String label;

    public MSLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean needsAllocate() {
        return false;
    }
}
