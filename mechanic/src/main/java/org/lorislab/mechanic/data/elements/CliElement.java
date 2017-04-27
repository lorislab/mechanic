package org.lorislab.mechanic.data.elements;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author andrej
 */
@Getter
@Setter
@ToString
public class CliElement implements ChangeDataElement {

    private static final Pattern LTRIM = Pattern.compile("\\s+$");
    
    private Path cliFile;

    @Override
    public String getDebugLog() {
        StringBuilder sb = new StringBuilder();
        if (cliFile != null) {
            sb.append("Cli: ").append(cliFile);
        }
        return sb.toString();
    }

    @Override
    public List<String> createCli() {
        List<String> result = null;
        if (cliFile == null || !Files.exists(cliFile)) {
            throw new RuntimeException("The cli file does not exists! " + cliFile);
        }
        try (Stream<String> file = Files.lines(cliFile)) {
            result = file.map(line -> LTRIM.matcher(line).replaceAll("")).collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error reading the CLI file! " + cliFile);
        }
        return result;
    }

}
