package q.rest.cart.model.entity;

import q.rest.cart.model.moyasar.PaymentResponseCC;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="crt_cart_gateway_first_response")
public class CartGatewayFirstResponse implements Serializable {

    @Id
    @SequenceGenerator(name = "crt_cart_gateway_first_response_id_seq_gen", sequenceName = "crt_cart_gateway_first_response_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crt_cart_gateway_first_response_id_seq_gen")
    @Column(name="id")
    private long id;

    @Column(name="status")
    private char status;//I=initial state, P=paid, F=failed

    @Column(name="cart_id")
    private long cartId;

    @Column(name="customer_id")
    private long customerId;

    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name="created_by")
    private int createdBy;

    @Column(name="g_payment_id")
    private String gPaymentId;

    @Column(name="g_status")
    private String gStatus;

    @Column(name="g_amount")
    private Integer gAmount;

    @Column(name="g_fee")
    private Integer gFee;

    @Column(name="g_currency")
    private String gCurrency;

    @Column(name="g_description")
    private String gDiscription;

    @Column(name="g_callback")
    private String gCallback;

    @Column(name="g_type")
    private String gType;

    @Column(name="g_company")
    private String gCompany;

    @Column(name="g_name")
    private String gName;

    @Column(name="g_number")
    private String gNumber;

    @Column(name="g_message")
    private String gMessage;

    @Column(name="g_transaction_url")
    private String gTransactionUrl;


    public CartGatewayFirstResponse() {
    }

    public CartGatewayFirstResponse(PaymentResponseCC ccr, Cart cart, int createdBy) {
        this.cartId = cart.getId();
        this.customerId = cart.getCustomerId();
        this.created = new Date();
        this.createdBy = createdBy;
        this.gPaymentId = ccr.getId();
        this.gStatus = ccr.getStatus();

        if(gStatus.equals("initiated"))
            this.status = 'I';//initiated
        else if(gStatus.equals("succeeded"))
            this.status = 'P';//paid
        else
            this.status = 'F';//failed
        this.gAmount = ccr.getAmount();
        this.gFee = ccr.getFee();
        this.gCurrency = ccr.getCurrency();
        this.gDiscription = ccr.getDescription();
        this.gCallback = ccr.getCallback();
        this.gType = ccr.getSource().getType();
        this.gCompany = ccr.getSource().getCompany();
        this.gName = ccr.getSource().getName();
        this.gNumber = ccr.getSource().getNumber();
        this.gMessage = ccr.getSource().getMessage();
        this.gTransactionUrl = ccr.getSource().getTransactionURL();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCartId() {
        return cartId;
    }

    public void setCartId(long cartId) {
        this.cartId = cartId;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
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

    public void setCreatedby(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getgPaymentId() {
        return gPaymentId;
    }

    public void setgPaymentId(String gPaymentId) {
        this.gPaymentId = gPaymentId;
    }

    public String getgStatus() {
        return gStatus;
    }

    public void setgStatus(String gStatus) {
        this.gStatus = gStatus;
    }

    public Integer getgAmount() {
        return gAmount;
    }

    public void setgAmount(Integer gAmount) {
        this.gAmount = gAmount;
    }

    public Integer getgFee() {
        return gFee;
    }

    public void setgFee(Integer gFee) {
        this.gFee = gFee;
    }

    public String getgCurrency() {
        return gCurrency;
    }

    public void setgCurrency(String gCurrency) {
        this.gCurrency = gCurrency;
    }

    public String getgDiscription() {
        return gDiscription;
    }

    public void setgDiscription(String gDiscription) {
        this.gDiscription = gDiscription;
    }

    public String getgCallback() {
        return gCallback;
    }

    public void setgCallback(String gCallback) {
        this.gCallback = gCallback;
    }

    public String getgType() {
        return gType;
    }

    public void setgType(String gType) {
        this.gType = gType;
    }

    public String getgCompany() {
        return gCompany;
    }

    public void setgCompany(String gCompany) {
        this.gCompany = gCompany;
    }

    public String getgName() {
        return gName;
    }

    public void setgName(String gName) {
        this.gName = gName;
    }

    public String getgNumber() {
        return gNumber;
    }

    public void setgNumber(String gNumber) {
        this.gNumber = gNumber;
    }

    public String getgMessage() {
        return gMessage;
    }

    public void setgMessage(String gMessage) {
        this.gMessage = gMessage;
    }

    public String getgTransactionUrl() {
        return gTransactionUrl;
    }

    public void setgTransactionUrl(String gTransactionUrl) {
        this.gTransactionUrl = gTransactionUrl;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }
}
