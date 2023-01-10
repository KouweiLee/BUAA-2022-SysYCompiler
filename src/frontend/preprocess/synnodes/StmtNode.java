package frontend.preprocess.synnodes;

import frontend.irgenerate.Initial;
import frontend.irgenerate.IrModule;
import frontend.irgenerate.instr.Call;
import frontend.irgenerate.instr.GetElementPtr;
import frontend.irgenerate.instr.Jump;
import frontend.irgenerate.instr.Return;
import frontend.irgenerate.instr.Store;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.GlobalValue;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.ESymbolTable;
import frontend.preprocess.VarSymbol;
import frontend.preprocess.Visitor;

import java.util.ArrayList;
import java.util.List;

public class StmtNode extends Token {
    private int type = -1;
    private static int Str_num = 1;

    public StmtNode() {
    }

    public Value visit() {
        if (type == 0) {//Lval=exp
            Value lval = getSon(0).visit();
            Value exp = getSon(2).visit();
            new Store(exp, lval);
        } else if (type == 1) {//addExp
            getSon(0).visit();
        } else if (type == 2) {//block
            BlockNode blockNode = (BlockNode) getSon(0);
            blockNode.visit();
        } else if (type == 3) {//if
            return visitIfStmt();
        } else if (type == 4) {//while
            return visitWhileStmt();
        } else if (type == 5) {//break or continue
            if (getSon(0).getName().equals("break")) {
                new Jump(Visitor.visitor.loopFollows.peek());
            } else {//continue
                new Jump(Visitor.visitor.loopConds.peek());
            }
        } else if (type == 6) {//return
            if (getSon(1) instanceof AddExpNode) {
                Value exp = getSon(1).visit();
                new Return(exp);
            } else {
                new Return();
            }
        } else if (type == 7) {//lval = getint()
            Value lval = getSon(0).visit();
            Value getint = new Call(IrModule.module.getFunction("getint"), null);
            new Store(getint, lval);
        } else if (type == 8) {//printf
            String formatString = getSon(2).getContent();
            int idx = 0, lens = formatString.length();
            List<Value> exps = new ArrayList<>();
            for (int i = 3; i < sons.size() - 1; i++) {//遍历所有exp
                if (getSon(i) instanceof AddExpNode) {
                    exps.add(getSon(i).visit());
                }
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < lens - 1; i++) {
                if ((formatString.charAt(i) == '%') && (formatString.charAt(i + 1) == 'd')) {
                    if (sb.length() != 0) {
                        outputStr(sb.toString());
                        sb = new StringBuilder();
                    }
                    ArrayList<Value> arrayList = new ArrayList<>();
                    arrayList.add(exps.get(idx++));
                    new Call("putint", arrayList);
                    i++;
                } else {
                    if ((formatString.charAt(i) == '\\') && (formatString.charAt(i + 1) == 'n')) {
                        sb.append("\n");
                        i = i + 1;
                    } else {
                        sb.append(formatString.charAt(i));
                    }
                }

            }
            outputStr(sb.toString());
        }
        return null;
    }

    private void outputStr(String string) {
        if (string.length() == 0) {
            return;
        }
        ArrayList<Value> arrayList = new ArrayList<>();
        Constant.ConstantStr str = new Constant.ConstantStr(string);
        GlobalValue pointer = new GlobalValue(new Initial.ValueInitial(str), "str" + Str_num++, str.getType().toPointerType(), true);
        IrModule.module.addGlobalValue(pointer);
        GetElementPtr ptr = new GetElementPtr(pointer, Constant.ConstantInt.ZERO);
        arrayList.add(ptr);
        new Call("putstr", arrayList);
    }

    private Value visitWhileStmt() {
        LOrExpNode cond = (LOrExpNode) getSon(2);
        StmtNode stmt = (StmtNode) getSon(-1);
        BasicBlock condBlock = new BasicBlock(Visitor.curFunc);
        BasicBlock loopBlock = new BasicBlock(Visitor.curFunc);
        BasicBlock endBlock = new BasicBlock(Visitor.curFunc);
        new Jump(condBlock);
        Visitor.curBlock = condBlock;
        cond.visitLOrExpNode(loopBlock, endBlock);//满足条件在loop, 不满足退出循环
        Visitor.visitor.loopConds.push(condBlock);
        Visitor.visitor.loopFollows.push(endBlock);
        //解析循环体
        Visitor.curBlock = loopBlock;
        stmt.visit();
        new Jump(condBlock);
        //结束循环, 出栈
        Visitor.visitor.loopConds.pop();
        Visitor.visitor.loopFollows.pop();
        Visitor.curBlock = endBlock;
        return null;
    }

