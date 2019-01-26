package q.rest.cart.model.moyasar;

import com.fasterxml.jackson.annotation.JsonProperty;


public class PaymentRequestCC extends PaymentRequest{
	
	@JsonProperty("source")
	private RequestSourceCC source;
	
	public PaymentRequestCC(){
		super();
	}
	
	public RequestSourceCC getSource() {
		return source;
	}

	public void setSource(RequestSourceCC source) {
		this.source = source;
	}
	
	
	
}
