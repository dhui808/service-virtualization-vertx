package webservicemockutil;

import java.util.ArrayList;
import java.util.List;

public class EntryMapping {

	private String entryPageUrl;
	private List<Flow> flows;
	
	public List<String> getFlowNames() {
		List<String> list = new ArrayList<String>();
		
		for (Flow flow: flows) {
			list.add(flow.getFlow());
		}
		
		return list;
	}
	
	public String getEntryPageUrl() {
		return entryPageUrl;
	}
	public void setEntryPageUrl(String entryPageUrl) {
		this.entryPageUrl = entryPageUrl;
	}
	public List<Flow> getFlows() {
		return flows;
	}
	public void setFlows(List<Flow> flows) {
		this.flows = flows;
	}
}

class Flow {
	private String flow;
	private String signaturePath;
	
	public String getFlow() {
		return flow;
	}
	public void setFlow(String flow) {
		this.flow = flow;
	}
	public String getSignaturePath() {
		return signaturePath;
	}
	public void setSignaturePath(String signaturePath) {
		this.signaturePath = signaturePath;
	}
}
