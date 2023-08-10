package edu.neu.ccs.prl.zeugma.internal.hint.fuzz.local;

import java.util.Random;

import edu.neu.ccs.prl.zeugma.internal.guidance.select.RandomSelector;
import edu.neu.ccs.prl.zeugma.internal.guidance.select.Selector;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.DerivedMutator;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.DerivedMutatorFactory;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;

public class LocalHintFactory implements DerivedMutatorFactory<LocalHintIndividual> {
    private final Selector<Object> selector;

    public LocalHintFactory(Random random) {
        this.selector = new RandomSelector<>(random);
    }

    @Override
    public boolean canCreateMutator(LocalHintIndividual parent, SimpleList<? extends LocalHintIndividual> population) {
        return !parent.getLocalHints().isEmpty();
    }

    @Override
    public DerivedMutator create(LocalHintIndividual parent, SimpleList<? extends LocalHintIndividual> population) {
        return selector.select(parent.getLocalHints());
    }
}
