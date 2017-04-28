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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lorislab.mechanic.logger.LoggerFactory;
import org.lorislab.mechanic.xml.model.Change;
import org.lorislab.mechanic.xml.model.ChangeLog;
import org.lorislab.mechanic.xml.model.FileType;

/**
 *
 * @author Andrej Petras
 */
public class ChangeLogDataService {
    
    public static final String ALL_PROFILES = "*";
    
    private ChangeLogDataService() {
    }

    public static ChangeLogData loadChangeLogData(Path file, List<String> profiles) {
        Logger LOGGER = LoggerFactory.getLogger(ChangeLogDataService.class);
        
        Set<String> tmp = new HashSet<>();
        if (profiles != null) {
            tmp.addAll(profiles);
        }
        tmp.add(null);
        
        LOGGER.log(Level.FINE, "Profiles: {0}", tmp);
        
        ChangeLogData result = loadChangeLogDataP(file, tmp);
        if (result == null) {
            throw new RuntimeException("The change log data is null!");
        }

        List<ChangeData> changes = result.getChanges();
        if (changes != null && !changes.isEmpty()) {
            changes.forEach((change) -> {
                LOGGER.log(Level.FINE, "Change id:{0} author:{1}", new Object[]{ change.getId(), change.getAuthor() });
                List<Path> clis = change.getCliFiles();
                if (clis != null) {
                    clis.forEach((cli) -> {
                        LOGGER.log(Level.FINE, "Cli: {0}", cli);
                    });
                }
            });
        } else {
            LOGGER.log(Level.FINE, "The list of changes is empty! ChangeLogFile: {0}", file);
        }
        return result;
    }
    
    private static ChangeLogData loadChangeLogDataP(Path file, Set<String> profiles) {
        Logger LOGGER = LoggerFactory.getLogger(ChangeLogDataService.class);
        
        ChangeLogData result = new ChangeLogData();
        result.setPath(file);

        Path parent = file.getParent();
        LOGGER.log(Level.FINE, "Open change log file: {0}", file);
        try {
            ChangeLog cl = ChangeLogService.loadChangeLog(file);
            if (cl != null) {

                List<Object> items = cl.getIncludeOrChange();
                if (items != null && !items.isEmpty()) {
                    for (int i = 0; i < items.size(); i++) {
                        Object item = items.get(i);
                        if (item instanceof FileType) {

                            Path include = getPath(parent, (FileType) item);                            
                            
                            // recursive
                            LOGGER.log(Level.FINE, "Open child change log file: {0}", include);
                            ChangeLogData child = loadChangeLogDataP(include, profiles);
                            result.addChangeDataAll(child.getChanges());

                        } else if (item instanceof Change) {

                            Change ch = (Change) item;
                            if (profiles.contains(ALL_PROFILES) || profiles.contains(ch.getProfile())) {
                                ChangeData chd = new ChangeData();
                                chd.setAuthor(ch.getAuthor());
                                chd.setId(ch.getId());
                                chd.setProfile(ch.getProfile());
                                chd.setParent(result);
                                chd.setBatch(ch.isBatch());
                                
                                List<FileType> clis = ch.getCli();
                                if (clis != null && !clis.isEmpty()) {
                                    for (int j = 0; j < clis.size(); j++) {
                                        Path path = getPath(parent, clis.get(j));                                             
                                        chd.addCliFile(path);
                                    }
                                }
                                result.addChangeData(chd);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error loading the change log file: " + file, ex);
        }
        return result;
    }

    private static Path getPath(Path parent, FileType ft) {
        Path result = null;
        if (ft != null) {
            result = Paths.get(ft.getFile());
            if (ft.isRelativePath()) {
                result = parent.resolve(ft.getFile());
            }
            
            if (!Files.exists(result)) {
                throw new RuntimeException("The file " + result + " does not exists!.");
            }
        }
        return result;
    }
}
