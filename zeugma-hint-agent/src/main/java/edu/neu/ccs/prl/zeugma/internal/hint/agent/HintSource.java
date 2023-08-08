package edu.neu.ccs.prl.zeugma.internal.hint.agent;

final class HintSource {
    private final MethodInfo method;

    HintSource(MethodInfo method) {
        this.method = method;
    }

    public boolean matches(String owner, String name, String desc, boolean isStatic) {
        return equalOrWild(method.getOwner(), owner)
               && equalOrWild(method.getName(), name)
               && method.getDescriptor().equals(desc)
               && isStatic == method.isStatic();
    }

    private boolean equalOrWild(String s1, String s2) {
        return "*".equals(s1) || s1.equals(s2);
    }
}
