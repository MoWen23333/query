package smartlims.testresultmgtsvc.datamanager;

public class ResultPackage_JSON {
	private String state;
	private Object result;

	public ResultPackage_JSON(String state, Object result) {
		this.state = state;
		this.result = result;
	}

	public String getState() {
		return state;
	}

	public Object getResult() {
		return result;
	}
}
