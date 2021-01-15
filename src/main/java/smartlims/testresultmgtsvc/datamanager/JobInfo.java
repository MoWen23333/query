package smartlims.testresultmgtsvc.datamanager;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JobInfo {
    protected String value;
    protected LookupStateItem status;
    protected LookupStateItem sqstatus;
    protected LookupItem submitter;
    protected LookupItem authoriser;
    protected String createDate;
    protected String completeDate;
}