    private Value visitIfStmt() {
        BasicBlock trueBlock = new BasicBlock(Visitor.curFunc);
        BasicBlock falseBlock = null;
        boolean hasElse = false;
        if (5 < sons.size()) {//存在else
            hasElse = true;
            falseBlock = new BasicBlock(Visitor.curFunc);
        }
        BasicBlock followBlock = new BasicBlock(Visitor.curFunc);
        LOrExpNode cond = (LOrExpNode) getSon(2);
        if (hasElse) {
            cond.visitLOrExpNode(trueBlock, falseBlock);
            Visitor.curBlock = trueBlock;
            getSon(4).visit();//true stmt
            new Jump(followBlock);//从true块跳到follow块
            Visitor.curBlock = falseBlock;
            getSon(6).visit();//false stmt
        } else {
            cond.visitLOrExpNode(trueBlock, followBlock);
            Visitor.curBlock = trueBlock;
            getSon(4).visit();
        }
//        Visitor.curBlock = trueBlock;
        new Jump(followBlock);//从true块跳到follow块
        //TODO:是否需要从false跳到trueblock
        Visitor.curBlock = followBlock;
        return null;
    }

    /**
     * @param isCheck 为true表示检查：无返回值的函数是否存在不匹配的 return语句
     */
    public void check(ESymbolTable eSymbolTable, int loopCycles, boolean isCheck) {
        int pos = 0;
        if (getSon(pos) instanceof LvalNode) {
            getSon(pos).check(eSymbolTable, loopCycles);
            VarSymbol varSymbol = ((LvalNode) getSon(pos)).getVarSymbol();
            if (varSymbol != null && varSymbol.isConst()) {
                Token ident = getSon(0).getSon(0);
                errors.add(new ErrorNode(ident.getLine(), ErrorNode.ECode.h, "cannot change const in line" + ident.getLine()));
            }
            pos += 2;
            if (getSon(pos) instanceof AddExpNode) {
                type = 0;
                getSon(pos).check(eSymbolTable, loopCycles);
            } else {
                type = 7;
            }
        } else if (getSon(pos) instanceof AddExpNode) {
            type = 1;
            getSon(pos).check(eSymbolTable, loopCycles);
        } else if (getSon(pos) instanceof BlockNode) {
            type = 2;
            int code = isCheck ? 1 : 0;
            ((BlockNode) getSon(pos)).setCheckReturnCode(code);
            ((BlockNode) getSon(pos)).check(eSymbolTable, loopCycles);
        } else if ("if".equals(getSon(pos).getName())) {
            type = 3;
            pos += 2;
            getSon(pos).check(eSymbolTable, loopCycles);
            pos += 2;
            ((StmtNode) getSon(pos)).check(eSymbolTable, loopCycles, isCheck);
            pos++;
            if (pos < sons.size() && "else".equals(getSon(pos).getName())) {
                ((StmtNode) getSon(++pos)).check(eSymbolTable, loopCycles, isCheck);
            }
        } else if ("while".equals(getSon(pos).getName())) {
            type = 4;
            pos += 2;
            getSon(pos).check(eSymbolTable, loopCycles);
            pos += 2;
            ((StmtNode) getSon(pos)).check(eSymbolTable, loopCycles + 1, isCheck);
        } else if ("break".equals(getSon(pos).getName()) || "continue".equals(getSon(pos).getName())) {
            type = 5;
            if (loopCycles == 0) {
                errors.add(new ErrorNode(getSon(pos).getLine(),
                        ErrorNode.ECode.m, "cannot use break or continue in no-while stmt in line " + getSon(pos).getLine()));
            }
        } else if ("return".equals(getSon(pos).getName())) {
            type = 6;
            int line = getSon(pos).getLine();
            pos++;
            if (getSon(pos) instanceof AddExpNode) {
                if (isCheck) {//应当无返回值
                    errors.add(new ErrorNode(line, ErrorNode.ECode.f, "void function should not return exp in line " + line));
                } else {
                    getSon(pos).check(eSymbolTable, loopCycles);
                }
            }
        } else if ("printf".equals(getSon(pos).getName())) {
            type = 8;
            int line = getSon(pos).getLine();
            pos += 2;
            int paramNum = getSon(pos).checkFormatString();
            int realParamNum = 0;
            pos++;
            while (pos < sons.size() && getSon(pos) instanceof AddExpNode) {
                getSon(pos).check(eSymbolTable, loopCycles);
                pos++;
                realParamNum++;
            }
            if (paramNum != -1 && realParamNum != paramNum)
                errors.add(new ErrorNode(line, ErrorNode.ECode.l, "formatString not corresponds with num of exp in line " + line));
        }
    }

}
