package edu.neu.ccs.prl.zeugma.internal.hint.agent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;

import edu.neu.ccs.prl.zeugma.internal.runtime.struct.SimpleList;

final class HintSourceUtil {
    private HintSourceUtil() {
        throw new AssertionError();
    }

    private static Collection<String> readSourceLines() {
        String path = System.getProperty("zeugma.hint.sources");
        if (path != null) {
            try {
                return Files.readAllLines(new File(path).toPath());
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(-1);
                throw new IllegalStateException("Invalid hint source file: " + path);
            }
        }
        return Collections.emptySet();
    }

    private static MethodInfo parseMethodInfo(String s, boolean isStatic) {
        int i = s.indexOf('#');
        int j = s.indexOf('(');
        String owner = s.substring(0, i);
        String name = s.substring(i + 1, j);
        String desc = s.substring(j);
        return new MethodInfo(owner, name, desc, isStatic);
    }

    static HintSource[] readSources() {
        Collection<String> lines = readSourceLines();
        SimpleList<HintSource> sources = new SimpleList<>();
        for (String line : lines) {
            String[] parts = line.split("\\s+");
            MethodInfo method = parseMethodInfo(parts[0], Boolean.parseBoolean(parts[1]));
            sources.add(new HintSource(method));
        }
        return sources.toArray(new HintSource[sources.size()]);
    }
}
