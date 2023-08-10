package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;


import edu.neu.ccs.prl.zeugma.internal.guidance.FuzzTarget;
import edu.neu.ccs.prl.zeugma.internal.guidance.TestObserver;
import edu.neu.ccs.prl.zeugma.internal.guidance.TestRunner;
import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.ComparisonEventBroker;
import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.GenerateEventBroker;
import edu.neu.ccs.prl.zeugma.internal.provider.BasicRecordingDataProvider;
import edu.neu.ccs.prl.zeugma.internal.provider.DataProviderFactory;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleSet;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Interval;
import edu.neu.ccs.prl.zeugma.internal.util.Math;

public class HintDeriver implements TestObserver {
    private static final int MINIMUM_MATCH_LENGTH = 2;
    private final TestRunner runner;
    private final SimpleSet<StringHint> hints = new SimpleSet<>();
    private ComparisonRecorder recorder;
    private GenerateCollector collector;

    public HintDeriver(FuzzTarget target) {
        DataProviderFactory factory = BasicRecordingDataProvider.createFactory(null, target.getMaxInputSize());
        this.runner = new TestRunner(target, factory, this);
    }

    @Override
    public synchronized void starting(ByteList input) {
        recorder = new ComparisonRecorder(input);
        collector = new GenerateCollector(input);
        ComparisonEventBroker.setSubscriber(recorder);
        GenerateEventBroker.setSubscriber(collector);
    }

    @Override
    public synchronized void finished() {
        ComparisonEventBroker.setSubscriber(null);
        GenerateEventBroker.setSubscriber(null);
    }


    public synchronized void derive(ByteList values) {
        runner.run(values);
        if (recorder != null && collector != null) {
            recorder.accept(this);
        }
    }

    public synchronized SimpleList<HintSite> getHintSites() {
        return collector == null ? new SimpleList<>() : HintUtil.toList(collector.getHintSites());
    }

    public synchronized SimpleList<StringHint> getHints() {
        return HintUtil.toList(hints);
    }

    void visit(StringComparison comparison) {
        String o1 = comparison.getOperand1();
        String o2 = comparison.getOperand2();
        SimpleList<StringHint> h1 = createHints(collector, o1, o2);
        SimpleList<StringHint> h2 = createHints(collector, o2, o1);
        addHints(h1, h2.isEmpty());
        addHints(h2, h1.isEmpty());
    }

    private synchronized void addHints(SimpleList<StringHint> list, boolean globalizable) {
        for (int i = 0; i < list.size(); i++) {
            StringHint hint = list.get(i);
            hint.setGlobalizable(globalizable);
            hints.add(hint);
        }
    }

    private static SimpleList<String> findShortest(SimpleList<String> values) {
        int minLength = Integer.MAX_VALUE;
        for (int i = 0; i < values.size(); i++) {
            minLength = Math.min(minLength, values.get(i).length());
        }
        SimpleList<String> result = new SimpleList<>();
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            if (value.length() == minLength) {
                result.add(value);
            }
        }
        return result;
    }

    private static void createHints(SimpleList<StringHint> hints, SimpleList<Interval> sources, String value) {
        if (sources != null) {
            for (int i = 0; i < sources.size(); i++) {
                hints.add(new StringHint(sources.get(i), value));
            }
        }
    }

    private static SimpleList<StringHint> createHints(GenerateCollector collector, String oldValue, String newValue) {
        SimpleList<StringHint> hints = new SimpleList<>();
        createHints(hints, collector.getSources(oldValue), newValue);
        if (hints.isEmpty() && oldValue.length() >= MINIMUM_MATCH_LENGTH) {
            // A full match could not be found; attempt to find partial matches
            SimpleList<String> matches = collector.findPartialMatches(oldValue);
            matches = findShortest(matches);
            for (int i = 0; i < matches.size(); i++) {
                String match = matches.get(i);
                if (!oldValue.equals(match)) {
                    String value = HintUtil.replaceFirst(match, oldValue, newValue);
                    createHints(hints, collector.getSources(match), value);
                }
            }
        }
        return hints;
    }
}
