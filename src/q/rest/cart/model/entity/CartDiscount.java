package q.rest.cart.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Table(name="crt_cart_discount")
@Entity
public class CartDiscount implements Serializable {


    @Id
    @SequenceGenerator(name = "crt_cart_discount_id_seq_gen", sequenceName = "crt_cart_discount_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crt_cart_discount_id_seq_gen")
    @Column(name="id")
    private long id;
    @Column(name="cart_id")
    private long cartId;
    @Column(name="discount_id")
    private long discountId;
    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(name="created_by")
    private int createdBy;


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

    public long getDiscountId() {
        return discountId;
    }

    public void setDiscountId(long discountId) {
        this.discountId = discountId;
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
}
