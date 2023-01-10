package backend;

import frontend.irgenerate.Initial;
import frontend.irgenerate.irclass.Constant;
import frontend.irgenerate.irclass.Value;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * 全局量, 包括数组, 字符串, 变量
 */
public class MSGlobalValue {
    public static final String word = ".word";
    public static final String array = ".word";
    public static final String str = ".asciiz";
    private String type;//.word, .asciiz
    private String name;
    //    private String content;
    private ArrayList<String> contents = new ArrayList<>();

    /**
     * 用于普通变量和字符串的全局初始化
     */
    public MSGlobalValue(String type, String name, Value value) {
        this.type = type;
        this.name = name;
        if (type.equals(word)) {
            //普通变量
            contents.add(value.getFullName());
        } else if (type.equals(str)) {
            String str = ((Constant.ConstantStr) value).getStr();
            str = str.replace("\n", "\\n");
            contents.add(str);
        }
    }

    /**
     * 用于数组的全局初始化
     * @param initial
     * @param size 数组总的元素个数
     */
    public MSGlobalValue(String type, String name, Initial initial, int size) {
        this.type = type;
        this.name = name;
//        assert type.equals(array);
        if(initial instanceof Initial.ArrayInitial){
            for (Initial init : ((Initial.ArrayInitial)initial).getInitials()) {
                if (init instanceof Initial.ArrayInitial) {
                    for (Initial init2 : ((Initial.ArrayInitial) init).getInitials()) {
                        contents.add(init2.getValue().getFullName());
                    }
                } else {
                    contents.add(init.getValue().getFullName());
                }
            }
        }
        for (int i = contents.size(); i < size; i++) {
            contents.add("0");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(type.equals(str)){
            return name + ": " + type + " \"" + contents.get(0) + "\"";
        }else {
            HashSet<String> tmp  = new HashSet<>(contents);
            if(tmp.size()==1 && tmp.contains("0")){
                return sb.append(name).append(": ").append(".space ").append(contents.size()*4).toString();
            }
            String s2 = contents.stream().reduce((s, s1)->(s+", "+s1)).orElse("");
            sb.append(name).append(": ").append(type).append(" ").append(s2);
            return sb.toString();
        }
    }
}
