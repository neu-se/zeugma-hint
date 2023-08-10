package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import java.util.Random;

import edu.neu.ccs.prl.zeugma.internal.guidance.Individual;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.Modifier;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.Mutator;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.IntProducer;

public class DerivingModifier<T extends Individual> implements Modifier<T> {
    /**
     * Mutates individuals.
     * <p>
     * Non-null.
     */
    private final Mutator mutator;
    /**
     * Chooses the number of stacked modifications to apply.
     * <p>
     * Non-null.
     */
    private final IntProducer countSampler;
    private final Random random;
    private final DerivedMutatorFactory<? super T> factory;

    public DerivingModifier(Random random, Mutator mutator, IntProducer countSampler,
                            DerivedMutatorFactory<? super T> factory) {
        if (random == null || mutator == null || countSampler == null || factory == null) {
            throw new NullPointerException();
        }
        this.mutator = mutator;
        this.countSampler = countSampler;
        this.random = random;
        this.factory = factory;
    }

    @Override
    public ByteList modify(T parent, SimpleList<? extends T> population) {
        int count = countSampler.get();
        SimpleList<Mutator> mutators = new SimpleList<>(count);
        for (int i = 0; i < count; i++) {
            if (factory.canCreateMutator(parent, population) && random.nextBoolean()) {
                mutators.add(factory.create(parent, population));
            } else {
                mutators.add(mutator);
            }
        }
        return HintUtil.apply(mutators, parent.getInput());
    }
}

