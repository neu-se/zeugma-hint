package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleSet;

public final class GlobalHintRegistry {
    private static final SimpleList<String> targets = new SimpleList<>();
    private static final SimpleSet<String> targetSet = new SimpleSet<>();

    private GlobalHintRegistry() {
        throw new AssertionError();
    }

    public static SimpleList<String> getTargets() {
        return targets;
    }

    public static void register(SimpleList<StringHint> localHints) {
        for (int i = 0; i < localHints.size(); i++) {
            StringHint hint = localHints.get(i);
            if (hint.isGlobalizable() && targetSet.add(hint.getTarget())) {
                targets.add(hint.getTarget());
            }
        }
    }
}
