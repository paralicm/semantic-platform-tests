package sk.intersoft.vicinity.agentTest;

public class Dump {
    StringBuffer out = new StringBuffer();

    public static String tab(int tab) {
        String out = "";
        for(int i = 0; i < tab; i++){
            out += "  ";
        }
        return out;
    }


    public void add(String string, int tab) {
        out.append(Dump.tab(tab) + string);
        nl();
    }

    public void add(String string) {
        out.append(string);
    }

    public void nl() {
        out.append(System.lineSeparator());
    }

    public String toString(){
        return out.toString();
    }



    // simple dump
    public static String indent(String string, int tab) {
        return tab(tab) + string;
    }

}
