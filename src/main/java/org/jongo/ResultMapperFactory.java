/*
 * Copyright (C) 2011 Benoit GUEROUT <bguerout at gmail dot com> and Yves AMSELLEM <amsellem dot yves at gmail dot com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jongo;

import org.bson.io.BasicOutputBuffer;
import org.jongo.marshall.decoder.ReadOnlyDBObject;
import org.jongo.marshall.Unmarshaller;

import com.mongodb.DBEncoder;
import com.mongodb.DBObject;
import com.mongodb.DefaultDBEncoder;

class ResultMapperFactory {


    public static <T> ResultMapper<T> newMapper(final Class<T> clazz, final Unmarshaller unmarshaller) {
        return new DBObjectResultMapper<T>(unmarshaller, clazz);
    }

    public static <T> ResultMapper<T> newPojoMapper(final Class<T> clazz) {
        return new PojoResultMapper<T>(clazz);
    }

    private static class DBObjectResultMapper<T> implements ResultMapper<T> {

        private final Unmarshaller unmarshaller;
        private final Class<T> clazz;

        public DBObjectResultMapper(Unmarshaller unmarshaller, Class<T> clazz) {
            this.unmarshaller = unmarshaller;
            this.clazz = clazz;
        }

        public T map(DBObject result) {
            return unmarshaller.unmarshall(convertToBytesArray(result), 0, clazz);
        }

        private byte[] convertToBytesArray(DBObject result) {
            BasicOutputBuffer buffer = new BasicOutputBuffer();
            DBEncoder dbEncoder = DefaultDBEncoder.FACTORY.create();
            dbEncoder.writeObject(buffer, result);
            return buffer.toByteArray();//TODO close me ?
        }
    }

    private static class PojoResultMapper<T> implements ResultMapper<T> {

        private final Class<T> clazz;

        public PojoResultMapper(Class<T> clazz) {
            this.clazz = clazz;
        }

        public T map(DBObject result) {
            if (!(result instanceof ReadOnlyDBObject)) {
                throw new IllegalArgumentException("PojoResultMapper can only map PojoDBObject instances");
            }
            return ((ReadOnlyDBObject<T>) result).as(clazz);
        }
    }
}
