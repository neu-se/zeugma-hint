package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;

public interface DerivedMutatorFactory<T> {
    boolean canCreateMutator(T parent, SimpleList<? extends T> population);

    DerivedMutator create(T parent, SimpleList<? extends T> population);
}