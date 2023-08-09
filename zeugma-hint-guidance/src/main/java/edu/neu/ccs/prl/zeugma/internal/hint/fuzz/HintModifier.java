package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import java.util.Random;

import edu.neu.ccs.prl.zeugma.internal.guidance.modify.Modifier;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.Mutator;
import edu.neu.ccs.prl.zeugma.internal.guidance.select.RandomSelector;
import edu.neu.ccs.prl.zeugma.internal.guidance.select.Selector;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.IntProducer;

public class HintModifier implements Modifier<HintIndividual> {
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
    private final Selector<Object> selector;
    private final OperationType[] enabled;

    public HintModifier(Random random, Mutator mutator, IntProducer countSampler, OperationType[] enabled) {
        if (mutator == null || countSampler == null) {
            throw new NullPointerException();
        } else if (enabled.length == 0) {
            throw new IllegalArgumentException();
        }
        this.mutator = mutator;
        this.countSampler = countSampler;
        this.selector = new RandomSelector<>(random);
        this.enabled = enabled.clone();
    }

    @Override
    public ByteList modify(HintIndividual parent, SimpleList<? extends HintIndividual> population) {
        int count = countSampler.get();
        SimpleList<Mutator> mutators = new SimpleList<>(count);
        SimpleList<OperationType> types = OperationType.getValid(parent, enabled);
        for (int i = 0; i < count; i++) {
            switch (selector.select(types)) {
                case LOCAL_HINT:
                    mutators.add(selector.select(parent.getLocalHints()));
                    break;
                case GLOBAL_HINT:
                    mutators.add(createGlobalHint(parent));
                    break;
                case MUTATE:
                    mutators.add(mutator);
                    break;
            }
        }
        return HintUtil.apply(mutators, parent.getInput());
    }

    private StringHint createGlobalHint(HintIndividual parent) {
        HintSite site = selector.select(parent.getHintSites());
        String target = selector.select(GlobalHintRegistry.getTargets());
        return new StringHint(site.getSource(), target);
    }
}
