package frontend.preprocess.synnodes;

import frontend.irgenerate.irclass.Value;
import frontend.preprocess.ESymbolTable;

import java.util.ArrayList;
import java.util.List;

public interface SynNode {
    List<ErrorNode> errors = new ArrayList<>();

    public void check(ESymbolTable ESymbolTable, int loopCycles);

    public Value visit();
}
