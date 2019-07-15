package q.rest.cart.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    private char status;//I = initial cart, T = waiting transfer, N = new cart, F = failed to pay cart!
    @Column(name="app_code")
    private int appCode;
    @Column(name="vat_percentage")
    private double vatPercentage;
    @Column(name="payment_method")
    private char paymentMethod;
    @JoinColumn(name="discount_id")
    @ManyToOne
    private Discount discount;
    @Transient
    private List<CartProduct> cartProducts;
    @Transient
    private CartDelivery cartDelivery;
    @Transient
    private List<CartComment> cartComments;
    @Transient
    private List<CartUsedWallet> cartUsedWallets;

    @JsonIgnore
    public double getProductsTotal(){
        double total = 0;
        try{
            for(CartProduct cartProduct : cartProducts){
                total += (cartProduct.getSalesPriceAfterDiscount() * cartProduct.getQuantity());
            }

        }catch(NullPointerException ex){
            total = 0;
        }
        return total;
    }

    @JsonIgnore
    public double getDeliveryFees(){
        try{
            return cartDelivery.getDeliveryCharges();
        }catch(NullPointerException ex){
            return 0;
        }
    }

    @JsonIgnore
    public double getDiscountTotal(){
        try{
            if(discount.getDiscountType() == 'D'){
                return -1 * getDeliveryFees();
            }
            throw new NullPointerException();
        }catch (NullPointerException nu){
            return 0;
        }
    }


    @JsonIgnore
    public double getSubTotal(){
        return getProductsTotal() +  getDeliveryFees() +  getDiscountTotal();
    }

    @JsonIgnore
    public double getVat(){
        return getSubTotal() * vatPercentage;
    }

    @JsonIgnore
    public double getUsedWalletAmount(){
        double total = 0;
        for(CartUsedWallet wallet : cartUsedWallets){
            total += wallet.getCustomerWallet().getAmount();
        }
        return total;
    }

    @JsonIgnore
    public double getGrandTotal(){
        return getSubTotal() + getVat();
    }

    @JsonIgnore
    public double getGrandTotalWithUsedWallet(){
        return getGrandTotal() - getUsedWalletAmount();
    }


    public List<CartComment> getCartComments() {
        return cartComments;
    }

    public void setCartComments(List<CartComment> cartComments) {
        this.cartComments = cartComments;
    }


    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
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

    public List<CartUsedWallet> getCartUsedWallets() {
        return cartUsedWallets;
    }

    public void setCartUsedWallets(List<CartUsedWallet> cartUsedWallets) {
        this.cartUsedWallets = cartUsedWallets;
    }
}
