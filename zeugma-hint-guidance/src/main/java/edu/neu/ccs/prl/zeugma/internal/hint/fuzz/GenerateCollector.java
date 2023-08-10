package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;

import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.GenerateEventSubscriber;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.Iterator;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.ObjectIntMap;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleMap;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleSet;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import edu.neu.ccs.prl.zeugma.internal.util.Interval;
import edu.neu.ccs.prl.zeugma.internal.util.UnmodifiableByteList;

class GenerateCollector implements GenerateEventSubscriber {
    private final ByteList input;
    private final SimpleMap<Object, SimpleList<Interval>> generatedSourceMap = new SimpleMap<>();

    GenerateCollector(ByteList input) {
        this.input = UnmodifiableByteList.of(input);
    }

    public synchronized SimpleList<Interval> getSources(Object o) {
        return generatedSourceMap.get(o);
    }

    @Override
    public int starting() {
        return input.size();
    }

    @Override
    public void finished(int start, Object generated) {
        int end = input.size();
        if (start != end) {
            synchronized (this) {
                if (!generatedSourceMap.containsKey(generated)) {
                    generatedSourceMap.put(generated, new SimpleList<>());
                }
                generatedSourceMap.get(generated).add(new Interval(start, end));
            }
        }
    }

    public synchronized SimpleList<String> findPartialMatches(String s) {
        Iterator<ObjectIntMap.Entry<Object>> itr = generatedSourceMap.getBackingMap().entryIterator();
        SimpleList<String> matches = new SimpleList<>();
        while (itr.hasNext()) {
            Object key = itr.next().getKey();
            if (key instanceof String && ((String) key).contains(s)) {
                matches.add((String) key);
            }
        }
        return matches;
    }

    public synchronized SimpleSet<Interval> getHintSites() {
        Iterator<ObjectIntMap.Entry<Object>> itr = generatedSourceMap.getBackingMap().entryIterator();
        SimpleSet<Interval> sites = new SimpleSet<>();
        while (itr.hasNext()) {
            Object key = itr.next().getKey();
            SimpleList<Interval> sources = generatedSourceMap.get(key);
            for (int i = 0; i < sources.size(); i++) {
                sites.add(sources.get(i));
            }
        }
        return sites;
    }
}