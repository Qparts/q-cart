package q.rest.cart.model.moyasar;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonSubTypes({
    @JsonSubTypes.Type(value = PaymentResponseCC.class, name = "source")
})
public class PaymentResponse {
	@JsonProperty("id")
	private String id;//paymentId
	@JsonProperty("status")
	private String status;//inititated
	@JsonProperty("amount")
	private Integer amount;//in halalas
	@JsonProperty("fee")
	private Integer fee;//transaction fees in halalas
	@JsonProperty("currency")
	private String currency;//SAR
	@JsonProperty("refunded")
	private Integer refunded;//0 in payment
	@JsonProperty("refunded_at")
	private String refundedAt;//null is default
	@JsonProperty("captured")
	private Integer captured;
	@JsonProperty("captured_at")
	private String capturedAt;
	@JsonProperty("voided_at")
	private String voidedAt;
	@JsonProperty("description")
	private String description;//payment desc
	@JsonProperty("amount_format")
	private String amountFormat;
	@JsonProperty("fee_format")
	private String feeFormat;
	@JsonProperty("refunded_format")
	private String refundedFormat;
	@JsonProperty("captured_format")
	private String capturedFormat;
	@JsonProperty("invoice_id")
	private String invoiceId;//null is default
	@JsonProperty("ip")
	private String ip;//null is default
	@JsonProperty("callback_url")
	private String callback;//callbackURL for client
	@JsonProperty("created_at")
	private String createdAt;//creation timestamp in ISO 8601 format.
	@JsonProperty("updated_at")
	private String updatedAt;//modification timestamp in ISO 8601 format.
	
	
	public String getId() {
		return id;
	}
	public String getStatus() {
		return status;
	}
	public Integer getAmount() {
		return amount;
	}
	public Integer getFee() {
		return fee;
	}
	public String getCurrency() {
		return currency;
	}
	public Integer getRefunded() {
		return refunded;
	}
	public String getRefundedAt() {
		return refundedAt;
	}
	public String getDescription() {
		return description;
	}
	public String getInvoiceId() {
		return invoiceId;
	}
	public String getIp() {
		return ip;
	}
	public String getCallback() {
		return callback;
	}
	public String getCreatedAt() {
		return createdAt;
	}
	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setId(String id) {
		this.id = id;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	public void setFees(Integer fee) {
		this.fee = fee;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public void setRefunded(Integer refunded) {
		this.refunded = refunded;
	}
	public void setRefundedAt(String refundedAt) {
		this.refundedAt = refundedAt;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public void setCallback(String callback) {
		this.callback = callback;
	}
	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}
	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}
	public String getAmountFormat() {
		return amountFormat;
	}
	public void setAmountFormat(String amountFormat) {
		this.amountFormat = amountFormat;
	}
	public String getFeeFormat() {
		return feeFormat;
	}
	public void setFeeFormat(String feeFormat) {
		this.feeFormat = feeFormat;
	}
	public String getRefundedFormat() {
		return refundedFormat;
	}
	public void setRefundedFormat(String refundedFormat) {
		this.refundedFormat = refundedFormat;
	}
	public void setFee(Integer fee) {
		this.fee = fee;
	}

	public Integer getCaptured() {
		return captured;
	}

	public void setCaptured(Integer captured) {
		this.captured = captured;
	}

	public String getCapturedAt() {
		return capturedAt;
	}

	public void setCapturedAt(String capturedAt) {
		this.capturedAt = capturedAt;
	}

	public String getVoidedAt() {
		return voidedAt;
	}

	public void setVoidedAt(String voidedAt) {
		this.voidedAt = voidedAt;
	}

	public String getCapturedFormat() {
		return capturedFormat;
	}

	public void setCapturedFormat(String capturedFormat) {
		this.capturedFormat = capturedFormat;
	}
}
