package q.rest.cart.model.moyasar;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentResponseCC extends PaymentResponse{

	public PaymentResponseCC(){
		super();
	}
	
	@JsonProperty("source")
	private ResponseSourceCC source;

	public ResponseSourceCC getSource() {
		return source;
	}

	public void setSource(ResponseSourceCC source) {
		this.source = source;
	}

}
