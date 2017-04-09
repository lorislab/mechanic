/*
 * Copyright 2017 lorislab.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.lorislab.mechanic.app;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * The parameter meta data.
 *
 * @author Andrej Petras
 */
@Getter
@Setter
@ToString
public class ParameterMetaData {

    /**
     * The name.
     */
    private String name;

    /**
     * The parameter.
     */
    private String parameter;

    /**
     * The property flag.
     */
    private boolean property;

    /**
     * The description.
     */
    private String description;

    /**
     * The corresponding field of the java object.
     */
    private Field field;

    /**
     * The type of the parameter.
     */
    private Type type;

    /**
     * The parameter type.
     */
    public enum Type {

        /**
         * The path.
         */
        PATH(Path.class),
        /**
         * The string.
         */
        STRING(String.class),
        /**
         * The boolean.
         */
        BOOLEAN(boolean.class),
        /**
         * The list of strings.
         */
        LIST(List.class),
        /**
         * The integer.
         */
        INTEGER(Integer.class);

        /**
         * The corresponding class.
         */
        private Class clazz;

        /**
         * The default constructor.
         *
         * @param clazz the corresponding class.
         */
        private Type(Class clazz) {
            this.clazz = clazz;
        }

        /**
         * Returns {@code true} if the {@code clazz} is the type.
         *
         * @param clazz the class.
         * @return {@code true} if the input parameter {@code clazz} is the
         * class of the type.
         */
        public boolean isClazz(Class clazz) {
            return this.clazz.equals(clazz);
        }
    }

}
