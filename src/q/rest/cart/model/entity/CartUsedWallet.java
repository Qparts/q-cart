package q.rest.cart.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="crt_cart_used_wallet")
public class CartUsedWallet implements Serializable {

    @Id
    @SequenceGenerator(name = "crt_cart_used_wallet_id_seq_gen", sequenceName = "crt_cart_used_wallet_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crt_cart_used_wallet_id_seq_gen")
    @Column(name="id")
    private long id;
    @Column(name="cart_id")
    private long cartId;
    @Column(name="created_by")
    private int createdBy;
    @Column(name="status")
    private char status;
    @JoinColumn(name="wallet_id")
    @ManyToOne
    private CustomerWallet customerWallet;

    public CartUsedWallet() {
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

    public CustomerWallet getCustomerWallet() {
        return customerWallet;
    }

    public void setCustomerWallet(CustomerWallet customerWallet) {
        this.customerWallet = customerWallet;
    }
}


