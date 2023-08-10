package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;


import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.GenerateEventBroker;
import edu.neu.ccs.prl.zeugma.internal.provider.PrimitiveWriter;
import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Interval;

public class StringHint extends DerivedMutator {
    private final String target;
    private boolean globalizable = true;

    public StringHint(Interval source, String target) {
        super(source, createReplacements(target));
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
        return String.format("Gen<String> -> %s", target);
    }

    private static ByteList createReplacements(String target) {
        ByteList replacements = new ByteArrayList();
        // Flag to indicate that an unconstrained String should be generated
        PrimitiveWriter.write(replacements, GenerateEventBroker.FLAG);
        byte[] values = target.getBytes();
        PrimitiveWriter.write(replacements, values.length);
        replacements.addAll(values);
        return replacements;
    }
}
