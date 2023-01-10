package utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class IOhelper {
    private static ArrayList<String> outputList = new ArrayList<>();

    public static String getInput() throws IOException {
        FileInputStream in = new FileInputStream("testfile.txt");
        int length = in.available();
        byte bytes[] = new byte[length];
        in.read(bytes);
        in.close();
        String sourceCode = new String(bytes, StandardCharsets.UTF_8);
        return sourceCode;
    }

    public static void clear(){
        outputList.clear();
    }

    public static void addOutput(String s){
        outputList.add(s);
    }

    public static void output(String filename) throws IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename));
        int size = outputList.size();
        for (String s : outputList) {
            osw.write(s);
            osw.write("\n");
        }
        osw.close();
    }
}
