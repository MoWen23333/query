package smartlims.testresultmgtsvc.datamanager;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InstrumentInfo {
    protected String instrumentId;
    protected String instrumentName;
    protected String fixedDasSetNum;
    protected LookupItem instStatus;
    protected String installSite;
    protected LookupItem inchargeBy;
    protected LookupItem techChargeBy;
}