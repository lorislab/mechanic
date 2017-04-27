package org.lorislab.mechanic.data.elements;

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
public abstract class AbsractSystemPropertyElement implements ChangeDataElement {
    
    protected String name;
    
    protected Type type;
    
    protected String typeValue;

    protected String value;
    
    @Override
    public String getDebugLog() {
        return toString();
    }

    public enum Type {
        HOST, SERVERGROUP;
    }
}
