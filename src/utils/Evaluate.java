package utils;

import frontend.irgenerate.Initial;
import frontend.irgenerate.Symbol;
import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.Value;
import frontend.preprocess.Visitor;
import frontend.preprocess.synnodes.AddExpNode;
import frontend.preprocess.synnodes.LvalNode;
import frontend.preprocess.synnodes.NumberNode;
import frontend.preprocess.synnodes.PrimaryExpNode;
import frontend.preprocess.synnodes.Token;

import java.util.List;

/**
 * 编译期求值工具类
 */
public class Evaluate {

    public static int evaluateAddExp(AddExpNode exp) {
        List<Token> sons = exp.getSons();
        int result = evaluateMulExp(sons.get(0));
        for (int i = 1; i < sons.size(); i++) {
            Token op = sons.get(i++);
            int second = evaluateMulExp(sons.get(i));
            result = caculateInt(op, result, second);
        }
        return result;
    }

    private static int evaluateMulExp(Token exp) {
        List<Token> sons = exp.getSons();
        int result = evaluateUnaryExp(sons.get(0));
        for (int i = 1; i < sons.size(); i++) {
            Token op = sons.get(i++);
            int second = evaluateUnaryExp(sons.get(i));
            result = caculateInt(op, result, second);
        }
        return result;
    }

    private static int evaluatePrimaryExp(Token exp) {
        Token son = exp.getSon(0);
        if (son instanceof NumberNode) {
            return ((NumberNode) son).getNumber();
        } else if (son instanceof LvalNode) {
            //TODO: 加入数组
            List<Token> sons = son.getSons();
            Symbol symbol = (Symbol) Visitor.visitor.getSymbol(sons.get(0).getName());
            Initial init = symbol.getInit();
            for(int i=1; i<sons.size(); i++){
                if(son.getSon(i) instanceof AddExpNode){
                    int index = Evaluate.evaluateAddExp((AddExpNode) son.getSon(i));
                    init = ((Initial.ArrayInitial) init).getInitials().get(index);
                }
            }
            return ((Constant.ConstantInt) init.getValue()).getNum();
        } else {
            return evaluateAddExp((AddExpNode) exp.getSon(1));
        }
    }

    private static int evaluateUnaryExp(Token exp) {
        if (exp instanceof PrimaryExpNode) {
            return evaluatePrimaryExp(exp);
        } else {
            int result = evaluateUnaryExp(exp.getSon(-1));
            List<Token> sons = exp.getSons();
            int size = sons.size();
            for (int i = 0; i < size - 1; i++) {
                if (sons.get(i).getContent().equals("-")) {
                    result = -result;
                }
            }
            return result;
        }
    }

    public static Value caculate(Token op, Value value1, Value value2) {
        Constant.ConstantInt first = (Constant.ConstantInt) value1;
        Constant.ConstantInt second = (Constant.ConstantInt) value2;
        int caculate = caculateInt(op, first.getNum(), second.getNum());
        return new Constant.ConstantInt(caculate);
    }

    public static int caculateInt(Token op, int left, int right) {
        switch (op.getContent()) {
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "*":
                return left * right;
            case "/":
                return left / right;
            case "%":
                return left % right;
            default:
                return 0;
        }
    }
}
