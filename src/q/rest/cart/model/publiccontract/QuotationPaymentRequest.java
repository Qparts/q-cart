package q.rest.cart.model.publiccontract;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class QuotationPaymentRequest {

    private long customerId;
    private CardHolder cardHolder;
    private long quotationId;
    private char paymentMethod;
    private double amount;

    @JsonIgnore
    public String getSourceString(){
        switch (paymentMethod){
            case 'W':
                return "wiretransfer";
            case 'M':
            case 'V':
                return "creditcard";
            default:
                return "";
        }
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public CardHolder getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(CardHolder cardHolder) {
        this.cardHolder = cardHolder;
    }

    public long getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(long quotationId) {
        this.quotationId = quotationId;
    }

    public char getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(char paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
