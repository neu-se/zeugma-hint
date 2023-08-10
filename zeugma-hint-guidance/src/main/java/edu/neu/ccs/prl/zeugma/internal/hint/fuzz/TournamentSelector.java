package edu.neu.ccs.prl.zeugma.internal.hint.fuzz;


import java.util.Comparator;
import java.util.Random;

import edu.neu.ccs.prl.zeugma.internal.guidance.select.Selector;
import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;

public class TournamentSelector<T> implements Selector<T> {
    /**
     * Pseudo-random number generator.
     * <p>
     * Non-null.
     */
    private final Random random;
    /**
     * Used to determine the winner of tournaments.
     * <p>
     * Non-null.
     */
    private final Comparator<? super T> comparator;
    /**
     * Tournament size used for selection.
     * <p>
     * Positive.
     */
    private final int tournamentSize;

    public TournamentSelector(Random random, Comparator<? super T> comparator, int tournamentSize) {
        if (random == null || comparator == null) {
            throw new NullPointerException();
        }
        if (tournamentSize < 1) {
            throw new IllegalArgumentException();
        }
        this.random = random;
        this.comparator = comparator;
        this.tournamentSize = tournamentSize;
    }

    @Override
    public <R extends T> R select(SimpleList<R> list) {
        if (list.isEmpty()) {
            throw new IllegalArgumentException();
        }
        R best = list.get(random.nextInt(list.size()));
        for (int i = 1; i < tournamentSize; i++) {
            R candidate = list.get(random.nextInt(list.size()));
            if (comparator.compare(candidate, best) > 0) {
                best = candidate;
            }
        }
        return best;
    }

    @Override
    public String toString() {
        return String.format("<%s Tournament %d>", comparator, tournamentSize);
    }
}