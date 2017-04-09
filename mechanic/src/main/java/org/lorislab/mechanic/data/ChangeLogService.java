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

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.lorislab.mechanic.xml.model.ChangeLog;

/**
 *
 * @author Andrej Petras
 */
public class ChangeLogService {

    private static final Schema SCHEMA;
    
    private static final Unmarshaller UM;
    
    static {
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            URL url = ChangeLogService.class.getResource("/xsd/mechanic_1_0.xsd");
            SCHEMA = schemaFactory.newSchema(url);        
            
            JAXBContext context = JAXBContext.newInstance(ChangeLog.class);
            UM = context.createUnmarshaller();
            UM.setSchema(SCHEMA);            
        } catch (Exception ex) {
            throw new RuntimeException("Error reading the XSD for the validation!", ex);
        }
    }
    
    private ChangeLogService() {
    }

    public static ChangeLog loadChangeLog(Path path) throws Exception {
        ChangeLog result = null;
        if (Files.exists(path)) {
            result = (ChangeLog) UM.unmarshal(path.toFile());
        }
        return result;
    }
}
