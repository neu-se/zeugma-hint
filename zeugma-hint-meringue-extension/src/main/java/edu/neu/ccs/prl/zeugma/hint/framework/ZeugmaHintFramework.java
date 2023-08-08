package edu.neu.ccs.prl.zeugma.hint.framework;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import edu.neu.ccs.prl.meringue.CampaignConfiguration;
import edu.neu.ccs.prl.meringue.FileUtil;
import edu.neu.ccs.prl.meringue.JarFuzzFramework;
import edu.neu.ccs.prl.meringue.JvmLauncher;
import edu.neu.ccs.prl.meringue.Replayer;
import edu.neu.ccs.prl.zeugma.internal.fuzz.CampaignOutput;
import edu.neu.ccs.prl.zeugma.internal.hint.agent.ZeugmaHintAgent;
import edu.neu.ccs.prl.zeugma.internal.hint.agent.ZeugmaHintTransformer;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.HintGuidanceForkMain;
import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.ComparisonEventBroker;
import edu.neu.ccs.prl.zeugma.internal.instrument.InstrumentMain;
import edu.neu.ccs.prl.zeugma.internal.instrument.InstrumentUtil;
import edu.neu.ccs.prl.zeugma.internal.parametric.ZeugmaReplayer;

public class ZeugmaHintFramework implements JarFuzzFramework {
    private CampaignConfiguration config;
    private Properties properties;
    private File frameworkJar;

    @Override
    public void initialize(CampaignConfiguration config, Properties properties) {
        this.config = config;
        this.properties = properties;
    }

    @Override
    public Process startCampaign() throws IOException {
        FileUtil.ensureEmptyDirectory(config.getOutputDirectory());
        return createLauncher(getJvmDirectory(frameworkJar.getParentFile())).withVerbose(true).launch();
    }

    @Override
    public File[] getCorpusFiles() {
        return CampaignOutput.getCorpusDirectory(config.getOutputDirectory()).listFiles();
    }

    @Override
    public File[] getFailureFiles() {
        return CampaignOutput.getFailuresDirectory(config.getOutputDirectory()).listFiles();
    }

    @Override
    public Class<? extends Replayer> getReplayerClass() {
        return ZeugmaReplayer.class;
    }

    @Override
    public Collection<File> getRequiredClassPathElements() {
        return Collections.singleton(frameworkJar);
    }

    private File getJvmDirectory(File directory) {
        File jvmDir;
        String path = properties.getProperty("jvm");
        jvmDir = path != null && !path.isEmpty() ? new File(path) : new File(directory, "jvm-inst");
        System.out.println("Using JVM directory: " + jvmDir);
        return jvmDir;
    }

    private JvmLauncher createLauncher(File jvmDir) throws IOException {
        List<String> options = new LinkedList<>(config.getJavaOptions());
        options.add(InstrumentUtil.getAgentOption(ZeugmaHintAgent.class));
        options.add(InstrumentUtil.getBootClassPathOption(ComparisonEventBroker.class));
        options.add("-cp");
        options.add(String.join(File.pathSeparator,
                                config.getTestClasspathJar().getAbsolutePath(),
                                frameworkJar.getAbsolutePath()));
        InstrumentMain.instrument(jvmDir,
                                  ZeugmaHintTransformer.class,
                                  FileUtil.getClassPathElement(ComparisonEventBroker.class),
                                  FileUtil.javaExecToJavaHome(config.getJavaExecutable()));
        File javaExec = FileUtil.javaHomeToJavaExec(jvmDir);
        String[] arguments = {
            config.getTestClassName(),
            config.getTestMethodName(),
            String.valueOf(config.getDuration().toMillis()),
            config.getOutputDirectory().getAbsolutePath()};
        return JvmLauncher.fromMain(javaExec,
                                    HintGuidanceForkMain.class.getName(),
                                    options.toArray(new String[0]),
                                    false,
                                    arguments,
                                    config.getWorkingDirectory(),
                                    config.getEnvironment());
    }

    @Override
    public List<String> getAnalysisJavaOptions() {
        // Have the hint-agent modify the generators on reruns
        return Arrays.asList(InstrumentUtil.getAgentOption(ZeugmaHintAgent.class),
                             "-Dzeugma.hint.analysis=true",
                             InstrumentUtil.getBootClassPathOption(ComparisonEventBroker.class));
    }

    @Override
    public String getCoordinate() {
        return "edu.neu.ccs.prl.zeugma:zeugma-hint-guidance";
    }

    @Override
    public void setFrameworkJar(File frameworkJar) {
        this.frameworkJar = frameworkJar;
    }
}