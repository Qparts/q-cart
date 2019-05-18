package q.rest.cart.model.entity;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name="crt_cart_product_compare")
@Entity
public class CartProductCompare implements Serializable {

    @Id
    @SequenceGenerator(name = "crt_cart_product_compare_id_seq_gen", sequenceName = "crt_cart_product_compare_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crt_cart_product_compare_id_seq_gen")
    @Column(name = "id")
    private long id;
    @Column(name="cart_product_id")
    private long cartProductId;
    @Column(name="vendor_id")
    private Integer vendorId;
    @Column(name="cost")
    private Double cost;
    @Column(name="created_by")
    private Integer createdBy;
    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getCartProductId() {
        return cartProductId;
    }
    public void setCartProductId(long cartProductId) {
        this.cartProductId = cartProductId;
    }
    public Integer getVendorId() {
        return vendorId;
    }
    public void setVendorId(Integer vendorId) {
        this.vendorId = vendorId;
    }
    public Double getCost() {
        return cost;
    }
    public void setCost(Double cost) {
        this.cost = cost;
    }
    public Integer getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }

}
