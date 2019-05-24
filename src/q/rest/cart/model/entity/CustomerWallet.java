package q.rest.cart.model.entity;

import javax.inject.Named;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name="crt_customer_wallet")
@Entity
public class CustomerWallet implements Serializable {

    @Id
    @Column(name="id")
    @SequenceGenerator(name = "crt_customer_wallet_id_seq_gen", sequenceName = "crt_customer_wallet_id_seq", initialValue=1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crt_customer_wallet_id_seq_gen")
    private long id;
    @Column(name="customer_id")
    private long customerId;
    @Column(name="amount")
    private double amount;
    @Column(name="payment_method")
    private char method;//C = credit card, W= wire transfer, M = mada, C = credit!
    @Column(name="credit_charges")
    private double creditCharges;
    @Column(name="gateway")
    private String gateway;
    @Column(name="created")
    private Date created;
    @Column(name="created_by")
    private int createdBy;
    @Column(name="transaction_id")
    private String transactionId;
    @Column(name="currency")
    private String currency;
    @Column(name="wallet_type")
    private char walletType;////P = payment, S = sales, R = refund, T = return
    @Column(name="cc_company")
    private String ccCompany;
    @Column(name="bank_id")
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
