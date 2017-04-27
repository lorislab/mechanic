package org.lorislab.mechanic.data.elements;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author andrej
 */
@Getter
@Setter
public class AddSystemPropertyElement extends AbsractSystemPropertyElement {

    @Override
    public List<String> createCli() {
        StringBuilder sb = new StringBuilder();
        if (typeValue != null && type != null && !typeValue.isEmpty()) {
            switch (type) {
                case HOST:
                    sb.append("/host=");
                    break;
                case SERVERGROUP:
                    sb.append("/server-group=");
                    break;
            }
            sb.append(typeValue);
        }        
        sb.append("/system-property=").append(name);
        sb.append(":add(value=\"").append(value).append("\")");
        
        return Arrays.asList(sb.toString());
    }
        
}
