package com.tutorial.asm;

public class Label {

	/**
     * Indicates if this label is only used for debug attributes. Such a label
     * is not the start of a basic block, the target of a jump instruction, or
     * an exception handler. It can be safely ignored in control flow graph
     * analysis algorithms (for optimization purposes).
     */
    static final int DEBUG = 1;

    /**
     * Indicates if the position of this label is known.
     */
    static final int RESOLVED = 2;

    /**
     * Indicates if this label has been updated, after instruction resizing.
     */
    static final int RESIZED = 4;

    /**
     * Indicates if this basic block has been pushed in the basic block stack.
     * See {@link MethodWriter#visitMaxs visitMaxs}.
     */
    static final int PUSHED = 8;

    /**
     * Indicates if this label is the target of a jump instruction, or the start
     * of an exception handler.
     */
    static final int TARGET = 16;

    /**
     * Indicates if a stack map frame must be stored for this label.
     */
    static final int STORE = 32;

    /**
     * Indicates if this label corresponds to a reachable basic block.
     */
    static final int REACHABLE = 64;

    /**
     * Indicates if this basic block ends with a JSR instruction.
     */
    static final int JSR = 128;

    /**
     * Indicates if this basic block ends with a RET instruction.
     */
    static final int RET = 256;

    /**
     * Indicates if this basic block is the start of a subroutine.
     */
    static final int SUBROUTINE = 512;

    /**
     * Indicates if this subroutine basic block has been visited by a
     * visitSubroutine(null, ...) call.
     */
    static final int VISITED = 1024;

    /**
     * Indicates if this subroutine basic block has been visited by a
     * visitSubroutine(!null, ...) call.
     */
    static final int VISITED2 = 2048;

    /**
     * Field used to associate user information to a label. Warning: this field
     * is used by the ASM tree package. In order to use it with the ASM tree
     * package you must override the
     * {@code org.objectweb.asm.tree.MethodNode#getLabelNode} method.
     */
    public Object info;

    /**
     * Flags that indicate the status of this label.
     *
     * @see #DEBUG
     * @see #RESOLVED
     * @see #RESIZED
     * @see #PUSHED
     * @see #TARGET
     * @see #STORE
     * @see #REACHABLE
     * @see #JSR
     * @see #RET
     */
    int status;

    /**
     * The line number corresponding to this label, if known. If there are
     * several lines, each line is stored in a separate label, all linked via
     * their next field (these links are created in ClassReader and removed just
     * before visitLabel is called, so that this does not impact the rest of the
     * code).
     */
    int line;

    /**
     * The position of this label in the code, if known.
     */
    int position;

    /**
     * Number of forward references to this label, times two.
     */
    private int referenceCount;

    /**
     * Informations about forward references. Each forward reference is
     * described by two consecutive integers in this array: the first one is the
     * position of the first byte of the bytecode instruction that contains the
     * forward reference, while the second is the position of the first byte of
     * the forward reference itself. In fact the sign of the first integer
     * indicates if this reference uses 2 or 4 bytes, and its absolute value
     * gives the position of the bytecode instruction. This array is also used
     * as a bitset to store the subroutines to which a basic block belongs. This
     * information is needed in {@link MethodWriter#visitMaxs}, after all
     * forward references have been resolved. Hence the same array can be used
     * for both purposes without problems.
     */
    private int[] srcAndRefPositions;

    // ------------------------------------------------------------------------

    /*
     * Fields for the control flow and data flow graph analysis algorithms (used
     * to compute the maximum stack size or the stack map frames). A control
     * flow graph contains one node per "basic block", and one edge per "jump"
     * from one basic block to another. Each node (i.e., each basic block) is
     * represented by the Label object that corresponds to the first instruction
     * of this basic block. Each node also stores the list of its successors in
     * the graph, as a linked list of Edge objects.
     *
     * The control flow analysis algorithms used to compute the maximum stack
     * size or the stack map frames are similar and use two steps. The first
     * step, during the visit of each instruction, builds information about the
     * state of the local variables and the operand stack at the end of each
     * basic block, called the "output frame", <i>relatively</i> to the frame
     * state at the beginning of the basic block, which is called the "input
     * frame", and which is <i>unknown</i> during this step. The second step, in
     * {@link MethodWriter#visitMaxs}, is a fix point algorithm that computes
     * information about the input frame of each basic block, from the input
     * state of the first basic block (known from the method signature), and by
     * the using the previously computed relative output frames.
     *
     * The algorithm used to compute the maximum stack size only computes the
     * relative output and absolute input stack heights, while the algorithm
     * used to compute stack map frames computes relative output frames and
     * absolute input frames.
     */

