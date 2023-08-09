package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;

public enum OperationType {
    LOCAL_HINT() {
        @Override
        public boolean isValid(HintIndividual parent) {
            return !parent.getLocalHints().isEmpty();
        }
    }, GLOBAL_HINT() {
        @Override
        public boolean isValid(HintIndividual parent) {
            return !GlobalHintRegistry.getTargets().isEmpty() && !parent.getHintSites().isEmpty();
        }
    }, MUTATE() {
        @Override
        public boolean isValid(HintIndividual parent) {
            return true;
        }
    };

    public abstract boolean isValid(HintIndividual parent);

    public static SimpleList<OperationType> getValid(HintIndividual parent, OperationType[] enabled) {
        SimpleList<OperationType> valid = new SimpleList<>(values().length);
        for (OperationType type : enabled) {
            if (type.isValid(parent)) {
                valid.add(type);
            }
        }
        return valid;
    }
}
