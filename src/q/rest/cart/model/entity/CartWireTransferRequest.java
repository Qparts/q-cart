package q.rest.cart.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="crt_cart_wire_transfer_request")
public class CartWireTransferRequest implements Serializable {

    @Id
    @SequenceGenerator(name = "crt_cart_wire_transfer_request_id_seq_gen", sequenceName = "crt_cart_wire_transfer_request_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crt_cart_wire_transfer_request_id_seq_gen")
    @Column(name="id")
    private long id;

    @Column(name="customer_id")
    private long customerId;

    @Column(name="cart_id")
    private long cartId;

    @Column(name="quotation_id")
    private long quotationId;

    @Column(name="wire_type")
    private char wireType;//F = forward, R = reverse

    @Column(name="amount")
    private double amount;

    @Column(name="status")
    private char status;

    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name="created_by")
    private int createdBy;

    @Column(name="processed")
    @Temporal(TemporalType.TIMESTAMP)
    private Date processed;

    @Column(name="processed_by")
    private Integer processedBy;

    @Column(name="payment_purpose")
    private String paymentPurpose;

    @Transient
    private Cart cart;


    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

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

    public long getCartId() {
        return cartId;
    }

    public void setCartId(long cartId) {
        this.cartId = cartId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
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

    public Date getProcessed() {
        return processed;
    }

    public void setProcessed(Date processed) {
        this.processed = processed;
    }

    public Integer getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Integer processedBy) {
        this.processedBy = processedBy;
    }

    public char getWireType() {
        return wireType;
    }

    public void setWireType(char wireType) {
        this.wireType = wireType;
    }

    public String getPaymentPurpose() {
        return paymentPurpose;
    }

    public void setPaymentPurpose(String paymentPurpose) {
        this.paymentPurpose = paymentPurpose;
    }

    public long getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(long quotationId) {
        this.quotationId = quotationId;
    }
}
