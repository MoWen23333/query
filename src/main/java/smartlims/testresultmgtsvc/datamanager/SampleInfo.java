package smartlims.testresultmgtsvc.datamanager;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SampleInfo{

    protected String sampleId;
    public ArrayList<LookupItem> aliquotIdNameList;
    protected String name;
    protected LookupStateItem status;
    protected LookupItem submitter;
    protected LookupItem authoriser;
    protected String createDate;
    protected String completeDate;
}