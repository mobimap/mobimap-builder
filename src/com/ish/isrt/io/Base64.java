//**************************************************************************************\\
//                            P R O J E C T  |  i S R T	                                \\
//                           (c) 2000-2007 Ilya Shakhat                                 \\
//**************************************************************************************\\

package com.ish.isrt.io;

public class Base64
{
    private final static byte[] ENCODING_ALPHABET =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes();

    private static byte[] DECODING = new byte[128];

    static {
        for (int i=0; i < ENCODING_ALPHABET.length; i++)
            DECODING[ENCODING_ALPHABET[i]] = (byte)i;
    }

    /**
     * Encode binary array in base64
     * @param source byte[]
     * @return byte[]
     */
    public static byte[] encode (byte[] source)
    {
        int requiredLength = 3 * ( (source.length + 2) / 3);
        byte[] sourceBytes = new byte[requiredLength];
        System.arraycopy (source, 0, sourceBytes, 0, source.length);

        byte[] target = new byte[4 * (requiredLength / 3)];

        for (int i = 0, j = 0; i < requiredLength; i+=3, j+=4)
        {
            int b1 = sourceBytes[i] & 0xff;
            int b2 = sourceBytes[i+1] & 0xff;
            int b3 = sourceBytes[i+2] & 0xff;

            target[j] = ENCODING_ALPHABET [b1 >>> 2];
            target[j+1] = ENCODING_ALPHABET [((b1 & 0x03) << 4) | (b2 >>> 4)];
            target[j+2] = ENCODING_ALPHABET [((b2 & 0x0f) << 2) | (b3 >>> 6)];
            target[j+3] = ENCODING_ALPHABET [b3 & 0x3f];
        }

        int numPadBytes = requiredLength - source.length;

        for (int i = target.length - numPadBytes; i < target.length; i++)
            target[i] = '=';
        return target;
    }

    /**
     * Decode base64-encoded byte array to binary
     * @param source byte[]
     * @return byte[]
     */
    public static byte[] decode (byte[] source)
    {
        int sourceLength = source.length;
        if (sourceLength % 4 != 0)
            throw new RuntimeException (
                "valid Base64 codes have a multiple of 4 characters");
        int numGroups = sourceLength / 4;
        int numExtraBytes = source[sourceLength-2] == '='  ? 2 : (source[sourceLength-1] == '=' ? 1 : 0);
        byte[] targetBytes = new byte[3 * numGroups];
        byte[] sourceBytes = new byte[4];

        for (int group = 0; group < numGroups; group++)
        {
            for (int i = 0; i < sourceBytes.length; i++)
            {
                sourceBytes[i] = DECODING[source[4 * group + i]];
            }
            convert4To3 (sourceBytes, targetBytes, group * 3);
        }
        byte[] result = new byte[targetBytes.length - numExtraBytes];
        System.arraycopy(targetBytes, 0, result, 0, result.length);

        return result;
    }

    private static void convert4To3 (byte[] source, byte[] target, int targetIndex)
    {
        target[targetIndex] = (byte) ( (source[0] << 2) | (source[1] >>> 4));
        target[targetIndex + 1] = (byte) ( ( (source[1] & 0x0f) << 4) | (source[2] >>> 2));
        target[targetIndex + 2] = (byte) ( ( (source[2] & 0x03) << 6) | (source[3]));
    }
}
