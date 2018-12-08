package com.tutorial.asm;

/**
 * A {@link ClassVisitor} that generates classes in bytecode form. More
 * precisely this visitor generates a byte array conforming to the Java class
 * file format. It can be used alone, to generate a Java class "from scratch",
 * or with one or more {@link ClassReader ClassReader} and adapter class visitor
 * to generate a modified class from one or more existing Java classes.
 * 
 * @author Eric Bruneton
 */
public class ClassWriter extends ClassVisitor {
	
	/**
	 * Flag to automatically compute the maximum stack size and the maximum
	 * number of local variables of methods. If this flag is set, then the
	 * arguments of the {@link MethodVisitor#visitMaxs visitMaxs} method of the
	 * {@link MethodVisitor} returned by the {@link #visitMethod visitMethod}
	 * method will be ignored, and computed automatically from the signature and
	 * the bytecode of each method.
	 * 
	 * @see #ClassWriter(int)
	 */
	public static final int COMPUTE_MAXS = 1;

	/**
	 * Flag to automatically compute the stack map frames of methods from
	 * scratch. If this flag is set, then the calls to the
	 * {@link MethodVisitor#visitFrame} method are ignored, and the stack map
	 * frames are recomputed from the methods bytecode. The arguments of the
	 * {@link MethodVisitor#visitMaxs visitMaxs} method are also ignored and
	 * recomputed from the bytecode. In other words, computeFrames implies
	 * computeMaxs.
	 * 
	 * @see #ClassWriter(int)
	 */
	public static final int COMPUTE_FRAMES = 2;
	
	/**
     * Pseudo access flag to distinguish between the synthetic attribute and the
     * synthetic access flag.
     */
    static final int ACC_SYNTHETIC_ATTRIBUTE = 0x40000;
	
	 /**
     * The type of CONSTANT_Class constant pool items.
     */
	static final int CLASS = 7;
	
	  /**
     * The type of CONSTANT_Fieldref constant pool items.
     */
	static final int FIELD = 9;
	
	/**
     * The type of CONSTANT_Methodref constant pool items.
     */
	static final int METH = 10;
	
	/**
     * The type of CONSTANT_InterfaceMethodref constant pool items.
     */
	static final int IMETH = 11;
	
	/**
     * The type of CONSTANT_String constant pool items.
     */
    static final int STR = 8;

    /**
     * The type of CONSTANT_Integer constant pool items.
     */
    static final int INT = 3;

    /**
     * The type of CONSTANT_Float constant pool items.
     */
    static final int FLOAT = 4;

    /**
     * The type of CONSTANT_Long constant pool items.
     */
    static final int LONG = 5;
    
    /**
     * The type of CONSTANT_Utf8 constant pool items.
     */
    static final int UTF8 = 1;

    /**
     * The type of CONSTANT_Double constant pool items.
     */
    static final int DOUBLE = 6;
    
    /**
     * The type of CONSTANT_NameAndType constant pool items.
     */
    static final int NAME_TYPE = 12;
    
    /**
     * The type of CONSTANT_MethodHandle constant pool items.
     */
    static final int HANDLE = 15;
    
    /**
     * The type of CONSTANT_MethodType constant pool items.
     */
    static final int MTYPE = 16;
    
    /**
     * The type of instructions without any argument.
     */
    static final int NOARG_INSN = 0;

    /**
     * The type of instructions with an signed byte argument.
     */
    static final int SBYTE_INSN = 1;

    /**
     * The type of instructions with an signed short argument.
     */
    static final int SHORT_INSN = 2;

    /**
     * The type of instructions with a local variable index argument.
     */
    static final int VAR_INSN = 3;

    /**
     * The type of instructions with an implicit local variable index argument.
     */
    static final int IMPLVAR_INSN = 4;

    /**
     * The type of instructions with a type descriptor argument.
     */
    static final int TYPE_INSN = 5;

    /**
     * The type of field and method invocations instructions.
     */
    static final int FIELDORMETH_INSN = 6;

    /**
     * The type of the INVOKEINTERFACE/INVOKEDYNAMIC instruction.
     */
    static final int ITFMETH_INSN = 7;

    /**
     * The type of the INVOKEDYNAMIC instruction.
     */
    static final int INDYMETH_INSN = 8;

    /**
     * The type of instructions with a 2 bytes bytecode offset label.
     */
    static final int LABEL_INSN = 9;

    /**
     * The type of instructions with a 4 bytes bytecode offset label.
     */
    static final int LABELW_INSN = 10;

    /**
     * The type of the LDC instruction.
     */
    static final int LDC_INSN = 11;

    /**
     * The type of the LDC_W and LDC2_W instructions.
     */
    static final int LDCW_INSN = 12;

    /**
     * The type of the IINC instruction.
     */
    static final int IINC_INSN = 13;

    /**
     * The type of the TABLESWITCH instruction.
     */
    static final int TABL_INSN = 14;

