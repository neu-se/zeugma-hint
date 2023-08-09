package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import edu.neu.ccs.prl.zeugma.internal.util.Interval;

public final class HintSite {
    private final Interval source;

    public HintSite(Interval source) {
        if (source.getStart() < 0) {
            throw new IllegalArgumentException();
        }
        this.source = source;
    }

    public Interval getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof HintSite)) {
            return false;
        }
        HintSite hintSite = (HintSite) o;
        return source.equals(hintSite.source);
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }
}
