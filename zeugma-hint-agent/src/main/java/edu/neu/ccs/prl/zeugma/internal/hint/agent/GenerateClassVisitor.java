package edu.neu.ccs.prl.zeugma.internal.hint.agent;

import static edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Opcodes.ACC_BRIDGE;
import static edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Opcodes.ACC_STATIC;

import edu.neu.ccs.prl.zeugma.internal.agent.ZeugmaAgent;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.ClassVisitor;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Label;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.MethodVisitor;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Opcodes;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.Type;
import edu.neu.ccs.prl.zeugma.internal.agent.org.objectweb.asm.commons.LocalVariablesSorter;
import edu.neu.ccs.prl.zeugma.internal.hint.runtime.event.GenerateEventBroker;

final class GenerateClassVisitor extends ClassVisitor {
    private static final HintSource[] sources = HintSourceUtil.readSources();
    /**
     * Name of the class being visited.
     */
    private String className;

    GenerateClassVisitor(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if ((access & ACC_BRIDGE) == 0 && (access & ACC_ABSTRACT) == 0) {
            for (HintSource source : sources) {
                if (source.matches(className, name, desc, (access & ACC_STATIC) != 0)) {
                    return new GenerateMethodVisitor(api, mv, access, desc);
                }
            }
        }
        return mv;
    }

    private static final class GenerateMethodVisitor extends LocalVariablesSorter {
        /**
         * Label marking the jump target if the {@link GenerateEventBroker#checkFlag} returns true.
         */
        private final Label jumpTarget = new Label();
        /**
         * Label marking the end of the added local variable's scope.
         */
        private final Label varEnd = new Label();
        private final int randomnessIndex;
        private int var;

        GenerateMethodVisitor(int api, MethodVisitor mv, int access, String desc) {
            super(api, access, desc, mv);
            randomnessIndex = findRandomness(access, desc);
        }

        private int findRandomness(int access, String desc) {
            int argumentIndex = (access & ACC_STATIC) != 0 ? 0 : 1;
            for (Type arg : Type.getArgumentTypes(desc)) {
                if ("com/pholser/junit/quickcheck/random/SourceOfRandomness".equals(arg.getInternalName())) {
                    return argumentIndex;
                }
                argumentIndex += arg.getSize();
            }
            throw new IllegalArgumentException();
        }

        @Override
        public void visitCode() {
            super.visitCode();
            Label varStart = new Label();
            super.visitLabel(varStart);
            var = newLocal(Type.INT_TYPE);
            super.visitLocalVariable(ZeugmaAgent.ADDED_MEMBER_PREFIX + "start", "I", null,
                                     varStart, varEnd, var);
            super.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(GenerateEventBroker.class),
                "starting",
                "()I",
                false);
            mv.visitVarInsn(Opcodes.ISTORE, var);
            // Check if generation should be delegated to the broker
            pushRandom();
            super.visitInsn(Opcodes.DUP);
            super.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(GenerateEventBroker.class),
                "checkFlag",
                "(Ljava/util/Random;)Z",
                false);
            super.visitJumpInsn(Opcodes.IFNE, jumpTarget);
            super.visitInsn(Opcodes.POP);
        }

        private void pushRandom() {
            super.visitVarInsn(Opcodes.ALOAD, randomnessIndex);
            super.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "com/pholser/junit/quickcheck/random/SourceOfRandomness",
                "toJDKRandom",
                "()Ljava/util/Random;",
                false);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.ARETURN) {
                super.visitInsn(Opcodes.DUP);
                mv.visitVarInsn(Opcodes.ILOAD, var);
                super.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    Type.getInternalName(GenerateEventBroker.class),
                    "finished",
                    "(Ljava/lang/Object;I)V",
                    false);
            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitLabel(jumpTarget);
            super.visitFrame(
                Opcodes.F_NEW,
                0,
                new Object[0],
                1,
                new Object[] {"java/util/Random"});
            super.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                Type.getInternalName(GenerateEventBroker.class),
                "produceString",
                "(Ljava/util/Random;)Ljava/lang/String;",
                false);
            visitInsn(Opcodes.ARETURN);
            super.visitLabel(varEnd);
            super.visitMaxs(maxStack, maxLocals);
        }
    }
}
