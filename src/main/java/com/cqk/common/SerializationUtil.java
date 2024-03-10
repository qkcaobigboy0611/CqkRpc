/**
 * @author qkcao
 * @date 2023/7/28 16:56
 */
package com.cqk.common;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

public class SerializationUtil {
    public static <T> byte[] serialize(T object) {
        RuntimeSchema<T> schema = RuntimeSchema.createFrom((Class<T>) object.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        byte[] bytes;
        try {
            bytes = ProtostuffIOUtil.toByteArray(object, schema, buffer);
        } finally {
            buffer.clear();
        }
        return bytes;
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        RuntimeSchema<T> schema = RuntimeSchema.createFrom(clazz);
        T object = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, object, schema);
        return object;
    }
}
