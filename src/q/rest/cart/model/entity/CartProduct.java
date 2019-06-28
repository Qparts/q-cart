package q.rest.cart.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name="crt_cart_product")
public class CartProduct implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @SequenceGenerator(name = "crt_cart_product_id_seq_gen", sequenceName = "crt_cart_product_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crt_cart_product_id_seq_gen")
    @Column(name="id")
    private long id;
    @Column(name="cart_id")
    private long cartId;
    @Column(name="product_id")
    private long productId;
    @Column(name="quantity")
    private int quantity;
    @Column(name="sales_price")
    private double salesPrice;
    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(name="created_by")
    private int createdBy;
    @Column(name="status")
    private char status;
    @JoinColumn(name="discount_id")
    @ManyToOne
    private Discount discount;
    @Transient
    private Cart cart;

    @Transient
    private List<CartProductCompare> cartProductCompares;


    @JsonIgnore
    public double getSalesPriceAfterDiscount(){
        if(discount != null){
            if(discount.getDiscountType() == 'P'){
                return salesPrice - (discount.getPercentage() * salesPrice);
            }
        }
        return salesPrice;
    }


    @JsonIgnore
    public double getDiscountValue(){
        if(discount != null){
            if(discount.getDiscountType() == 'P'){
                return discount.getPercentage() * salesPrice;
            }
        }
        return 0;
    }


    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(double salesPrice) {
        this.salesPrice = salesPrice;
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

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public List<CartProductCompare> getCartProductCompares() {
        return cartProductCompares;
    }

    public void setCartProductCompares(List<CartProductCompare> cartProductCompares) {
        this.cartProductCompares = cartProductCompares;
    }
}
