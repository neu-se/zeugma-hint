package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import ec.util.MersenneTwister;
import edu.neu.ccs.prl.zeugma.internal.fuzz.GuidanceManager;
import edu.neu.ccs.prl.zeugma.internal.guidance.FuzzTarget;
import edu.neu.ccs.prl.zeugma.internal.guidance.Guidance;
import edu.neu.ccs.prl.zeugma.internal.guidance.GuidanceBuilder;
import edu.neu.ccs.prl.zeugma.internal.guidance.Individual;
import edu.neu.ccs.prl.zeugma.internal.guidance.ParametricGuidance;
import edu.neu.ccs.prl.zeugma.internal.guidance.PopulationTracker;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.Mutator;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.RegionMutator;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.ShiftedGeometricSampler;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.global.GlobalHintFactory;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.global.GlobalHintIndividual;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.global.GlobalHintRegistry;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.link.LinkedHintFactory;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.link.LinkedHintIndividual;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.link.LinkedHintRegistry;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.local.LocalHintFactory;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.local.LocalHintIndividual;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Function;
import edu.neu.ccs.prl.zeugma.internal.util.IntProducer;

public class HintGuidanceBuilder implements GuidanceBuilder {
    private final Random random = new MersenneTwister();
    private final long duration;
    private final File outputDirectory;
    private final String level = System.getProperty("zeugma.hint.level");

    public HintGuidanceBuilder(long duration, File outputDirectory) {
        this.duration = duration;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public Guidance build(FuzzTarget target) throws IOException {
        switch (level) {
            case "local":
                return create(target, LocalHintIndividual::new, new LocalHintFactory(random));
            case "global":
                GlobalHintRegistry registry = new GlobalHintRegistry();
                return create(target,
                              (i) -> new GlobalHintIndividual(i, registry),
                              new GlobalHintFactory(random, registry));
            case "link":
                LinkedHintRegistry lRegistry = new LinkedHintRegistry();
                return create(target,
                              (i) -> new LinkedHintIndividual(i, lRegistry),
                              new LinkedHintFactory(random, lRegistry));
            default:
                throw new IllegalArgumentException("Unknown hint level: " + level);
        }
    }

    private <T extends Individual> ParametricGuidance<T> create(FuzzTarget target,
                                                                Function<? super ByteList, ? extends T> factory,
                                                                DerivedMutatorFactory<? super T> mutatorFactory)
        throws IOException {
        IntProducer countSampler = new ShiftedGeometricSampler(random, 4);
        Mutator mutator = new RegionMutator(random, new ShiftedGeometricSampler(random, 8));
        return new ParametricGuidance<>(target,
                                        new GuidanceManager(target, outputDirectory, duration),
                                        random,
                                        new PopulationTracker<>(factory),
                                        new DerivingModifier<>(random, mutator, countSampler, mutatorFactory));
    }
}