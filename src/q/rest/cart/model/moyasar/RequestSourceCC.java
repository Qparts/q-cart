package q.rest.cart.model.moyasar;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestSourceCC {

	@JsonProperty("type")
	private String type;
	@JsonProperty("name")
	private String name;
	@JsonProperty("number")
	private String number;
	@JsonProperty("cvc")
	private String cvc;
	@JsonProperty("month")
	private int month;
	@JsonProperty("year")
	private int year;
	
	
	public String getType() {
		return type;
	}
	public String getName() {
		return name;
	}
	public String getNumber() {
		return number;
	}
	public String getCvc() {
		return cvc;
	}
	public int getMonth() {
		return month;
	}
	public int getYear() {
		return year;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public void setCvc(String cvc) {
		this.cvc = cvc;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public void setYear(int year) {
		this.year = year;
	}
	
	

}
