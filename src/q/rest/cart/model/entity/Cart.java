package q.rest.cart.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="crt_cart")
public class Cart implements Serializable {


    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "crt_cart_id_seq_gen", sequenceName = "crt_cart_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crt_cart_id_seq_gen")
    @Column(name="id")
    private long id;
    @Column(name="customer_id")
    private long customerId;
    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(name="created_by")
    private int createdBy;
    @Column(name="status")
    private char status;
    @Column(name="app_code")
    private int appCode;
    @Column(name="vat_percentage")
    private double vatPercentage;
    @Column(name="payment_method")
    private char paymentMethod;
    @Transient
    private List<CartProduct> cartProducts;
    @Transient
    private CartDelivery cartDelivery;
    @Transient
    private CartDiscount cartDiscount;


    public CartDiscount getCartDiscount() {
        return cartDiscount;
    }

    public void setCartDiscount(CartDiscount cartDiscount) {
        this.cartDiscount = cartDiscount;
    }

    public CartDelivery getCartDelivery() {
        return cartDelivery;
    }

    public void setCartDelivery(CartDelivery cartDelivery) {
        this.cartDelivery = cartDelivery;
    }

    public List<CartProduct> getCartProducts() {
        return cartProducts;
    }

    public void setCartProducts(List<CartProduct> cartProducts) {
        this.cartProducts = cartProducts;
    }

    public char getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(char paymentMethod) {
        this.paymentMethod = paymentMethod;
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

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    public int getAppCode() {
        return appCode;
    }

    public void setAppCode(int appCode) {
        this.appCode = appCode;
    }

    public double getVatPercentage() {
        return vatPercentage;
    }

    public void setVatPercentage(double vatPercentage) {
        this.vatPercentage = vatPercentage;
    }


}
