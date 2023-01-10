package backend.operand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class PhyReg implements Reg {
    private static final HashMap<Integer, PhyReg> globalRegsMap = new HashMap<>();

    public static PhyReg getGlobalReg(Integer integer) {
        return globalRegsMap.get(integer);
    }

    public static Set<Integer> getRegIndexes() {
        return globalRegsMap.keySet();
    }

    public static final PhyReg zero = new PhyReg("zero");
    public static final PhyReg v0 = new PhyReg("v0");
    public static final PhyReg sp = new PhyReg("sp");
    public static final PhyReg ra = new PhyReg("ra");
    public static final PhyReg a0 = new PhyReg("a0");

    public static final PhyReg t0 = new PhyReg("t0");
    public static final PhyReg t1 = new PhyReg("t1");
    public static final PhyReg t2 = new PhyReg("t2");
    public static final PhyReg t3 = new PhyReg("t3");
    public static final PhyReg t4 = new PhyReg("t4");
    public static final PhyReg t5 = new PhyReg("t5");
    public static final PhyReg t6 = new PhyReg("t6");
    public static final PhyReg t7 = new PhyReg("t7");
    public static final PhyReg t8 = new PhyReg("t8");
    public static final PhyReg t9 = new PhyReg("t9");

    public static final PhyReg s0 = new PhyReg("s0");
    public static final PhyReg s1 = new PhyReg("s1");
    public static final PhyReg s2 = new PhyReg("s2");
    public static final PhyReg s3 = new PhyReg("s3");
    public static final PhyReg s4 = new PhyReg("s4");
    public static final PhyReg s5 = new PhyReg("s5");
    public static final PhyReg s6 = new PhyReg("s6");
    public static final PhyReg s7 = new PhyReg("s7");
    public static final PhyReg v1 = new PhyReg("v1");
    public static final PhyReg a1 = new PhyReg("a1");
    public static final PhyReg a2 = new PhyReg("a2");
    public static final PhyReg a3 = new PhyReg("a3");
    public static final PhyReg k0 = new PhyReg("k0");
    public static final PhyReg k1 = new PhyReg("k1");
    public static final PhyReg gp = new PhyReg("gp");
    //    public static final PhyReg
    public static List<PhyReg> tempRegs = new ArrayList<PhyReg>() {{
        add(k0);
        add(k1);
        add(gp);
//        add(t0);
//        add(t1);
//        add(t2);
//        add(t3);
//        add(t4);
//        add(t5);
//        add(t6);
//        add(t7);
//        add(t8);
//        add(t9);
    }};

    public static List<PhyReg> getTempRegs() {
        return tempRegs;
    }

    static {
        globalRegsMap.put(0, s0);
        globalRegsMap.put(1, s1);
        globalRegsMap.put(2, s2);
        globalRegsMap.put(3, s3);
        globalRegsMap.put(4, s4);
        globalRegsMap.put(5, s5);
        globalRegsMap.put(6, s6);
        globalRegsMap.put(7, s7);
        globalRegsMap.put(8, v1);
        globalRegsMap.put(9, a1);
        globalRegsMap.put(10, a2);
        globalRegsMap.put(11, a3);
//        globalRegsMap.put(12, k0);
//        globalRegsMap.put(13, k1);
//        globalRegsMap.put(14, gp);
        globalRegsMap.put(15, t0);
        globalRegsMap.put(16, t1);
        globalRegsMap.put(17, t2);
        globalRegsMap.put(18, t3);
        globalRegsMap.put(19, t4);
        globalRegsMap.put(20, t5);
        globalRegsMap.put(21, t6);
        globalRegsMap.put(22, t7);
        globalRegsMap.put(23, t8);
        globalRegsMap.put(24, t9);
    }

//    private static HashSet<Integer> regsIndexes = new HashSet<>(nameMap.keySet());

//    public static HashSet<Integer> getRegsIndexes() {
//        return regsIndexes;
//    }

    private String name;
    private int index;//nameMap的key

//    public PhyReg(int index) {
//        this.name = nameMap.get(index);
//        this.index = index;
//    }

    public PhyReg(String name) {
        this.name = name;
        this.index = -1;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "$" + name;
    }

    @Override
    public boolean needsAllocate() {
        return false;
    }
}
