package com.ronin.grace.visit;

import com.ronin.grace.adapter.GraceAdapter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class GraceVisitor extends ClassVisitor {


    private String className;
    private String superName;
    private String[] interfaces;

    public GraceVisitor(ClassVisitor cv) {
        super(Opcodes.ASM6, cv);
    }

    /**
     * ASM进入到类的方法时进行回调
     *
     * @param access
     * @param name       方法名
     * @param desc
     * @param signature
     * @param exceptions
     * @return
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
        GraceAdapter adapter = null;
        try {
            adapter = new GraceAdapter(Opcodes.ASM6, methodVisitor, access, name, desc);
            adapter.setClassName(this.className);
            adapter.setSuperName(this.superName);
            adapter.setInterfaces(this.interfaces);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != adapter) {
            return adapter;
        }
        return methodVisitor;
    }

    /**
     * 当ASM进入类时回调
     *
     * @param version
     * @param access
     * @param name       类名
     * @param signature
     * @param superName  父类名
     * @param interfaces 实现的接口名
     */
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
        this.superName = superName;
        this.interfaces = interfaces;
    }
}
