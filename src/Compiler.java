import backend.Backend;
import frontend.irgenerate.IrModule;
import midEnd.MidEnd;
import utils.IOhelper;
import frontend.preprocess.Lexer;
import frontend.preprocess.Parser;
import frontend.preprocess.Visitor;

import java.io.IOException;

public class Compiler {
    private static boolean isDebug = false;
    private static boolean parserOut = false;
    private static boolean openMidEnd = true;
    private static boolean openBackEnd = true;
    public static boolean openGlobalReg = true;
    public static void main(String[] args) throws IOException {
        if (isDebug) {
            parserOut = true;
        }
        Lexer lexer = new Lexer(IOhelper.getInput(), false);
        Parser parser = new Parser(lexer.getTokens(), false);
        Visitor visitor = Visitor.visitor;
        visitor.setCompUnitNode(parser.getCompUnitNode());
        boolean isRight = visitor.checkError(parser.getErrors(), true);
        if (!isRight) {
            System.out.println("somewhere is wrong");
            if (!isDebug)
                return;
        }
        visitor.visit();
        //输出未优化代码
        IrModule.module.outputLLVM("llvm_ir_O0.txt");
        MidEnd midEnd = new MidEnd(IrModule.module.getDefFunctions());
        if(openMidEnd){
            midEnd.run(true, openGlobalReg);
        }
        //输出优化完成的中间代码
        IrModule.module.outputLLVM("llvm_ir.txt");

        //后端开始
        if(openBackEnd){
//            midEnd.removePhi();
            Backend backend = new Backend(IrModule.module);
            backend.outputMips("mips.txt");
        }
    }
}
