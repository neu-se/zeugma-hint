package edu.neu.ccs.prl.zeugma.internal.hint.fuzz.local;

import edu.neu.ccs.prl.zeugma.internal.guidance.FuzzTarget;
import edu.neu.ccs.prl.zeugma.internal.guidance.Individual;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.Hint;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.HintDeriver;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;

public class LocalHintIndividual extends Individual {
    private SimpleList<Hint> localHints = null;

    public LocalHintIndividual(ByteList input) {
        super(input);
    }

    public SimpleList<Hint> getLocalHints() {
        if (localHints == null) {
            throw new IllegalStateException();
        }
        return localHints;
    }

    @Override
    public void initialize(FuzzTarget target) {
        HintDeriver deriver = new HintDeriver(target);
        deriver.derive(getInput());
        initialize(deriver);
    }

    protected void initialize(HintDeriver deriver) {
        this.localHints = deriver.getHints();
    }
}

