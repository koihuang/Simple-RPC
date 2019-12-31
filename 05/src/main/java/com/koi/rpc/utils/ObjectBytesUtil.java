package com.koi.rpc.utils;

import java.io.*;
import java.util.List;

/**
 * @author whuang
 * @date 2019/12/23
 */
public final class ObjectBytesUtil {
    private ObjectBytesUtil() {
    }

    public static Object bytesToObject(List<Byte> byteList) throws IOException, ClassNotFoundException {
        int size = byteList.size();
        byte[] bytes1 = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes1[i] = byteList.get(i);
        }
        ByteArrayInputStream bi = new ByteArrayInputStream(bytes1);
        ObjectInputStream oi = new ObjectInputStream(bi);

        Object obj = oi.readObject();

        bi.close();
        oi.close();

        return obj;
    }

    public static byte[] objectToBytes(Object obj) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        byte[] bytes = bos.toByteArray();
        oos.close();
        bos.close();
        return bytes;
    }
}
