package sir.barchable.util;

import java.lang.reflect.Array;

/**
 * Created by sankala on 4/1/16.
 */
public class ArrayUtils {

    public static byte[] join(final byte[]...arrays) {

        int totalLength = 0;
        for(byte[] anArray : arrays) {
            totalLength += anArray.length;
        }
        byte[] result = (byte[]) Array.newInstance(arrays[0].getClass().getComponentType(), totalLength);
        totalLength = 0;
        for(byte[] anArray : arrays) {
            System.arraycopy(anArray, 0, result, totalLength, anArray.length);
            totalLength += anArray.length;
        }
        return result;
    }

}
