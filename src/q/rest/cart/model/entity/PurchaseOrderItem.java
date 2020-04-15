package q.rest.cart.model.entity;


import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "crt_purchase_order_item")
public class PurchaseOrderItem implements Serializable {

    @Id
    @SequenceGenerator(name = "crt_purchase_order_item_id_seq_gen", sequenceName = "crt_purchase_order_item_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crt_purchase_order_item_id_seq_gen")
    @Column(name="id")
    private long id;
    @Column(name="purchase_order_id")
    private long purchaseOrderId;
    @Column(name="item_number")
    private String itemNumber;
    @Column(name="quantity")
    private int quantity;
    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(name="status")
    private char status;
    @Column(name="brand")
    private String brand;
    @Column(name="retail_price")
    private double retailPrice;
    @Column(name="special_offer")
    private boolean specialOffer;
    @Column(name="special_offer_price")
    private double specialOfferPrice;
    @Column(name="wholesales_price")
    private double wholesalesPrice;
    @Column(name="factor")
    private double factor;
    @Column(name="policy_name")
    private String policyName;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getItemNumber() {
        return itemNumber;
    }

    public void setItemNumber(String itemNumber) {
        this.itemNumber = itemNumber;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(double retailPrice) {
        this.retailPrice = retailPrice;
    }

    public boolean isSpecialOffer() {
        return specialOffer;
    }

    public void setSpecialOffer(boolean specialOffer) {
        this.specialOffer = specialOffer;
    }

    public double getSpecialOfferPrice() {
        return specialOfferPrice;
    }

    public void setSpecialOfferPrice(double specialOfferPrice) {
        this.specialOfferPrice = specialOfferPrice;
    }

    public double getWholesalesPrice() {
        return wholesalesPrice;
    }

    public void setWholesalesPrice(double wholesalesPrice) {
        this.wholesalesPrice = wholesalesPrice;
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }
}
