//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.core.data;

final public class DataType
{
    public static final int CLASS_CROSSROAD = 1;
    public static final int CLASS_STREET = 2;
    public static final int CLASS_BUILDING = 4;
    public static final int CLASS_ROUTE = 6;
    public static final int CLASS_CATEGORY = 8;

    public final static int CLASS_NONE = 0;
    public final static int CLASS_POI = 4;
    public final static int CLASS_ADDRESS = 16;
    public final static int CLASS_SYSTEM = 64;

    public final static int MASK_PC = 0x1000000;
    public final static int MASK_STREET = 0x2000000;
    public final static int MASK_POI = 0x4000000;
    public final static int MASK_ROUTE = 0x6000000;
    public final static int MASK_CATEGORY = 0x8000000;
    public final static int MASK_ICON = 0x9000000;
    public final static int MASK_ADDRESS = 0x10000000;
    public final static int MASK_SYSTEM = 0x40000000;
}
