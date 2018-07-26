package sk.intersoft.vicinity.semptests;

public class SemanticPlatformTests {

    public static void main(String [ ] args) {
        PrepareTDs prepare = new PrepareTDs();

        boolean result = prepare.prepareN(1, "./adapter1-nm.txt");

        System.out.println(result?"success":"failed");
        //assertThat(result);
    }


}
