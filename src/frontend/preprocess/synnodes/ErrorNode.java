package frontend.preprocess.synnodes;

public class ErrorNode extends Token implements Comparable<ErrorNode>{

    public enum ECode {
        a, b, c, d, e, f, g, h, i, j, k, l, m
    }

    private int line;
    private ECode code;
    private String info;

    public ErrorNode(int line, ECode code, String info) {
        this.line = line;
        this.code = code;
        this.info = info;
    }

    @Override
    /**
     * 从小到大根据line排序
     */
    public int compareTo(ErrorNode other) {
        return Integer.compare(line, other.line);
    }

    @Override
    public String toString() {
        return line +  " " + code.name();
    }
}
