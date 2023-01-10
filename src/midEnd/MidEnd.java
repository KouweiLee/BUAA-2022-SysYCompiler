package midEnd;

import frontend.irgenerate.IrModule;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Function;

import java.io.IOException;
import java.util.List;

/**
 * 中端优化类, 优化了:
 * 1.
 */
public class MidEnd {
    //不包含外部函数的所有函数
    private List<Function> functions;

    public MidEnd(List<Function> functions) {
        this.functions = functions;
    }

    public void run(boolean O1, boolean openGlobalReg) throws IOException {
        MakeDFG makeDFG = new MakeDFG(functions);
        makeDFG.run();
        if (O1) {
            Mem2Reg mem2Reg = new Mem2Reg(functions);
//            IrModule.module.outputLLVM("llvm_ir_O0.txt");
            GVN gvn = new GVN(functions);
            MidRegAllocate midRegAllocate = new MidRegAllocate(functions);
//            IrModule.module.outputLLVM("llvm_ir_O1.txt");
            RemovePhi removePhi = new RemovePhi(functions);
            MergeBlock mergeBlock = new MergeBlock(functions);
//            if(openGlobalReg)
//                makeDFG.run();
        }
    }
}
