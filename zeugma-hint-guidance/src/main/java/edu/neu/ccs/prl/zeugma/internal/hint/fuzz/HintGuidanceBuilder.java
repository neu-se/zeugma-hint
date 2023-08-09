package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import ec.util.MersenneTwister;
import edu.neu.ccs.prl.zeugma.internal.fuzz.GuidanceManager;
import edu.neu.ccs.prl.zeugma.internal.guidance.FuzzTarget;
import edu.neu.ccs.prl.zeugma.internal.guidance.Guidance;
import edu.neu.ccs.prl.zeugma.internal.guidance.GuidanceBuilder;
import edu.neu.ccs.prl.zeugma.internal.guidance.ParametricGuidance;
import edu.neu.ccs.prl.zeugma.internal.guidance.PopulationTracker;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.Mutator;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.RegionMutator;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.ShiftedGeometricSampler;
import edu.neu.ccs.prl.zeugma.internal.util.IntProducer;

public class HintGuidanceBuilder implements GuidanceBuilder {
    private final Random random = new MersenneTwister();
    private final long duration;
    private final File outputDirectory;
    private final String hintLevel = System.getProperty("zeugma.hint.level");

    public HintGuidanceBuilder(long duration, File outputDirectory) {
        this.duration = duration;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public Guidance build(FuzzTarget target) throws IOException {
        GuidanceManager manager = new GuidanceManager(target, outputDirectory, duration);
        Mutator mutator = new RegionMutator(random, new ShiftedGeometricSampler(random, 8));
        IntProducer countSampler = new ShiftedGeometricSampler(random, 4);
        return new ParametricGuidance<>(
            target,
            manager,
            random,
            new PopulationTracker<>(HintIndividual::new),
            new HintModifier(random, mutator, countSampler, getEnabledOperations(hintLevel)));
    }

    private static OperationType[] getEnabledOperations(String level) {
        if (level == null || level.equals("none")) {
            return new OperationType[] {OperationType.MUTATE};
        } else if (level.equals("local")) {
            return new OperationType[] {OperationType.MUTATE, OperationType.LOCAL_HINT};
        } else if (level.equals("global")) {
            return new OperationType[] {OperationType.MUTATE, OperationType.LOCAL_HINT, OperationType.GLOBAL_HINT};
        } else {
            throw new IllegalArgumentException("Unknown hint level: " + level);
        }
    }
}