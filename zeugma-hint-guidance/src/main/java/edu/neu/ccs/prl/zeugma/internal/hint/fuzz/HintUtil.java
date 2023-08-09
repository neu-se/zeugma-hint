package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import edu.neu.ccs.prl.zeugma.internal.guidance.modify.Mutator;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.Iterator;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.ObjectIntMap;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleSet;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

public final class HintUtil {
    private HintUtil() {
        throw new AssertionError();
    }

    public static <T> SimpleList<T> toList(SimpleSet<? extends T> set) {
        SimpleList<T> result = new SimpleList<>();
        Iterator<? extends ObjectIntMap.Entry<? extends T>> itr = set.getBackingMap().entryIterator();
        while (itr.hasNext()) {
            result.add(itr.next().getKey());
        }
        return result;
    }

    public static String replaceFirst(String s, String old, String replacement) {
        StringBuilder builder = new StringBuilder(s);
        int index = builder.indexOf(old);
        return builder.replace(index, index + old.length(), replacement).toString();
    }

    public static ByteList apply(DerivedMutator[] mutators, ByteList values) {
        // Apply the mutators in reverse-order by position; mutators targeting earlier positions can shift the start of
        // mutator that target later positions
        java.util.Arrays.sort(mutators, (m1, m2) -> {
            int s1 = m1.getSource().getStart();
            int s2 = m2.getSource().getStart();
            //noinspection UseCompareMethod
            return (s1 < s2) ? 1 : ((s1 == s2) ? 0 : -1);
        });
        DerivedMutator last = null;
        for (DerivedMutator mutator : mutators) {
            // Check that mutator targets a range before the range targeted by the last mutator
            if (mutator.isValid(values) && (last == null || mutator.getSource().getEnd() <= last.getSource()
                                                                                                .getStart())) {
                last = mutator;
                values = mutator.mutate(values);
            }
        }
        return values;
    }

    public static ByteList apply(SimpleList<Mutator> mutators, ByteList parent) {
        SimpleList<DerivedMutator> derived = new SimpleList<>();
        SimpleList<Mutator> other = new SimpleList<>();
        for (int i = 0; i < mutators.size(); i++) {
            Mutator m = mutators.get(i);
            if (m instanceof DerivedMutator) {
                derived.add((DerivedMutator) m);
            } else {
                other.add(m);
            }
        }
        ByteList child = apply(derived.toArray(new DerivedMutator[derived.size()]), parent);
        for (int i = 0; i < mutators.size(); i++) {
            Mutator m = mutators.get(i);
            if (!(m instanceof DerivedMutator) && m.isValid(child)) {
                child = m.mutate(child);
            }
        }
        return child;
    }
}
