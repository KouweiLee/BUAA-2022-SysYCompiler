package frontend.preprocess;

import frontend.preprocess.synnodes.AddExpNode;
import frontend.preprocess.synnodes.BlockItemNode;
import frontend.preprocess.synnodes.BlockNode;
import frontend.preprocess.synnodes.CompUnitNode;
import frontend.preprocess.synnodes.DeclNode;
import frontend.preprocess.synnodes.DefNode;
import frontend.preprocess.synnodes.EqExpNode;
import frontend.preprocess.synnodes.ErrorNode;
import frontend.preprocess.synnodes.FuncDefNode;
import frontend.preprocess.synnodes.FuncFParamNode;
import frontend.preprocess.synnodes.FuncFParamsNode;
import frontend.preprocess.synnodes.FuncRParamsNode;
import frontend.preprocess.synnodes.InitValNode;
import frontend.preprocess.synnodes.LAndExpNode;
import frontend.preprocess.synnodes.LOrExpNode;
import frontend.preprocess.synnodes.LvalNode;
import frontend.preprocess.synnodes.MulExpNode;
import frontend.preprocess.synnodes.NumberNode;
import frontend.preprocess.synnodes.PrimaryExpNode;
import frontend.preprocess.synnodes.RelExpNode;
import frontend.preprocess.synnodes.StmtNode;
import frontend.preprocess.synnodes.Token;
import frontend.preprocess.synnodes.UnaryExpNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private List<Token> tokens;
    private int pos;//当前token位置
    private int lens;//tokens的长度
    private int preLine;
    private int nowLine;
    private CompUnitNode compUnitNode;
    private List<ErrorNode> errors;
    private ArrayList<String> outputs = new ArrayList<>();//输出要用

    public Parser(List<Token> tokens, boolean isPrint) throws IOException {
        this.tokens = tokens;
        this.pos = 0;
        this.lens = tokens.size();
        this.preLine = 0;
        this.nowLine = 0;
        errors = new ArrayList<>();
        compUnitNode = ((CompUnitNode) CompUnit());
        if (isPrint) {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("output.txt"));
            int sz = outputs.size();
            for (int i = 0; i < sz; i++) {
                osw.write(outputs.get(i));
                if (i != sz - 1) {
                    osw.write("\n");
                }
            }
            osw.close();
        }
    }

    public List<ErrorNode> getErrors(){
        return errors;
    }
    public CompUnitNode getCompUnitNode() {
        return compUnitNode;
    }

    private Token CompUnit() {//f
        CompUnitNode compUnit = new CompUnitNode();
        while (pos < lens && (getTokenType().equals(Def.Const) ||
                (getTokenType().equals(Def.Int) && getTokenType(1).equals(Def.Ident) && !getTokenType(2).equals(Def.Lsmall)))) {
            compUnit.addChildNode(Decl());
        }
        while (pos < lens && (getTokenType().equals(Def.VoidTK) ||
                (getTokenType().equals(Def.Int) && getTokenType(1).equals(Def.Ident)))) {
            compUnit.addChildNode(FuncDef());
        }
        compUnit.addChildNode(MainFuncDef());
        addVnOut("CompUnit");
        return compUnit;
    }

    private Token MainFuncDef() {
        FuncDefNode main = new FuncDefNode();
        main.addChildNode(newTerminal());//int
        main.addChildNode(newTerminal());//main
        main.addChildNode(newTerminal());//(
        checkRparent(main);
        main.addChildNode(Block());
        addVnOut("MainFuncDef");
        return main;
    }

    private Token Block() {
        BlockNode block = new BlockNode();
        block.addChildNode(newTerminal());//{
//        System.out.println(getTokenType());
        while (pos < lens && !getTokenType().equals(Def.Rbig)) {
//            System.out.println("109");
            block.addChildNode(BlockItem());
        }
        block.addChildNode(newTerminal());//}
        addVnOut("Block");
        return block;
    }

    private Token BlockItem() {
        BlockItemNode item = new BlockItemNode();
        if (getTokenType().equals(Def.Const) || getTokenType().equals(Def.Int)) {
            item.addChildNode(Decl());
        } else {
            item.addChildNode(Stmt());
        }
        return item;
    }

    private Token Stmt() {
        StmtNode stmt = new StmtNode();
        if (getTokenType().equals(Def.If)) {
            stmt.addChildNode(newTerminal());
            stmt.addChildNode(newTerminal());//(
            stmt.addChildNode(Cond());
            checkRparent(stmt);
            stmt.addChildNode(Stmt());
            if (getTokenType().equals(Def.Else)) {
                stmt.addChildNode(newTerminal());
                stmt.addChildNode(Stmt());
            }
        } else if (getTokenType().equals(Def.While)) {
            stmt.addChildNode(newTerminal());
            stmt.addChildNode(newTerminal());
            stmt.addChildNode(Cond());
            checkRparent(stmt);
            stmt.addChildNode(Stmt());
        } else if (getTokenType().equals(Def.Break) || getTokenType().equals(Def.Continue)) {
            stmt.addChildNode(newTerminal());
            checkSemicn(stmt);
        } else if (getTokenType().equals(Def.Return)) {
            stmt.addChildNode(newTerminal());
            if (isExp()) {
                stmt.addChildNode(Exp());
            }
            checkSemicn(stmt);
        } else if (getTokenType().equals(Def.Printf)) {
            stmt.addChildNode(newTerminal());//printf
            stmt.addChildNode(newTerminal());//(
            stmt.addChildNode(newTerminal());//FormatString
            while ((getTokenType().equals(Def.Comma))) {
//                stmt.addChildNode(newTerminal());
                nextToken();
                stmt.addChildNode(Exp());
            }
            checkRparent(stmt);
            checkSemicn(stmt);
        } else if (getTokenType().equals(Def.Lbig)) {
            stmt.addChildNode(Block());
        } else if (getTokenType().equals(Def.Semicn)) {
            stmt.addChildNode(newTerminal());
        } else {//exp或lval, 这里采用了回溯
            int pre = pos;
            int pre_sz = outputs.size();
            Token lval = LVal();
            if (getTokenType().equals(Def.Assign)) {
                //为Lval, 对应有Lval=exp; || Lval = getint();
                stmt.addChildNode(lval);
                stmt.addChildNode(newTerminal());//等号
                if (getTokenType().equals(Def.Getint)) {
                    stmt.addChildNode(newTerminal());
                    stmt.addChildNode(newTerminal());
                    checkRparent(stmt);
                    checkSemicn(stmt);
                } else {
                    stmt.addChildNode(Exp());
                    checkSemicn(stmt);
                }
            } else {
                pos = pre;
                int now_sz = outputs.size();
                for (int i = now_sz - 1; i >= pre_sz; i--) {
                    outputs.remove(i);
                }
                stmt.addChildNode(Exp());//这里必须有exp,之前已经考虑过没有的情况了
                checkSemicn(stmt);
            }
        }
        addVnOut("Stmt");
        return stmt;
    }

    private Token Cond() {
//        CondNode node = new CondNode();
        Token node = LOrExp();
        addVnOut("Cond");
        return node;
    }

    private Token LOrExp() {
        LOrExpNode node = new LOrExpNode();
        node.addChildNode(LAndExp());
        while (pos < lens && getTokenType().equals(Def.Or)) {
            addVnOut("LOrExp");
            node.addChildNode(newTerminal());
            node.addChildNode(LAndExp());
        }
        addVnOut("LOrExp");
        return node;
    }

    private Token LAndExp() {
        LAndExpNode node = new LAndExpNode();
        node.addChildNode(EqExp());
        while (pos < lens && getTokenType().equals(Def.And)) {
            addVnOut("LAndExp");
            node.addChildNode(newTerminal());
            node.addChildNode(EqExp());
        }
        addVnOut("LAndExp");
        return node;
    }

    private Token EqExp() {
        EqExpNode node = new EqExpNode();
        node.addChildNode(RelExp());
        while (pos < lens && (getTokenType().equals(Def.Eql) || getTokenType().equals(Def.Neq))) {
            addVnOut("EqExp");
            node.addChildNode(newTerminal());
            node.addChildNode(RelExp());
        }
        addVnOut("EqExp");
        return node;
    }

    private Token RelExp() {
        RelExpNode node = new RelExpNode();
        node.addChildNode(AddExp());
        while (pos < lens && (getTokenType().equals(Def.Geq) || getTokenType().equals(Def.Gre) ||
                getTokenType().equals(Def.Lss) || getTokenType().equals(Def.Leq))) {
            addVnOut("RelExp");
            node.addChildNode(newTerminal());
            node.addChildNode(AddExp());
        }
        addVnOut("RelExp");
        return node;
    }

    private boolean isExp() {
        String type = getTokenType();
        return type.equals(Def.Lsmall) || type.equals(Def.Ident) || type.equals(Def.NumberInt) ||
                type.equals(Def.Add) || type.equals(Def.Sub) || type.equals(Def.Not);
    }

    private Token FuncDef() {
        FuncDefNode node = new FuncDefNode();
        node.addChildNode(FuncType());
        node.addChildNode(newTerminal());
        node.addChildNode(newTerminal());
        if (!getTokenType().equals(Def.Rsmall) && !getTokenType().equals(Def.Lbig)) node.addChildNode(FuncFParams());
//        System.out.println(getTokenType());
        checkRparent(node);
        node.addChildNode(Block());
        addVnOut("FuncDef");
        return node;
    }

    private Token FuncType() {
        Token node = newTerminal();
        addVnOut("FuncType");
        return node;
    }

    private Token FuncFParams() {
        FuncFParamsNode node = new FuncFParamsNode();
        node.addChildNode(FuncFParam());
        while (getTokenType().equals(Def.Comma)) {
//            node.addChildNode(newTerminal());
            nextToken();
            node.addChildNode(FuncFParam());
        }
        addVnOut("FuncFParams");
        return node;
    }

    private Token FuncFParam() {
        FuncFParamNode funcFParam = new FuncFParamNode();
        funcFParam.addChildNode(BType());
        funcFParam.addChildNode(newTerminal());
        if (getTokenType().equals(Def.Lmedium)) {
            funcFParam.addChildNode(newTerminal());
            checkRbrack(funcFParam);
            while (getTokenType().equals(Def.Lmedium)) {
                funcFParam.addChildNode(newTerminal());
                funcFParam.addChildNode(ConstExp());
                checkRbrack(funcFParam);
            }
        }
        addVnOut("FuncFParam");
        return funcFParam;
    }

    private Token BType() {
        Token node = newTerminal();//int
        return node;
    }

    private Token Decl() {
        if (getTokenType().equals(Def.Const)) {
            return ConstDecl();
        }else {
            return VarDecl();
        }
    }

    private Token VarDecl() {
        DeclNode node = new DeclNode();
        node.addChildNode(BType());
        node.addChildNode(VarDef());
        while (pos < lens && getTokenType().equals(Def.Comma)) {
            nextToken();
            node.addChildNode(VarDef());
        }
        checkSemicn(node);
        addVnOut("VarDecl");
        return node;
    }

    private Token VarDef() {
        DefNode node = new DefNode(false);
        node.addChildNode(newTerminal());
        while (pos < lens && getTokenType().equals(Def.Lmedium)) {
            node.addChildNode(newTerminal());
            node.addChildNode(ConstExp());
            checkRbrack(node);
        }
        if (getTokenType().equals(Def.Assign)) {
            node.addChildNode(newTerminal());
            node.addChildNode(InitVal());
        }
        addVnOut("VarDef");
        return node;
    }

    private Token InitVal() {
        InitValNode node = new InitValNode();
        if (getTokenType().equals(Def.Lbig)) {
            node.addChildNode(newTerminal());
            node.addChildNode(InitVal());
            while (pos < lens && getTokenType().equals(Def.Comma)) {
//                node.addChildNode(newTerminal());
                nextToken();
                node.addChildNode(InitVal());
            }
            node.addChildNode(newTerminal());
        } else {
            node.addChildNode(Exp());
        }
        addVnOut("InitVal");
        return node;
    }

    private Token ConstDecl() {
        DeclNode constDecl = new DeclNode();
        constDecl.setConstDecl(true);
        constDecl.addChildNode(newTerminal());//const
        constDecl.addChildNode(BType());//int
        constDecl.addChildNode(ConstDef());
        while (pos < lens && getTokenType().equals(Def.Comma)) {
            nextToken();//逗号
            constDecl.addChildNode(ConstDef());
        }
        checkSemicn(constDecl);
        //正常, 则分号的下一个节点; 缺少分号, 则不执行nextToken
        addVnOut("ConstDecl");
        return constDecl;
    }

    private Token ConstDef() {
        DefNode constDef = new DefNode(true);
        constDef.addChildNode(newTerminal());
        while (pos < lens && getTokenType().equals(Def.Lmedium)) {
            constDef.addChildNode(newTerminal());
            constDef.addChildNode(ConstExp());
            checkRbrack(constDef);
        }
        constDef.addChildNode(newTerminal());//assign
        constDef.addChildNode(ConstInitVal());
        addVnOut("ConstDef");
        return constDef;
    }

    private Token ConstInitVal() {
        InitValNode node = new InitValNode();
        if (getTokenType().equals(Def.Lbig)) {
            node.addChildNode(newTerminal());//{
            if (!getTokenType().equals(Def.Rbig)) {
                node.addChildNode(ConstInitVal());
                while (getTokenType().equals(Def.Comma)) {
                    nextToken();
                    node.addChildNode(ConstInitVal());
                }
            }
            node.addChildNode(newTerminal());//}
        } else {
            node.addChildNode(ConstExp());
        }
        addVnOut("ConstInitVal");
        return node;
    }

    private Token ConstExp() {
        //ConstExpNode constExp = new ConstExpNode();
        //constExp.addChildNode(AddExp());
        Token constExp = AddExp();
        addVnOut("ConstExp");
        return constExp;
    }

    private Token AddExp() {
        AddExpNode addExp = new AddExpNode();
        addExp.addChildNode(MulExp());
        while (pos < lens && (getTokenType().equals(Def.Add) || getTokenType().equals(Def.Sub))) {
            addVnOut("AddExp");
            addExp.addChildNode(newTerminal());//+|-
            addExp.addChildNode(MulExp());
        }
        addVnOut("AddExp");
        return addExp;
    }

    private Token MulExp() {
        MulExpNode mulExp = new MulExpNode();
        mulExp.addChildNode(UnaryExp());
        while (pos < lens && (getTokenType().equals(Def.Mult) || getTokenType().equals(Def.Div) ||
                getTokenType().equals(Def.Mod))) {
            addVnOut("MulExp");
            mulExp.addChildNode(newTerminal());//*|/|%
            mulExp.addChildNode(UnaryExp());
        }
        addVnOut("MulExp");
        return mulExp;
    }

    private Token UnaryExp() {
        UnaryExpNode unaryExp = new UnaryExpNode();
        if (getTokenType().equals(Def.Ident) && getTokenType(1).equals(Def.Lsmall)) {//Indet (FuncParams)
            unaryExp.addChildNode(newTerminal());//Ident
            unaryExp.addChildNode(newTerminal());//(
            if (!getTokenType().equals(Def.Rsmall) && isExp()) unaryExp.addChildNode(FuncRParams());
            checkRparent(unaryExp);
        } else if (isUnaryOp()) {
            int outnum = 0;
            while (isUnaryOp()){
                outnum++;
                unaryExp.addChildNode(UnaryOp());
            }
            unaryExp.addChildNode(UnaryExp());
            while (outnum!=0){
                outnum--;
                addVnOut("UnaryExp");
            }
            return unaryExp;
        } else {
            unaryExp.addChildNode(PrimaryExp());
        }
        addVnOut("UnaryExp");
        return unaryExp;
    }
    private boolean isUnaryOp(){
        return getTokenType().equals(Def.Add) ||
                getTokenType().equals(Def.Sub) || getTokenType().equals(Def.Not);
    }
    private Token UnaryOp() {
        Token unaryOp = newTerminal();
        addVnOut("UnaryOp");
        return unaryOp;
    }

    private Token PrimaryExp() {
        PrimaryExpNode primaryExp = new PrimaryExpNode();
        if (getTokenType().equals(Def.Lsmall)) {
            primaryExp.addChildNode(newTerminal());
            primaryExp.addChildNode(Exp());
            checkRparent(primaryExp);
        } else if (getTokenType().equals(Def.Ident)) {
            primaryExp.addChildNode(LVal());
        } else {
            primaryExp.addChildNode(Number());
        }
        addVnOut("PrimaryExp");
        return primaryExp;
    }

    private Token Number() {
        Token token = newTerminal();
//        System.out.println(token.getContent());
        NumberNode number = new NumberNode(Integer.parseInt(token.getContent()));
        addVnOut("Number");
        return number;
    }

    private Token LVal() {
        LvalNode lval = new LvalNode();
        lval.addChildNode(newTerminal());
        while (getTokenType().equals(Def.Lmedium)) {
            lval.addChildNode(newTerminal());
            lval.addChildNode(Exp());
            checkRbrack(lval);
        }
        addVnOut("LVal");
        return lval;
    }

    private Token Exp() {
        Token exp = AddExp();
        addVnOut("Exp");
        return exp;
    }

    private Token FuncRParams() {
        FuncRParamsNode funcRParams = new FuncRParamsNode();
        funcRParams.addChildNode(Exp());
        while (pos < lens && getTokenType().equals(Def.Comma)) {
//            funcRParams.addChildNode(newTerminal());
            nextToken();
            funcRParams.addChildNode(Exp());
        }
        addVnOut("FuncRParams");
        return funcRParams;
    }

    private void nextToken() {
        if (pos < lens) {
            outputs.add(tokens.get(pos).toString());
            preLine = tokens.get(pos).getLine();
            pos += 1;
            nowLine = pos == lens ? nowLine : tokens.get(pos).getLine();
        }
    }

    private Token newTerminal() {//将当前Token转换为语法树节点
        Token syn = tokens.get(pos);
        nextToken();
        return syn;
    }

    private void addVnOut(String s) {//非终结符输出
        outputs.add("<" + s + ">");
    }

    //    private Token newVnNode(String type){//增加非终结符
