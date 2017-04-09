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
package org.lorislab.mechanic.data;

import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Andrej Petras
 */
public class ExpressionDataService {

    private static final Pattern EXPRESSION_PATTERN = Pattern.compile("(\\$\\{[^\\}]+\\})");

    private ExpressionDataService() {
    }
    
    public static String processExpressions(String text, final Properties properties, final Set<String> propertiesKeys, final Set<String> usedKeys) {
        Matcher matcher = EXPRESSION_PATTERN.matcher(text);
        String originalText = text;
        while (matcher.find()) {
            String expressionString = originalText.substring(matcher.start(), matcher.end());
            String valueTolookup = expressionString.replaceFirst("\\$\\{", "").replaceFirst("\\}$", "");
            if (propertiesKeys.remove(valueTolookup)) {
                String value = properties.getProperty(valueTolookup, "");
                text = text.replace(expressionString, value);
                
                usedKeys.remove(valueTolookup);
            }
        }
        return text;
    }

}
