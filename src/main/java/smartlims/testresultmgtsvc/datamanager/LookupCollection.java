package smartlims.testresultmgtsvc.datamanager;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LookupCollection {
    private ArrayList<LookupItem> staffs;
	private ArrayList<LookupStateItem> sampleStatus;
	private ArrayList<LookupStateItem> jobStatus;
	private ArrayList<LookupStateItem> sqStatus;
	private ArrayList<LookupItem> instStatus;
	private ArrayList<LookupItem> group;	
	private ArrayList<LookupItem> testItems;
	private ArrayList<LookupItem> applicant;
}