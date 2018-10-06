package sk.intersoft.vicinity.semptests;

import org.apache.log4j.Logger;

import java.io.File;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class RunAgent implements Runnable {
    private static final Logger LOG = Logger.getLogger(RunAgent.class);
    private Thread threadForAgent;
    private Process agentProcess;
    private String agentConfigFile;
    private ProcessBuilder builder;
    private static int counter = 1;

    public RunAgent(String agentConfigFile) {
        this.builder = new ProcessBuilder();
        this.agentConfigFile = agentConfigFile;
    }

    public void start() {
        threadForAgent = new Thread(this, "VicinityAgent");
        threadForAgent.start();
    }

    @Override
    public void run() {
        if (AutoDiscoveryFunctionalTest.WINDOWS) {
            builder.command(System.getProperty("user.home")+"/vicinity/agent/agent.bat", "", "");
        } else {
            builder.command("sh", "-c", System.getProperty("user.home") + "/vicinity/agent/agent.sh");
        }
        builder.directory(new File(System.getProperty("user.home") + "/vicinity/agent/"));
//        builder.redirectError(new File(System.getProperty("user.home")+
//                "/vicinity/agent/err" + Thread.currentThread().getId() + ".txt"));
//        builder.redirectOutput(new File(System.getProperty("user.home")+
//                "/vicinity/agent/out" + Thread.currentThread().getId()+".txt"));
        ClassLoader classLoader = AutoDiscoveryFunctionalTest.class.getClassLoader();
        try {
            Files.copy(
                    new File(classLoader.getResource(agentConfigFile).getFile()).toPath(),
                    new File(System.getProperty("" +
                            "user.home") + "/vicinity/agent/config/agents/agent-01.json").toPath(),
                    REPLACE_EXISTING);
            agentProcess = builder.start();
            agentProcess.waitFor();
            //LOG.info(String.format("after waitFor in run() - %s", agentConfigFile));
        } catch (Exception e) {
            System.out.println("Error by starting the agent: " + e.getMessage());
            System.exit(100);
        }
    }

    public void stop() {
        if (AutoDiscoveryFunctionalTest.WINDOWS) {
            agentProcess.destroy();
            return;
        }

        ProcessBuilder builder2 = new ProcessBuilder();
        try {
            builder2.command("sh", "-c", System.getProperty("user.home") + "/vicinity/agent/agent.sh stop");
            builder2.directory(new File(System.getProperty("user.home") + "/vicinity/agent/"));
//            builder2.redirectError(new File(System.getProperty("user.home")+
//                    "/vicinity/agent/errStop" + counter + ".txt"));
//            builder2.redirectOutput(new File(System.getProperty("user.home")+
//                    "/vicinity/agent/outStop" + counter +".txt"));
//            counter++;
            Process process = builder2.start();
            process.waitFor();
            //LOG.info(String.format("after waitFor in stop() - %s", agentConfigFile));
        } catch (Exception e) {
            System.out.println("Error by stopping the agent: " + e.getMessage());
        }
        //just wait a little bit for agent to run
        try {
            Thread.sleep(5000);
        } catch (Exception ex) {
        }
        agentProcess.destroy();
        threadForAgent.stop();
    }
}
