package frontend.preprocess.synnodes;

import frontend.irgenerate.IrModule;
import frontend.irgenerate.Symbol;
import frontend.irgenerate.SymbolTable;
import frontend.irgenerate.Type;
import frontend.irgenerate.instr.Alloca;
import frontend.irgenerate.instr.Return;
import frontend.irgenerate.instr.Store;
import frontend.irgenerate.irclass.Argument;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Function;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Def;
import frontend.preprocess.FuncSymbol;
import frontend.preprocess.ESymbolTable;
import frontend.preprocess.VarSymbol;
import frontend.preprocess.Visitor;

import java.util.ArrayList;
import java.util.List;

public class FuncDefNode extends Token {
    private FuncSymbol funcSymbol;

    public FuncDefNode() {
    }

    @Override
    public Value visit(){
        Visitor.isGlobal = false;
        String type = getSon(0).getType();
        Type retType = type.equals(Def.VoidTK) ? Type.VoidType.VOID_TYPE : Type.BasicType.I32;
        String name = getSon(1).getContent();
        SymbolTable symbolTable = Visitor.curSymbolTable.newSon();
        Visitor.curSymbolTable = symbolTable;
        Visitor.curBlock = new BasicBlock();
        List<Argument> arguments = new ArrayList<>();
        Function function = new Function(retType, arguments, name);
        Visitor.curFunc = function;
        Visitor.curBlock.setFunction(function);
        if(getSon(3) instanceof FuncFParamsNode){
            FuncFParamsNode funcFParams = (FuncFParamsNode) getSon(3);
             arguments = funcFParams.getArguments();
             function.addArguments(arguments);
            for (Argument argument : arguments) {
                Type argument_Type = argument.getType();
                Value pointer = new Alloca(argument_Type);
                symbolTable.addSymbol(new Symbol(argument.getCode_name(), argument.getType(), pointer, null, false));
                new Store(argument, pointer);
            }
        }
        if(name.equals("main")){
            IrModule.module.setMainFunction(function);
        }else {
            IrModule.module.addFunction(function);
        }
//        function.insertBlockEnd(Visitor.curBlock);
        BlockNode blockNode = (BlockNode) getSon(-1);
        blockNode.setPassSymTableSon(true);
        blockNode.visit();
        if((Visitor.curBlock.getLastInstr() == null) || (!Visitor.curBlock.getLastInstr().isTerminator())){//没有返回值语句加上一条
            new Return();
        }
        Visitor.curBlock = null;
        Visitor.curFunc = null;
        return null;
    }

    public void check(ESymbolTable eSymbolTable, int loopCycles) {
        int pos = 0;
        String returnType = getSon(pos++).getType();
        String funcName = getSon(pos).getName();
        int line = getSon(pos).getLine();
        pos++;
        pos++;
        ESymbolTable eSymbolTableSon = eSymbolTable.newSon();
        List<VarSymbol> params;
        if (getSon(pos) instanceof FuncFParamsNode) {
            ((FuncFParamsNode) getSon(pos)).check(eSymbolTableSon, loopCycles);
            params = ((FuncFParamsNode) getSon(pos)).getParams();
            pos++;
        }else {
            params = new ArrayList<>();
        }
        this.funcSymbol = new FuncSymbol(funcName, returnType, line, params);
        ErrorNode err = eSymbolTable.addSymbol(funcSymbol);
        if(err != null){
            errors.add(err);
        }
        pos++;
        BlockNode block = (BlockNode) getSon(pos);
        block.setPassSymTableSon(true);
        int returnCode = returnType.equals(Def.VoidTK) ? 1 : 2;
        block.setCheckReturnCode(returnCode);
        block.check(eSymbolTableSon, loopCycles);
    }
}
