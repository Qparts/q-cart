package q.rest.cart.model.entity;

import java.io.Serializable;
import java.util.Date;

public class CustomerWallet implements Serializable {

    private long id;
    private long customerId;
    private double amount;
    private char method;
    private double creditCharges;
    private String gateway;
    private Date created;
    private int createdBy;
    private String transactionId;
    private String currency;
    private char walletType;
    private String ccCompany;
    private Integer bankId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public char getMethod() {
        return method;
    }

    public void setMethod(char method) {
        this.method = method;
    }

    public double getCreditCharges() {
        return creditCharges;
    }

    public void setCreditCharges(double creditCharges) {
        this.creditCharges = creditCharges;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public char getWalletType() {
        return walletType;
    }

    public void setWalletType(char walletType) {
        this.walletType = walletType;
    }

    public String getCcCompany() {
        return ccCompany;
    }

    public void setCcCompany(String ccCompany) {
        this.ccCompany = ccCompany;
    }

    public Integer getBankId() {
        return bankId;
    }

    public void setBankId(Integer bankId) {
        this.bankId = bankId;
    }
}
