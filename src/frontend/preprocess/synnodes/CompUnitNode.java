package frontend.preprocess.synnodes;

import frontend.irgenerate.irclass.Value;
import frontend.preprocess.ESymbolTable;

public class CompUnitNode extends Token implements SynNode {

    public CompUnitNode(){
    }

    @Override
    public Value visit(){
        for(SynNode son : sons){
            son.visit();
        }
        return null;
    }

    public void check(ESymbolTable ESymbolTable, int loopCycles){
        for(SynNode son : sons){
            son.check(ESymbolTable, loopCycles);
        }
    }

}
