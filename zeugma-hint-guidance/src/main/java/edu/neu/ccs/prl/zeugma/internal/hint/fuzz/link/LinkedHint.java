package edu.neu.ccs.prl.zeugma.internal.hint.fuzz.link;

import edu.neu.ccs.prl.zeugma.internal.guidance.linked.MethodCallVertex;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.HintUtil;
import edu.neu.ccs.prl.zeugma.internal.runtime.model.MethodIdentifier;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.UnmodifiableByteList;

public class LinkedHint {
    private final SimpleList<MethodIdentifier> path;
    private final UnmodifiableByteList replacements;

    public LinkedHint(ByteList replacements, MethodCallVertex vertex) {
        this.path = HintUtil.getPathToRoot(vertex);
        this.replacements = UnmodifiableByteList.of(new ByteArrayList(replacements));
    }

    public int similarity(SimpleList<MethodIdentifier> p) {
        int count = 0;
        for (; count < path.size() && count < p.size(); count++) {
            if (!path.get(count).equals(p.get(count))) {
                break;
            }
        }
        return count;
    }

    public UnmodifiableByteList getReplacements() {
        return replacements;
    }
}