    /**
     * Start of the output stack relatively to the input stack. The exact
     * semantics of this field depends on the algorithm that is used.
     *
     * When only the maximum stack size is computed, this field is the number of
     * elements in the input stack.
     *
     * When the stack map frames are completely computed, this field is the
     * offset of the first output stack element relatively to the top of the
     * input stack. This offset is always negative or null. A null offset means
     * that the output stack must be appended to the input stack. A -n offset
     * means that the first n output stack elements must replace the top n input
     * stack elements, and that the other elements must be appended to the input
     * stack.
     */
    int inputStackTop;

    /**
     * Maximum height reached by the output stack, relatively to the top of the
     * input stack. This maximum is always positive or null.
     */
    int outputStackMax;

    /**
     * The successor of this label, in the order they are visited. This linked
     * list does not include labels used for debug info only. If
     * {@link ClassWriter#COMPUTE_FRAMES} option is used then, in addition, it
     * does not contain successive labels that denote the same bytecode position
     * (in this case only the first label appears in this list).
     */
    Label successor;

    /**
     * The next basic block in the basic block stack. This stack is used in the
     * main loop of the fix point algorithm used in the second step of the
     * control flow analysis algorithms. It is also used in
     * {@link #visitSubroutine} to avoid using a recursive method, and in
     * ClassReader to temporarily store multiple source lines for a label.
     *
     * @see MethodWriter#visitMaxs
     */
    Label next;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructs a new label.
     */
    public Label() {
    }

    // ------------------------------------------------------------------------
    // Methods to compute offsets and to manage forward references
    // ------------------------------------------------------------------------

    /**
     * Returns the offset corresponding to this label. This offset is computed
     * from the start of the method's bytecode. <i>This method is intended for
     * {@link Attribute} sub classes, and is normally not needed by class
     * generators or adapters.</i>
     *
     * @return the offset corresponding to this label.
     * @throws IllegalStateException
     *             if this label is not resolved yet.
     */
    public int getOffset() {
        if ((status & RESOLVED) == 0) {
            throw new IllegalStateException(
                    "Label offset position has not been resolved yet");
        }
        return position;
    }

    // ------------------------------------------------------------------------
    // Methods related to subroutines
    // ------------------------------------------------------------------------

    /**
     * Returns true is this basic block belongs to the given subroutine.
     *
     * @param id
     *            a subroutine id.
     * @return true is this basic block belongs to the given subroutine.
     */
    boolean inSubroutine(final long id) {
        if ((status & Label.VISITED) != 0) {
            return (srcAndRefPositions[(int) (id >>> 32)] & (int) id) != 0;
        }
        return false;
    }

    /**
     * Returns true if this basic block and the given one belong to a common
     * subroutine.
     *
     * @param block
     *            another basic block.
     * @return true if this basic block and the given one belong to a common
     *         subroutine.
     */
    boolean inSameSubroutine(final Label block) {
        if ((status & VISITED) == 0 || (block.status & VISITED) == 0) {
            return false;
        }
        for (int i = 0; i < srcAndRefPositions.length; ++i) {
            if ((srcAndRefPositions[i] & block.srcAndRefPositions[i]) != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Marks this basic block as belonging to the given subroutine.
     *
     * @param id
     *            a subroutine id.
     * @param nbSubroutines
     *            the total number of subroutines in the method.
     */
    void addToSubroutine(final long id, final int nbSubroutines) {
        if ((status & VISITED) == 0) {
            status |= VISITED;
            srcAndRefPositions = new int[nbSubroutines / 32 + 1];
        }
        srcAndRefPositions[(int) (id >>> 32)] |= (int) id;
    }

    // ------------------------------------------------------------------------
    // Overriden Object methods
    // ------------------------------------------------------------------------

    /**
     * Returns a string representation of this label.
     *
     * @return a string representation of this label.
     */
    @Override
    public String toString() {
        return "L" + System.identityHashCode(this);
    }
	
}
