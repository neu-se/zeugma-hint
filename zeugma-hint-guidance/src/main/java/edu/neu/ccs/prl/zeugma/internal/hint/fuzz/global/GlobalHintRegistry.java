package edu.neu.ccs.prl.zeugma.internal.hint.fuzz.global;

import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.Hint;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleSet;

public class GlobalHintRegistry {
    private final SimpleList<String> targets = new SimpleList<>();
    private final SimpleSet<String> targetSet = new SimpleSet<>();

    public SimpleList<String> getTargets() {
        return targets;
    }

    public void register(Hint hint) {
        if (hint.isGlobalizable() && targetSet.add(hint.getTarget())) {
            targets.add(hint.getTarget());
        }
    }

    public void register(SimpleList<? extends Hint> hints) {
        for (int i = 0; i < hints.size(); i++) {
            register(hints.get(i));
        }
    }
}
