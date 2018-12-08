package com.tutorial.asm;

/**
 * A {@link MethodVisitor} that generates methods in bytecode form. Each visit
 * method of this class appends the bytecode corresponding to the visited
 * instruction to a byte vector, in the order these methods are called.
 * 
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
public class MethodWriter extends MethodVisitor {
	
	/**
     * Pseudo access flag used to denote constructors.
     */
    static final int ACC_CONSTRUCTOR = 0x80000;
    
    /**
     * Full frame
     */
    static final int FULL_FRAME = 255; // ff
    
    /**
     * Frame has exactly the same locals as the previous stack map frame and
     * number of stack items is 1
     */
    static final int SAME_LOCALS_1_STACK_ITEM_FRAME = 64; // to 127 (40-7f)

    /**
     * Reserved for future use
     */
    static final int RESERVED = 128;

    /**
     * Frame has exactly the same locals as the previous stack map frame and
     * number of stack items is 1. Offset is bigger then 63;
     */
    static final int SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED = 247; // f7
    
    /**
     * Frame where current locals are the same as the locals in the previous
     * frame, except that the k last locals are absent. The value of k is given
     * by the formula 251-frame_type.
     */
    static final int CHOP_FRAME = 248; // to 250 (f8-fA)

    /**
     * Frame has exactly the same locals as the previous stack map frame and
     * number of stack items is zero. Offset is bigger then 63;
     */
    static final int SAME_FRAME_EXTENDED = 251; // fb

    /**
     * Frame where current locals are the same as the locals in the previous
     * frame, except that k additional locals are defined. The value of k is
     * given by the formula frame_type-251.
     */
    static final int APPEND_FRAME = 252; // to 254 // fc-fe
	
	 /**
     * The class writer to which this method must be added.
     */
    final ClassWriter cw;
    
    /**
     * Access flags of this method.
     */
    private int access;
    
    /**
     * The index of the constant pool item that contains the name of this
     * method.
     */
    private final int name;

    /**
     * The index of the constant pool item that contains the descriptor of this
     * method.
     */
    private final int desc;

    /**
     * The descriptor of this method.
     */
    private final String descriptor;
    
    /**
     * If not zero, indicates that the code of this method must be copied from
     * the ClassReader associated to this writer in <code>cw.cr</code>. More
     * precisely, this field gives the number of bytes to copied from
     * <code>cw.cr.b</code>.
     */
    int classReaderLength;

    /**
     * The signature of this method.
     */
    String signature;

    /**
     * If not zero, indicates that the code of this method must be copied from
     * the ClassReader associated to this writer in <code>cw.cr</code>. More
     * precisely, this field gives the index of the first byte to copied from
     * <code>cw.cr.b</code>.
     */
    int classReaderOffset;
    
    /**
     * Number of exceptions that can be thrown by this method.
     */
    int exceptionCount;

    /**
     * The exceptions that can be thrown by this method. More precisely, this
     * array contains the indexes of the constant pool items that contain the
     * internal names of these exception classes.
     */
    int[] exceptions;
    
    /**
     * Indicates what must be automatically computed.
     * 
     * @see #FRAMES
     * @see #MAXS
     * @see #NOTHING
     */
    private final int compute;
    
    /**
     * Indicates that the stack map frames must be recomputed from scratch. In
     * this case the maximum stack size and number of local variables is also
     * recomputed from scratch.
     * 
     * @see #compute
     */
    private static final int FRAMES = 0;
    
    /**
     * Indicates that the maximum stack size and number of local variables must
     * be automatically computed.
     * 
     * @see #compute
     */
    private static final int MAXS = 1;
    
    /**
     * Indicates that nothing must be automatically computed.
     * 
     * @see #compute
     */
    private static final int NOTHING = 2;
    
    /**
     * Maximum number of local variables for this method.
     */
    private int maxLocals;
    
    /**
     * Number of local variables in the current stack map frame.
     */
    private int currentLocals;
    
    /**
     * A list of labels. This list is the list of basic blocks in the method,
     * i.e. a list of Label objects linked to each other by their
     * {@link Label#successor} field, in the order they are visited by
     * {@link MethodVisitor#visitLabel}, and starting with the first basic
     * block.
     */
    private Label labels;

    /**
     * The annotation default attribute of this method. May be <tt>null</tt>.
     */
    private ByteVector annd;
	
	/**
     * Constructs a new {@link MethodWriter}.
     * 
     * @param cw
     *            the class writer in which the method must be added.
     * @param access
     *            the method's access flags (see {@link Opcodes}).
     * @param name
     *            the method's name.
     * @param desc
     *            the method's descriptor (see {@link Type}).
     * @param signature
     *            the method's signature. May be <tt>null</tt>.
     * @param exceptions
     *            the internal names of the method's exceptions. May be
     *            <tt>null</tt>.
     * @param computeMaxs
     *            <tt>true</tt> if the maximum stack size and number of local
     *            variables must be automatically computed.
     * @param computeFrames
     *            <tt>true</tt> if the stack map tables must be recomputed from
     *            scratch.
     */
    MethodWriter(final ClassWriter cw, final int access, final String name,
            final String desc, final String signature,
            final String[] exceptions, final boolean computeMaxs,
            final boolean computeFrames) {
        super(Opcodes.ASM5);
        if (cw.firstMethod == null) {
            cw.firstMethod = this;
        } else {
            cw.lastMethod.mv = this;
        }
        cw.lastMethod = this;
        this.cw = cw;
        this.access = access;
        if ("<init>".equals(name)) {
            this.access |= ACC_CONSTRUCTOR;
        }
        this.name = cw.newUTF8(name);
        this.desc = cw.newUTF8(desc);
        this.descriptor = desc;
        if (ClassReader.SIGNATURES) {
            this.signature = signature;
        }
        if (exceptions != null && exceptions.length > 0) {
            exceptionCount = exceptions.length;
            this.exceptions = new int[exceptionCount];
            for (int i = 0; i < exceptionCount; ++i) {
                this.exceptions[i] = cw.newClass(exceptions[i]);
            }
        }
        this.compute = computeFrames ? FRAMES : (computeMaxs ? MAXS : NOTHING);
        if (computeMaxs || computeFrames) {
            // updates maxLocals
            int size = Type.getArgumentsAndReturnSizes(descriptor) >> 2;
            if ((access & Opcodes.ACC_STATIC) != 0) {
                --size;
            }
            maxLocals = size;
            currentLocals = size;
            // creates and visits the label for the first basic block
            labels = new Label();
            labels.status |= Label.PUSHED;
            visitLabel(labels);
        }
    }

}
