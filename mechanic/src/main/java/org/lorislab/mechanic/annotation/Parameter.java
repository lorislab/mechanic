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
package org.lorislab.mechanic.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The application parameter annotation.
 *
 * @author Andrej Petras
 */
@Target(ElementType.FIELD)
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {

    /**
     * The parameter name.
     *
     * @return the parameter name.
     */
    String name();

    /**
     * Returns {@code true} if the parameter could be use in the property file.
     *
     * @return {@code true} if the parameter could be use in the property file.
     */
    boolean property() default false;

    /**
     * The parameter descriptions.
     *
     * @return the descriptions of the parameter.
     */
    String description() default "";

}
