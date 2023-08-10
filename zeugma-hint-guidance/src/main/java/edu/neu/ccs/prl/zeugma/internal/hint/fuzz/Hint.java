package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import edu.neu.ccs.prl.zeugma.internal.util.Interval;

public class Hint extends DerivedMutator {
    private final String target;
    private boolean globalizable = true;

    public Hint(Interval source, String target) {
        super(source, HintUtil.createReplacements(target));
        this.target = target;
    }

    public boolean isGlobalizable() {
        return globalizable;
    }

    public void setGlobalizable(boolean globalizable) {
        this.globalizable = globalizable;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return String.format("Hint -> %s", target);
    }
}
