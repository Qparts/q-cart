package q.rest.cart.model.moyasar;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseSourceCC {
	@JsonProperty("type")
	private String type;
	@JsonProperty("company")
	private String company;
	@JsonProperty("name")
	private String name;
	@JsonProperty("number")
	private String number;//masked
	@JsonProperty("message")
	private String message;//payment gateway message
	@JsonProperty("transaction_url")
	private String transactionURL;//3dsecure
	
	
	
	public String getType() {
		return type;
	}
	public String getCompany() {
		return company;
	}
	public String getName() {
		return name;
	}
	public String getNumber() {
		return number;
	}
	public String getMessage() {
		return message;
	}
	public String getTransactionURL() {
		return transactionURL;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void setTransactionURL(String transactionURL) {
		this.transactionURL = transactionURL;
	}
	
	
	
}
