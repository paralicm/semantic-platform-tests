package sk.intersoft.vicinity.semptests;

import java.io.File;
import java.nio.file.Files;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class RunAgent implements Runnable {
    private Thread threadForAgent;
    private Process agentProcess;
    private String agentConfigFile;
    private ProcessBuilder builder;

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
        builder.command("sh", "-c", System.getProperty("user.home") + "/vicinity/agent/agent.sh");
        builder.directory(new File(System.getProperty("user.home") + "/vicinity/agent/"));
        //builder.redirectError(new File(System.getProperty("user.home")+"/vicinity/err.txt"));
        ClassLoader classLoader = AutoDiscoveryFunctionalTest.class.getClassLoader();
        try {
            Files.copy(
                    new File(classLoader.getResource(agentConfigFile).getFile()).toPath(),
                    new File(System.getProperty("" +
                            "user.home") + "/vicinity/agent/config/agents/agent-01.json").toPath(),
                    REPLACE_EXISTING);
            agentProcess = builder.start();
            agentProcess.waitFor();
        } catch (Exception e) {
            System.out.println("Error by starting the agent: " + e.getMessage());
            System.exit(100);
        }
    }

    public void stop() {
        ProcessBuilder builder2 = new ProcessBuilder();
        try {
            builder2.command("sh", "-c", System.getProperty("user.home") + "/vicinity/agent/agent.sh stop");
            Process process = builder2.start();
            process.waitFor();
        } catch (Exception e) {
            System.out.println("Error by stopping the agent: " + e.getMessage());
        }
        agentProcess.destroy();
        threadForAgent.stop();
    }
}
