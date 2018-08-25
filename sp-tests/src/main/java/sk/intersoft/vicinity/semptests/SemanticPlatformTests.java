package sk.intersoft.vicinity.semptests;

public class SemanticPlatformTests {

    public static void main(String [ ] args) {

        //TODO: generovat objects_TD podla slovnikov pre property a actions & events
        Application adapter = new Application();
        adapter.objects_TD = "td-sample-1.json";
        Thread t1 = new Thread(adapter);
        t1.start();

        //just wait a little bit for adapter to start
        try {
            Thread.sleep(10000);
        } catch (Exception ex) {}

        //TODO: spustit agenta

        //TODO: kontaktovat NM

        //TODO: porovnat vysledky

        System.out.println("Woke up after sleep!");

        PrepareTDs prepare = new PrepareTDs();

        boolean result = prepare.prepareN(1, "./adapter1-nm.txt");

        System.out.println(result?"success":"failed");
        //assertThat(result);
    }


}