//        return new Token(type, null, null);
//    }
    private void checkSemicn(Token node) {
        if (tokenIsNot(Def.Semicn)) {
            ErrorNode err = new ErrorNode(preLine, ErrorNode.ECode.i,
                    node.getClass().getName() + " lacks ; in line " + preLine);
            node.addChildNode(err);
            errors.add(err);
        } else node.addChildNode(newTerminal());

    }

    private void checkRparent(Token node) {
        if (tokenIsNot(Def.Rsmall)) {
            ErrorNode err = new ErrorNode(preLine, ErrorNode.ECode.j,
                    node.getClass().getName() + " lacks ) in line " + preLine);
            node.addChildNode(err);
            errors.add(err);
        } else node.addChildNode(newTerminal());

    }

    /**
     * 检查是否为右括号 ，同时将括号加入到node的子节点中
     */
    private void checkRbrack(Token node) {
        if (tokenIsNot(Def.Rmedium)) {
            ErrorNode err = new ErrorNode(preLine, ErrorNode.ECode.k,
                    node.getClass().getName() + " lacks ] in line " + preLine);
            node.addChildNode(err);
            errors.add(err);
        } else node.addChildNode(newTerminal());
    }

    private String getTokenType(int off) {
        return tokens.get(pos + off).getType();
    }

    private String getTokenType() {
        return tokens.get(pos).getType();
    }

    private boolean tokenIsNot(String type) {
        return !getTokenType().equals(type);
    }
}
