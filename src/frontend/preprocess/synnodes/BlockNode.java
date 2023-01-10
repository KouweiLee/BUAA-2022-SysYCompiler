package frontend.preprocess.synnodes;

import frontend.irgenerate.SymbolTable;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.ESymbolTable;
import frontend.preprocess.Visitor;

public class BlockNode extends Token {
    private boolean passSymTableSon = false;
    private int checkReturnCode = 0;

    public BlockNode() {
    }

    /**
     * 对于一个 函数的第一个语句块，会包括参数信息，为true
     * @param flag
     */
    public void setPassSymTableSon(boolean flag) {
        passSymTableSon = flag;
    }

    @Override
    public Value visit() {
        if (!passSymTableSon) {
            Visitor.curSymbolTable = Visitor.curSymbolTable.newSon();
        }
        int size = sons.size();
        for (int i = 1; i < size; i++) {
            getSon(i).visit();
        }
        Visitor.curSymbolTable = Visitor.curSymbolTable.getFather();
        return null;
    }

    //判断函数结尾是否有返回值
    private boolean haveValReturn() {
        Token blockItem = getSon(-2);
        if (blockItem instanceof BlockItemNode) {
            if (blockItem.getSon(0) instanceof StmtNode) {
                StmtNode stmt = ((StmtNode) blockItem.getSon(0));
                if ("return".equals(stmt.getSon(0).getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param code 为1表示返回值类型为void, 为2表示返回值类型为int
     */
    public void setCheckReturnCode(int code) {
        checkReturnCode = code;
    }

    @Override
    public void check(ESymbolTable eSymbolTable, int loopCycles) {
        /**
         * checkReturnCode: 0, 不检查返回值; 1, 应该返回值为void; 2, 函数结尾应该为return
         */
        int size = sons.size();
        boolean checkVoidReturn = checkReturnCode == 1;
        if (!passSymTableSon) {
            eSymbolTable = eSymbolTable.newSon();
        }
        for (int i = 1; i < size - 1; i++) {
            ((BlockItemNode) sons.get(i)).check(eSymbolTable, loopCycles, checkVoidReturn);
        }
        if (checkReturnCode == 2 && !haveValReturn()) {
            int tline = getSon(-1).getLine();//}所在行号
            errors.add(new ErrorNode(tline, ErrorNode.ECode.g, "Function needs return value in line " + tline));
        }
    }
}