    /**
     * The type of the LOOKUPSWITCH instruction.
     */
    static final int LOOK_INSN = 15;

    /**
     * The type of the MULTIANEWARRAY instruction.
     */
    static final int MANA_INSN = 16;

    /**
     * The type of the WIDE instruction.
     */
    static final int WIDE_INSN = 17;

    /**
     * The instruction types of all JVM opcodes.
     */
    static final byte[] TYPE;
    
    /**
     * Normal type Item stored in the ClassWriter {@link ClassWriter#typeTable},
     * instead of the constant pool, in order to avoid clashes with normal
     * constant pool items in the ClassWriter constant pool's hash table.
     */
    static final int TYPE_NORMAL = 30;
    
    /**
     * Uninitialized type Item stored in the ClassWriter
     * {@link ClassWriter#typeTable}, instead of the constant pool, in order to
     * avoid clashes with normal constant pool items in the ClassWriter constant
     * pool's hash table.
     */
    static final int TYPE_UNINIT = 31;

    /**
     * Merged type Item stored in the ClassWriter {@link ClassWriter#typeTable},
     * instead of the constant pool, in order to avoid clashes with normal
     * constant pool items in the ClassWriter constant pool's hash table.
     */
    static final int TYPE_MERGED = 32;
    
    /**
     * The type of BootstrapMethods items. These items are stored in a special
     * class attribute named BootstrapMethods and not in the constant pool.
     */
    static final int BSM = 33;
    
    /**
     * The type of CONSTANT_InvokeDynamic constant pool items.
     */
    static final int INDY = 18;
    
    /**
     * The base value for all CONSTANT_MethodHandle constant pool items.
     * Internally, ASM store the 9 variations of CONSTANT_MethodHandle into 9
     * different items.
     */
    static final int HANDLE_BASE = 20;
    
    /**
     * Index of the next item to be added in the constant pool.
     */
    int index;
    
    /**
     * The number of entries in the BootstrapMethods attribute.
     */
    int bootstrapMethodsCount;

    /**
     * The constant pool of this class.
     */
    final ByteVector pool;

    /**
     * The constant pool's hash table data.
     */
    Item[] items;
    
    /**
     * The BootstrapMethods attribute.
     */
    ByteVector bootstrapMethods;
    
    /**
     * Number of elements in the {@link #typeTable} array.
     */
    private short typeCount;
    
    /**
     * The threshold of the constant pool's hash table.
     */
    int threshold;

    /**
     * A reusable key used to look for items in the {@link #items} hash table.
     */
    final Item key;

    /**
     * A reusable key used to look for items in the {@link #items} hash table.
     */
    final Item key2;

    /**
     * A reusable key used to look for items in the {@link #items} hash table.
     */
    final Item key3;

    /**
     * A reusable key used to look for items in the {@link #items} hash table.
     */
    final Item key4;
    
    /**
     * The class reader from which this class writer was constructed, if any.
     */
    ClassReader cr;
    

    /**
     * <tt>true</tt> if the maximum stack size and number of local variables
     * must be automatically computed.
     */
    private boolean computeMaxs;

    /**
     * <tt>true</tt> if the stack map frames must be recomputed from scratch.
     */
    private boolean computeFrames;
    
    /**
     * The methods of this class. These methods are stored in a linked list of
     * {@link MethodWriter} objects, linked to each other by their
     * {@link MethodWriter#mv} field. This field stores the first element of
     * this list.
     */
    MethodWriter firstMethod;
    
    /**
     * The methods of this class. These methods are stored in a linked list of
     * {@link MethodWriter} objects, linked to each other by their
     * {@link MethodWriter#mv} field. This field stores the last element of this
     * list.
     */
    MethodWriter lastMethod;
    
    /**
     * Computes the instruction types of JVM opcodes.
     */
    static {
        int i;
        byte[] b = new byte[220];
        String s = "AAAAAAAAAAAAAAAABCLMMDDDDDEEEEEEEEEEEEEEEEEEEEAAAAAAAADD"
                + "DDDEEEEEEEEEEEEEEEEEEEEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                + "AAAAAAAAAAAAAAAAANAAAAAAAAAAAAAAAAAAAAJJJJJJJJJJJJJJJJDOPAA"
                + "AAAAGGGGGGGHIFBFAAFFAARQJJKKJJJJJJJJJJJJJJJJJJ";
        for (i = 0; i < b.length; ++i) {
            b[i] = (byte) (s.charAt(i) - 'A');
        }
        TYPE = b;
    }
    
    /**
     * Adds an UTF8 string to the constant pool of the class being build. Does
     * nothing if the constant pool already contains a similar item. <i>This
     * method is intended for {@link Attribute} sub classes, and is normally not
     * needed by class generators or adapters.</i>
     * 
     * @param value
     *            the String value.
     * @return the index of a new or already existing UTF8 item.
     */
    public int newUTF8(final String value) {
        key.set(UTF8, value, null, null);
        Item result = get(key);
        if (result == null) {
            pool.putByte(UTF8).putUTF8(value);
            result = new Item(index++, key);
            put(result);
        }
        return result.index;
    }
    
