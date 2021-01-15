package smartlims.testresultmgtsvc.datamanager;
import lombok.Getter;

@Getter
public class LookupPassItem extends LookupItem {
    protected String allCount;
    protected String passCount;
    protected String unPassCount;
    protected String passRate;

    public LookupPassItem (String id, String value, String allCount, String passCount, String unPassCount, String passRate) {
        super(id, value);
        this.allCount = allCount;
        this.passCount = passCount;
        this.unPassCount = unPassCount;
        this.passRate = passRate;
    }
    
}