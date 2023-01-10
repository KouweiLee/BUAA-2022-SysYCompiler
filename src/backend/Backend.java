package backend;

import frontend.irgenerate.IrModule;

import java.io.IOException;

public class Backend {
    private MSModule msModule;
    private IrModule llvmModule;
    private boolean debug = true;
    public Backend(IrModule lvModule) throws IOException {
        this.llvmModule = lvModule;
        this.msModule = MSModule.msModule;
        msModule.parseLLvmModule();
        if(debug){
            msModule.outputMips("mips_vir.txt");
        }
        RegAllocator regAllocator = new RegAllocator();
        regAllocator.allocTempRegs();
    }

    public void outputMips(String filename) throws IOException {
        msModule.outputMips(filename);
    }


}
