package frontend.preprocess;

import frontend.irgenerate.Symbol;
import frontend.irgenerate.SymbolTable;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Function;
import frontend.preprocess.synnodes.CompUnitNode;
import frontend.preprocess.synnodes.ErrorNode;
import frontend.preprocess.synnodes.SynNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Visitor {
    public static final Visitor visitor = new Visitor();
    private CompUnitNode compUnitNode;
    private ESymbolTable highestTableForErr;
    private SymbolTable highestTableForIR;
    public static SymbolTable curSymbolTable;
    public static boolean isGlobal = true;//用于变量的确定是否为全局变量, 在函数中设为false, 一旦设为False, 就为false
    public static boolean isConst = false;//用于变量是否为常量, 仅在类ConstDecl中有效
    public static boolean isGetRParam = false;//用于判断是否为实参,对lval有效
    public static BasicBlock curBlock = null;
    public static Function curFunc = null;
    //用于continue和break的中间代码生成
    public Stack<BasicBlock> loopConds = new Stack<>();
    public Stack<BasicBlock> loopFollows = new Stack<>();

    public Visitor() {
        highestTableForErr = new ESymbolTable();
        highestTableForErr.setFather(null);
        highestTableForIR = new SymbolTable();
        highestTableForIR.setFather(null);
    }
    public void addSymbol(Symbol symbol) {
        curSymbolTable.addSymbol(symbol);
    }

    public Symbol getSymbol(String name) {
        return curSymbolTable.getSymbolInAll(name);
    }

    public void setCompUnitNode(CompUnitNode compUnitNode) throws IOException {
        this.compUnitNode = compUnitNode;
    }

    public void visit() {
        isGlobal = true;
        curSymbolTable = highestTableForIR;
        compUnitNode.visit();
        curBlock = null;
        curFunc = null;
        curSymbolTable = null;
    }

    public boolean checkError(List<ErrorNode> parserErrs, boolean isPrint) throws IOException {
        compUnitNode.check(highestTableForErr, 0);
        List<ErrorNode> allErrs = new ArrayList<>();
        allErrs.addAll(SynNode.errors);
        allErrs.addAll(parserErrs);
        Collections.sort(allErrs);
        if (isPrint) {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("error.txt"));
            int size = allErrs.size();
            for (int i = 0; i < size; i++) {
                osw.write(allErrs.get(i).toString());
                if (i != size - 1) {
                    osw.write("\n");
                }
            }
            osw.close();
        }
        return allErrs.size() == 0;
    }


}
