package edu.neu.ccs.prl.zeugma.internal.hint.fuzz.global;

import edu.neu.ccs.prl.zeugma.internal.guidance.FuzzTarget;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.HintDeriver;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.HintUtil;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.local.LocalHintIndividual;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Interval;

public class GlobalHintIndividual extends LocalHintIndividual {
    private final GlobalHintRegistry registry;
    private SimpleList<Interval> hintSites = null;

    public GlobalHintIndividual(ByteList input, GlobalHintRegistry registry) {
        super(input);
        if (registry == null) {
            throw new NullPointerException();
        }
        this.registry = registry;
    }

    public SimpleList<Interval> getHintSites() {
        if (hintSites == null) {
            throw new IllegalStateException();
        }
        return hintSites;
    }

    @Override
    public void initialize(FuzzTarget target) {
        HintDeriver deriver = new HintDeriver(target);
        deriver.derive(getInput());
        super.initialize(deriver);
        this.hintSites = HintUtil.toList(deriver.getHintSites());
        registry.register(getLocalHints());
    }
}
