package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import edu.neu.ccs.prl.zeugma.internal.guidance.FuzzTarget;
import edu.neu.ccs.prl.zeugma.internal.guidance.Individual;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

public class HintIndividual extends Individual {
    private SimpleList<StringHint> localHints = null;
    private SimpleList<HintSite> hintSites = null;

    public HintIndividual(ByteList input) {
        super(input);
    }

    public SimpleList<StringHint> getLocalHints() {
        if (localHints == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " instance has not been initialized.");
        }
        return localHints;
    }

    public SimpleList<HintSite> getHintSites() {
        if (hintSites == null) {
            throw new IllegalStateException(getClass().getSimpleName() + " instance has not been initialized.");
        }
        return hintSites;
    }

    @Override
    public void initialize(FuzzTarget target) {
        HintDeriver deriver = new HintDeriver(target);
        deriver.derive(getInput());
        this.localHints = deriver.getHints();
        this.hintSites = deriver.getHintSites();
        GlobalHintRegistry.register(this.localHints);
    }
}
