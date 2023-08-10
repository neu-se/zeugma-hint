package edu.neu.ccs.prl.zeugma.internal.hint.fuzz.global;

import java.util.Random;

import edu.neu.ccs.prl.zeugma.internal.guidance.select.RandomSelector;
import edu.neu.ccs.prl.zeugma.internal.guidance.select.Selector;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.DerivedMutator;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.DerivedMutatorFactory;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.Hint;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.local.LocalHintFactory;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.util.Interval;

public class GlobalHintFactory implements DerivedMutatorFactory<GlobalHintIndividual> {
    private final Selector<Object> selector;
    private final Random random;
    private final LocalHintFactory local;
    private final GlobalHintRegistry registry;

    public GlobalHintFactory(Random random, GlobalHintRegistry registry) {
        if (random == null || registry == null) {
            throw new NullPointerException();
        }
        this.selector = new RandomSelector<>(random);
        this.random = random;
        this.local = new LocalHintFactory(random);
        this.registry = registry;
    }

    @Override
    public boolean canCreateMutator(GlobalHintIndividual parent,
                                    SimpleList<? extends GlobalHintIndividual> population) {
        return local.canCreateMutator(parent, population) || canCreateGlobalHint(parent);
    }

    @Override
    public DerivedMutator create(GlobalHintIndividual parent, SimpleList<? extends GlobalHintIndividual> population) {
        if (canCreateGlobalHint(parent) && (!local.canCreateMutator(parent, population) || random.nextBoolean())) {
            return createGlobalHint(parent);
        } else {
            return local.create(parent, population);
        }
    }

    public boolean canCreateGlobalHint(GlobalHintIndividual parent) {
        return !parent.getHintSites().isEmpty() && !registry.getTargets().isEmpty();
    }

    public Hint createGlobalHint(GlobalHintIndividual parent) {
        Interval site = selector.select(parent.getHintSites());
        String target = selector.select(registry.getTargets());
        return new Hint(site, target);
    }
}
