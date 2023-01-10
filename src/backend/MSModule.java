package backend;

import frontend.irgenerate.Initial;
import frontend.irgenerate.IrModule;
import frontend.irgenerate.Type;
import frontend.irgenerate.irclass.BasicBlock;
import frontend.irgenerate.irclass.Function;
import frontend.irgenerate.irclass.GlobalValue;
import utils.IOhelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MSModule {
    public static final MSModule msModule = new MSModule();
    private IrModule irModule;
    private ArrayList<MSGlobalValue> msglobalValues;
    private ArrayList<MSFunction> msfunctions;
    public static MSFunction msNowFunc;
    public static MSBlock msNowBlock;
    public static final HashMap<BasicBlock, MSBlock> lvBB2MSBB = new HashMap<>();

    private MSModule() {
        msglobalValues = new ArrayList<>();
        msfunctions = new ArrayList<>();
        irModule = IrModule.module;
    }

    public void parseLLvmModule() {
        //全局量
        List<GlobalValue> lvGlobalValues = irModule.getGlobalValues();
        for (GlobalValue lvGlobalValue : lvGlobalValues) {
            String name = lvGlobalValue.getOnlyName();
            Initial initial = lvGlobalValue.getInitial();
            MSGlobalValue msGlobalValue = null;
            //字符串
            if (lvGlobalValue.getGlobalType().equals("string")) {
//                System.out.println(name);
                msGlobalValue = new MSGlobalValue(MSGlobalValue.str, name, initial.getValue());
            }else if(lvGlobalValue.getGlobalType().equals("array")){
                //数组
                int size = ((Type.ArrayType) initial.getType()).getSize();
                msGlobalValue = new MSGlobalValue(MSGlobalValue.array, name, initial, size);
            }else {
                //普通变量
                msGlobalValue = new MSGlobalValue(MSGlobalValue.word, name, initial.getValue());
            }
            msglobalValues.add(msGlobalValue);
        }
        //普通函数
        List<Function> functions = irModule.getFunctions();
        for (Function function : functions) {
            if (!function.isExternal())
                msfunctions.add(new MSFunction(function, false));
        }
        MSFunction mainFunc = new MSFunction(irModule.getMainFunction(), true);
        msfunctions.add(mainFunc);
        msNowBlock = null;
        msNowFunc = null;
    }

    public void outputMips(String filename) throws IOException {
        IOhelper.clear();
        IOhelper.addOutput(".data");
        for (MSGlobalValue msglobalValue : msglobalValues) {
            IOhelper.addOutput(msglobalValue.toString());
        }
        IOhelper.addOutput(".text\nj func_main\n");
        for (MSFunction msfunction : msfunctions) {
            IOhelper.addOutput(msfunction.toString());
        }
        IOhelper.output(filename);
    }

    public ArrayList<MSFunction> getMsfunctions() {
        return msfunctions;
    }
}
