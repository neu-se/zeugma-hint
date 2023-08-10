package edu.neu.ccs.prl.zeugma.internal.hint.fuzz.link;

import edu.neu.ccs.prl.zeugma.internal.guidance.linked.MethodCallVertex;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.Hint;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleMap;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleSet;
import edu.neu.ccs.prl.zeugma.internal.util.Interval;

public class LinkedHintRegistry {
    private final SimpleList<LinkedHint> hints = new SimpleList<>();
    private final SimpleSet<LinkedHint> hintSet = new SimpleSet<>();

    public SimpleList<LinkedHint> getHints() {
        return hints;
    }

    public void register(LinkedHint hint) {
        if (hintSet.add(hint)) {
            hints.add(hint);
        }
    }

    public void linkAndRegister(SimpleMap<Interval, MethodCallVertex> siteMap, SimpleList<Hint> hints) {
        for (int i = 0; i < hints.size(); i++) {
            Hint hint = hints.get(i);
            if (hint.isGlobalizable() && siteMap.containsKey(hint.getSource())) {
                MethodCallVertex vertex = siteMap.get(hint.getSource());
                register(new LinkedHint(hint.getReplacements(), vertex));
            }
        }
    }
}