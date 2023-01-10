package frontend.preprocess;

import frontend.preprocess.synnodes.Token;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private String sourceCode;
    private List<Token> tokens;
    private int nowLine = 1;
    private int pos = 0;
    private int lens;//length of sourceCode
    private boolean nowIsAnatation;//use for tackling anatation

    public Lexer(String sourceCode, boolean isPrint) throws IOException {
        this.sourceCode = sourceCode;
        this.lens = sourceCode.length();
        this.tokens = new ArrayList<>();
        //开始词法分析
        this.nowIsAnatation = false;
        Token token;
        while (true) {
            token = getToken();
            if (nowIsAnatation == true){
                nowIsAnatation = false;
                continue;
            }
            if (token == null) {
                break;
            }
            tokens.add(token);
        }
        //如果需要输出
        if (isPrint) {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream("output.txt"));
            int size = tokens.size();
            for (int i = 0; i < size; i++) {
                osw.write(tokens.get(i).toString());
                if (i != size - 1) {
                    osw.write("\n");
                }
            }
            osw.close();
        }
    }
    public List<Token> getTokens(){
        return tokens;
    }
    private void check_space() {
        while (pos < lens && isWhite()) {
            if (sourceCode.charAt(pos) == '\n') {
                nowLine++;
            }
            pos++;
        }
    }

    //若到末尾或出错，则返回Null
    private Token getToken() {
        //忽略空格等
        check_space();
        if (pos >= lens) {
            return null;
        }
        //处理注释
        boolean isAnatation = check_anatation();
        if(isAnatation){
            this.nowIsAnatation = true;
            return null;
        }
        //正式解析，分为3类，特殊字符、数字、标识符
        StringBuilder sb = new StringBuilder();
        //如果为字母，即标识符或保留字
        if (isLetter()) {
            while (isLetter() || isDigit()) {
                sb.append(sourceCode.charAt(pos));
                pos++;
            }
            return matchWord(sb.toString());
        } else if (isDigit()) {//即数字
            while (isDigit()) {
                sb.append(sourceCode.charAt(pos));
                pos++;
            }
            return new Token("INTCON", sb.toString(), nowLine);
        } else {//特殊符号
            return matchSpecWord();
        }
    }

    //给定word，返回相应的token
    private Token matchWord(String word) {
        String type;
        switch (word) {
            case "main":
                type = "MAINTK";
                break;
            case "const":
                type = "CONSTTK";
                break;
            case "int":
                type = "INTTK";
                break;
            case "break":
                type = "BREAKTK";
                break;
            case "continue":
                type = "CONTINUETK";
                break;
            case "if":
                type = "IFTK";
                break;
            case "else":
                type = "ELSETK";
                break;
            case "while":
                type = "WHILETK";
                break;
            case "getint":
                type = "GETINTTK";
                break;
            case "printf":
                type = "PRINTFTK";
                break;
            case "return":
                type = "RETURNTK";
                break;
            case "void":
                type = "VOIDTK";
                break;
            default://变量
                type = "IDENFR";
        }
        return new Token(type, word, nowLine);
    }

    //给定特殊符号，返回相应的token
    private Token matchSpecWord() {
        char nowChar = sourceCode.charAt(pos);
        String type;
        //pos为开始的字符，tmppos为结束的字符位置
        int tmppos = pos;
        switch (nowChar) {
            case '"':
                tmppos++;
                while (tmppos < lens && sourceCode.charAt(tmppos) != '"') {
                    tmppos++;
                }
                type = "STRCON";//包括双引号
                break;
            case '!':
                if (tmppos + 1 < lens && sourceCode.charAt(tmppos + 1) == '=') {
                    type = "NEQ";
                    tmppos++;
                    break;
                }
                type = "NOT";
                break;
            case '&':
                if (tmppos + 1 < lens && sourceCode.charAt(tmppos + 1) == '&') {
                    tmppos++;
                    type = "AND";
                    break;
                }
                type = "ERROR";
                break;
            case '|':
                if (tmppos + 1 < lens && sourceCode.charAt(tmppos + 1) == '|') {
                    tmppos++;
                    type = "OR";
                    break;
                }
                type = "ERROR";
                break;
            case '+':
                type = "PLUS";
                break;
            case '-':
                type = "MINU";
                break;
            case '*':
                type = "MULT";
                break;
            case '/':
                type = "DIV";
                break;
            case '%':
                type = "MOD";
                break;
            case '<':
                if (tmppos + 1 < lens && sourceCode.charAt(tmppos + 1) == '=') {
                    tmppos++;
                    type = "LEQ";
                    break;
                }
                type = "LSS";
                break;
            case '>':
                if (tmppos + 1 < lens && sourceCode.charAt(tmppos + 1) == '=') {
                    tmppos++;
                    type = "GEQ";
                    break;
                }
                type = "GRE";
                break;
            case '=':
                if (tmppos + 1 < lens && sourceCode.charAt(tmppos + 1) == '=') {
                    tmppos++;
                    type = "EQL";
                    break;
                }
                type = "ASSIGN";
                break;
            case ';':
                type = "SEMICN";
                break;
            case ',':
                type = "COMMA";
                break;
            case '(':
                type = "LPARENT";
                break;
            case ')':
                type = "RPARENT";
                break;
            case '[':
                type = "LBRACK";
                break;
            case ']':
                type = "RBRACK";
                break;
            case '{':
                type = "LBRACE";
                break;
            case '}':
                type = "RBRACE";
                break;
            default:
                type = "ERROR";
                break;
        }
        String content = sourceCode.substring(pos, tmppos + 1);
        pos = tmppos + 1;
        return new Token(type, content, nowLine);
    }

    private boolean isDigit() {
        return Character.isDigit(sourceCode.charAt(pos));
    }

    private boolean isLetter() {
        return Character.isLetter(sourceCode.charAt(pos)) ||
                sourceCode.charAt(pos) == '_';
    }

    private boolean isWhite() {
        return Character.isWhitespace(sourceCode.charAt(pos));
    }

    //如果为注释则返回true
    private boolean check_anatation() {
        boolean isAnatation = false;
        if (sourceCode.charAt(pos) == '/') {
            if (pos + 1 < lens && sourceCode.charAt(pos + 1) == '/') {
                pos += 2;
                while (pos < lens) {
                    if (sourceCode.charAt(pos) == '\n') {
                        nowLine++;
                        pos++;
                        break;
                    }
                    pos++;
                }
                isAnatation = true;
            } else if (pos + 1 < lens && sourceCode.charAt(pos + 1) == '*') {
                pos += 2;
                while (pos < lens) {
                    if (sourceCode.charAt(pos) == '*') {
                        if (pos + 1 < lens && sourceCode.charAt(pos + 1) == '/') {
                            pos += 2;
                            break;
                        }
                    }
                    if (sourceCode.charAt(pos) == '\n') {
                        nowLine++;
                    }
                    pos++;
                }
                isAnatation = true;
            }
        }
        return isAnatation;
    }
}
