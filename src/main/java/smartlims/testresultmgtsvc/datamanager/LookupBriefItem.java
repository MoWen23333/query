package smartlims.testresultmgtsvc.datamanager;

import lombok.Getter;

@Getter
public class LookupBriefItem extends LookupItem {
    protected LookupStateItem status;
    protected LookupStateItem sqStatus;

    public LookupBriefItem (String id, String value, LookupStateItem status, LookupStateItem sqStatus) {
        super(id, value);
        this.status = status;
        this.sqStatus = sqStatus;
    }
}