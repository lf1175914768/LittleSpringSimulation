package test.beans;

public class NestedTestBean implements INestedTestBean {
	
	private String company = "";
	
	public NestedTestBean() {}
	
	public NestedTestBean(String company) {
		setCompany(company);
	}
	
	public void setCompany(String company) {
		this.company = (company != null ? company : "");
	}

	public String getCompany() {
		return company;
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof NestedTestBean)) {
			return false;
		}
		NestedTestBean ntb = (NestedTestBean) obj;
		return this.company.equals(ntb.company);
	}

	public int hashCode() {
		return this.company.hashCode();
	}

	public String toString() {
		return "NestedTestBean: " + this.company;
	}

}
