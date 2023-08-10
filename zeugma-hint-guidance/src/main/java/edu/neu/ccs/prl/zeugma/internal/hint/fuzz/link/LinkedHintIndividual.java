package edu.neu.ccs.prl.zeugma.internal.hint.fuzz.link;

import edu.neu.ccs.prl.zeugma.internal.guidance.FuzzTarget;
import edu.neu.ccs.prl.zeugma.internal.guidance.linked.CallTree;
import edu.neu.ccs.prl.zeugma.internal.guidance.linked.CallTreeBuilder;
import edu.neu.ccs.prl.zeugma.internal.guidance.linked.CallTreeVertexVisitor;
import edu.neu.ccs.prl.zeugma.internal.guidance.linked.MethodCallVertex;
import edu.neu.ccs.prl.zeugma.internal.guidance.linked.ParameterRequestVertex;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.HintDeriver;
import edu.neu.ccs.prl.zeugma.internal.hint.fuzz.local.LocalHintIndividual;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleMap;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleSet;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Interval;

public class LinkedHintIndividual extends LocalHintIndividual {
    private final LinkedHintRegistry registry;
    private SimpleMap<Interval, MethodCallVertex> siteMap = null;

    public LinkedHintIndividual(ByteList input, LinkedHintRegistry registry) {
        super(input);
        if (registry == null) {
            throw new NullPointerException();
        }
        this.registry = registry;
    }

    public SimpleMap<Interval, MethodCallVertex> getSiteMap() {
        if (siteMap == null) {
            throw new IllegalStateException();
        }
        return siteMap;
    }

    @Override
    public void initialize(FuzzTarget target) {
        HintDeriver deriver = new HintDeriver(target);
        deriver.derive(getInput());
        super.initialize(deriver);
        // Associate hints and hint sites with tree vertices
        CallTreeBuilder builder = new CallTreeBuilder(target);
        CallTree tree = builder.build(getInput());
        linkSites(tree, deriver.getHintSites());
        registry.linkAndRegister(siteMap, getLocalHints());
    }

    private void linkSites(CallTree tree, SimpleSet<Interval> sites) {
        this.siteMap = new SimpleMap<>();
        tree.preorderTraverse(new CallTreeVertexVisitor() {
            @Override
            public void visit(MethodCallVertex vertex) {
                Interval source = vertex.getSourceInterval();
                if (sites.contains(source) && !siteMap.containsKey(source)) {
                    // Take the closest vertex to the root for the source interval
                    siteMap.put(source, vertex);
                }
            }

            @Override
            public void visit(ParameterRequestVertex vertex) {
            }
        });
    }
}
