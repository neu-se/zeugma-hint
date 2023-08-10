package edu.neu.ccs.prl.zeugma.internal.hint.fuzz.link;

import java.util.Comparator;
import java.util.Random;

import edu.neu.ccs.prl.zeugma.internal.guidance.linked.MethodCallVertex;
import edu.neu.ccs.prl.zeugma.internal.guidance.select.RandomSelector;
import edu.neu.ccs.prl.zeugma.internal.guidance.select.Selector;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.DerivedMutator;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.DerivedMutatorFactory;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.HintUtil;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.TournamentSelector;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.local.LocalHintFactory;
import edu.neu.ccs.prl.zeugma.internal.runtime.model.MethodIdentifier;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;

public class LinkedHintFactory implements DerivedMutatorFactory<LinkedHintIndividual> {
    private final Selector<Object> selector;
    private final Random random;
    private final LocalHintFactory local;
    private final LinkedHintRegistry registry;

    public LinkedHintFactory(Random random, LinkedHintRegistry registry) {
        if (random == null || registry == null) {
            throw new NullPointerException();
        }
        this.selector = new RandomSelector<>(random);
        this.random = random;
        this.local = new LocalHintFactory(random);
        this.registry = registry;
    }

    @Override
    public boolean canCreateMutator(LinkedHintIndividual parent,
                                    SimpleList<? extends LinkedHintIndividual> population) {
        return local.canCreateMutator(parent, population) || canCreateLinkedHint(parent);
    }

    @Override
    public DerivedMutator create(LinkedHintIndividual parent, SimpleList<? extends LinkedHintIndividual> population) {
        if (canCreateLinkedHint(parent) && (!local.canCreateMutator(parent, population) || random.nextBoolean())) {
            return bindLinkedHint(parent);
        } else {
            return local.create(parent, population);
        }
    }

    public boolean canCreateLinkedHint(LinkedHintIndividual parent) {
        return !parent.getSiteMap().isEmpty() && !registry.getHints().isEmpty();
    }

    public DerivedMutator bindLinkedHint(LinkedHintIndividual parent) {
        MethodCallVertex site = selector.select(parent.getSiteMap().getValues());
        TournamentSelector<LinkedHint> hintSelector = new TournamentSelector<>(random, createHintCompartor(site), 32);
        LinkedHint hint = hintSelector.select(registry.getHints());
        return new DerivedMutator(site.getSourceInterval(), hint.getReplacements());
    }

    private Comparator<? super LinkedHint> createHintCompartor(MethodCallVertex site) {
        SimpleList<MethodIdentifier> path = HintUtil.getPathToRoot(site);
        return Comparator.comparingInt(h -> h.similarity(path));
    }
}
