package smartlims.testresultmgtsvc.datamanager;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AliquotInfo{
	protected String aliquotId;
	protected String subGroup;
	protected String testItem;
	protected String clientUser;
	protected LookupItem tester;
	protected LookupItem completer;
	protected String productText;
	protected String testGather;
	protected String requiredDate;
	protected String name;
    protected LookupStateItem status;
    protected LookupItem submitter;
    protected LookupItem authoriser;
    protected String createDate;
    protected String completeDate;
	protected LookupStateItem sqStatus;
}