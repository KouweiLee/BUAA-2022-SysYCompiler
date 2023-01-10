package frontend.preprocess.synnodes;

import frontend.irgenerate.irclass.Value;
import frontend.preprocess.ESymbolTable;

import java.util.ArrayList;
import java.util.List;

public class Token implements SynNode {
    //当type为ERROR时表示出错，没有这个单词
    private int line;
    private String type;//Token的种类
    private String content;//若为FormatString, 则包括双引号
    protected List<Token> sons;

    public Token(String type, String content, int line) {
        this.line = line;
        this.type = type;
        this.content = content;
    }

    public Token() {
        this.sons = new ArrayList<>();
        this.content = null;
        this.type = null;
    }

    public List<Token> getSons() {
        return sons;
    }

    public void addChildNode(Token node) {
        sons.add(node);
    }

    @Override
    public String toString() {
        return type + " " + content;
    }

    public Token getSon(int pos) {
        if (pos < 0) {
            return sons.get(sons.size() + pos);
        }
        if (pos >= sons.size()) {
            return null;
        }
        return sons.get(pos);
    }

    public String getName() {
        return content;
    }
    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public int getLine() {
        return line;
    }

    @Override
    public void check(ESymbolTable ESymbolTable, int loopCycles) {
        return;
    }

    @Override
    public Value visit() {
        return null;
    }

    //返回%d的个数; -1为formatString出错, 不考虑其他情况了
    public int checkFormatString() {
        int lens = content.length();
        int paramNum = 0;
        for (int i = 1; i < lens - 1; i++) {
            if (content.charAt(i) == '\\') {
                if (content.charAt(i + 1) != 'n') {
                    errors.add(new ErrorNode(line, ErrorNode.ECode.a, "\\ is wrong in line " + line));
                    return -1;
                }
            } else if (content.charAt(i) == '%') {//%不能单独出现
                if (content.charAt(i + 1) != 'd') {
                    errors.add(new ErrorNode(line, ErrorNode.ECode.a, "% is wrong in line " + line));
                    return -1;
                } else {
                    paramNum++;
                }
            } else if (!(content.charAt(i) == 32 || content.charAt(i) == 33 || (content.charAt(i) >= 40 && content.charAt(i) <= 126))) {
                errors.add(new ErrorNode(line, ErrorNode.ECode.a, "undefined letter exists in line " + line));
                return -1;
            }
        }
        return paramNum;
    }
}
