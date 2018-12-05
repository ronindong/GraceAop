package com.ronin.grace.adapter;

import com.ronin.grace.annotation.Cost;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

public class GraceAdapter extends AdviceAdapter {
    private String name;
    private String desc;
    private MethodVisitor mv;

    private String className;
    private String superName;
    private String[] interfaces;
    private boolean isMethodCost;


    public GraceAdapter(int api, MethodVisitor mv, int access, String name, String desc) {
        super(api, mv, access, name, desc);
        this.mv = mv;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public void visitCode() {
        super.visitCode();
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (Type.getDescriptor(Cost.class).equals(desc)) {
            isMethodCost = true;
        }

        return super.visitAnnotation(desc, visible);
    }

    @Override
    protected void onMethodEnter() {

        if (isMethodCost) {
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("========start=========");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            mv.visitLdcInsn(name);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
            mv.visitMethodInsn(INVOKESTATIC, "com/ronin/grace/util/TimeCache", "setStartTime", "(Ljava/lang/String;J)V", false);

        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
        if (isMethodCost) {
            mv.visitLdcInsn(name);
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
            mv.visitMethodInsn(INVOKESTATIC, "com/ronin/grace/util/TimeCache", "setEndTime", "(Ljava/lang/String;J)V", false);
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn(name);
            mv.visitMethodInsn(INVOKESTATIC, "com/ronin/grace/util/TimeCache", "getCostTime", "(Ljava/lang/String;)Ljava/lang/String;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("========end=========");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

        }
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    public void setInterfaces(String[] interfaces) {
        this.interfaces = interfaces;
    }
}
