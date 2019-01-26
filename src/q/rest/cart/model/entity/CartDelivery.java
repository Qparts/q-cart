package q.rest.cart.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="crt_cart_delivery")
public class CartDelivery implements Serializable {

    @Id
    @SequenceGenerator(name = "crt_cart_delivery_id_seq_gen", sequenceName = "crt_cart_delivery_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crt_cart_delivery_id_seq_gen")
    @Column(name="id")
    private long id;
    @Column(name="cart_id")
    private long cartId;
    @Column(name="address_id")
    private long addressId;
    @Column(name="delivery_charges")
    private double deliveryCharges;
    @Column(name="preferred_courier")
    private Integer preferredCuorier;
    @Column(name="status")
    private char status;
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

    public long getAddressId() {
        return addressId;
    }

    public void setAddressId(long addressId) {
        this.addressId = addressId;
    }

    public Integer getPreferredCuorier() {
        return preferredCuorier;
    }

    public void setPreferredCuorier(Integer preferredCuorier) {
        this.preferredCuorier = preferredCuorier;
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

    public double getDeliveryCharges() {
        return deliveryCharges;
    }

    public void setDeliveryCharges(double deliveryCharges) {
        this.deliveryCharges = deliveryCharges;
    }
}
