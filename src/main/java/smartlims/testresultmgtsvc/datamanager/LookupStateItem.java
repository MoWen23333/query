package smartlims.testresultmgtsvc.datamanager;

import lombok.Getter;

@Getter
public class LookupStateItem extends LookupItem{
    protected String state;

    public LookupStateItem(String id, String value, String state) {
        super(id, value);
        this.state = state;
    }
}