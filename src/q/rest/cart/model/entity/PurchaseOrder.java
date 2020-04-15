package q.rest.cart.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "crt_purchase_order")
public class PurchaseOrder implements Serializable {
    @Id
    @SequenceGenerator(name = "crt_purchase_order_id_seq_gen", sequenceName = "crt_purchase_order_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crt_purchase_order_id_seq_gen")
    @Column(name="id")
    private long id;
    @Column(name="vendor_id")
    private int vendorId;
    @Column(name="created_by_vendor_id")
    private int createdByVendorUser;
    @Column(name="target_vendor_id")
    private int targetVendorId;
    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    @Column(name="status")
    private char status;
    @Column(name="notes")
    private String note;
    @Transient
    private List<PurchaseOrderItem> items;

    public PurchaseOrder(){
        items = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getVendorId() {
        return vendorId;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public int getCreatedByVendorUser() {
        return createdByVendorUser;
    }

    public void setCreatedByVendorUser(int createdByVendorUser) {
        this.createdByVendorUser = createdByVendorUser;
    }

    public int getTargetVendorId() {
        return targetVendorId;
    }

    public void setTargetVendorId(int targetVendorId) {
        this.targetVendorId = targetVendorId;
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

    public List<PurchaseOrderItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseOrderItem> items) {
        this.items = items;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