    /**
     * Adds a class reference to the constant pool of the class being build.
     * Does nothing if the constant pool already contains a similar item.
     * <i>This method is intended for {@link Attribute} sub classes, and is
     * normally not needed by class generators or adapters.</i>
     * 
     * @param value
     *            the internal name of the class.
     * @return the index of a new or already existing class reference item.
     */
    public int newClass(final String value) {
        return newClassItem(value).index;
    }
    
    /**
     * Adds a class reference to the constant pool of the class being build.
     * Does nothing if the constant pool already contains a similar item.
     * <i>This method is intended for {@link Attribute} sub classes, and is
     * normally not needed by class generators or adapters.</i>
     * 
     * @param value
     *            the internal name of the class.
     * @return a new or already existing class reference item.
     */
    Item newClassItem(final String value) {
        key2.set(CLASS, value, null, null);
        Item result = get(key2);
        if (result == null) {
            pool.put12(CLASS, newUTF8(value));
            result = new Item(index++, key2);
            put(result);
        }
        return result;
    }
    
    /**
     * Returns the constant pool's hash table item which is equal to the given
     * item.
     * 
     * @param key
     *            a constant pool item.
     * @return the constant pool's hash table item which is equal to the given
     *         item, or <tt>null</tt> if there is no such item.
     */
    private Item get(final Item key) {
        Item i = items[key.hashCode % items.length];
        while (i != null && (i.type != key.type || !key.isEqualTo(i))) {
            i = i.next;
        }
        return i;
    }
    
    /**
     * Puts the given item in the constant pool's hash table. The hash table
     * <i>must</i> not already contains this item.
     * 
     * @param i
     *            the item to be added to the constant pool's hash table.
     */
    private void put(final Item i) {
        if (index + typeCount > threshold) {
            int ll = items.length;
            int nl = ll * 2 + 1;
            Item[] newItems = new Item[nl];
            for (int l = ll - 1; l >= 0; --l) {
                Item j = items[l];
                while (j != null) {
                    int index = j.hashCode % newItems.length;
                    Item k = j.next;
                    j.next = newItems[index];
                    newItems[index] = j;
                    j = k;
                }
            }
            items = newItems;
            threshold = (int) (nl * 0.75);
        }
        int index = i.hashCode % items.length;
        i.next = items[index];
        items[index] = i;
    }
    
    /**
     * Constructs a new {@link ClassWriter} object and enables optimizations for
     * "mostly add" bytecode transformations. These optimizations are the
     * following:
     * 
     * <ul>
     * <li>The constant pool from the original class is copied as is in the new
     * class, which saves time. New constant pool entries will be added at the
     * end if necessary, but unused constant pool entries <i>won't be
     * removed</i>.</li>
     * <li>Methods that are not transformed are copied as is in the new class,
     * directly from the original class bytecode (i.e. without emitting visit
     * events for all the method instructions), which saves a <i>lot</i> of
     * time. Untransformed methods are detected by the fact that the
     * {@link ClassReader} receives {@link MethodVisitor} objects that come from
     * a {@link ClassWriter} (and not from any other {@link ClassVisitor}
     * instance).</li>
     * </ul>
     * 
     * @param classReader
     *            the {@link ClassReader} used to read the original class. It
     *            will be used to copy the entire constant pool from the
     *            original class and also to copy other fragments of original
     *            bytecode where applicable.
     * @param flags
     *            option flags that can be used to modify the default behavior
     *            of this class. <i>These option flags do not affect methods
     *            that are copied as is in the new class. This means that the
     *            maximum stack size nor the stack frames will be computed for
     *            these methods</i>. See {@link #COMPUTE_MAXS},
     *            {@link #COMPUTE_FRAMES}.
     */
    public ClassWriter(final ClassReader classReader, final int flags) {
    	this(flags);
    	classReader.copyPool(this);
    	this.cr = classReader;
    }
    
    /**
     * Constructs a new {@link ClassWriter} object.
     * 
     * @param flags
     *            option flags that can be used to modify the default behavior
     *            of this class. See {@link #COMPUTE_MAXS},
     *            {@link #COMPUTE_FRAMES}.
     */
    public ClassWriter(final int flags) {
    	super(Opcodes.ASM5);
    	index = 1;
    	pool = new ByteVector();
    	items = new Item[256];
    	threshold = (int) (0.75 * items.length);
    	key = new Item();
		key2 = new Item();
		key3 = new Item();
		key4 = new Item();
		this.computeMaxs = (flags & COMPUTE_MAXS) != 0;
		this.computeFrames = (flags & COMPUTE_FRAMES) != 0;
    }

}
