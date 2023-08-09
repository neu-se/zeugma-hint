package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import edu.neu.ccs.prl.zeugma.internal.guidance.modify.ModifierUtil;
import edu.neu.ccs.prl.zeugma.internal.guidance.modify.Mutator;
import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Interval;
import edu.neu.ccs.prl.zeugma.internal.util.UnmodifiableByteList;

public class DerivedMutator implements Mutator {
    private final Interval source;
    private final ByteList replacements;

    public DerivedMutator(Interval source, ByteList replacements) {
        if (source.getStart() < 0) {
            throw new IllegalArgumentException();
        }
        this.source = source;
        this.replacements = UnmodifiableByteList.of(new ByteArrayList(replacements));
    }

    public Interval getSource() {
        return source;
    }

    @Override
    public String toString() {
        return String.format("%s -> %s", source, replacements);
    }

    @Override
    public boolean isValid(ByteList parent) {
        return source.getEnd() <= parent.size();
    }

    @Override
    public ByteList mutate(ByteList parent) {
        return ModifierUtil.replaceRange(parent, source.getStart(), source.size(), replacements);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof DerivedMutator)) {
            return false;
        }
        DerivedMutator that = (DerivedMutator) o;
        if (!source.equals(that.source)) {
            return false;
        }
        return replacements.equals(that.replacements);
    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + replacements.hashCode();
        return result;
    }
}
