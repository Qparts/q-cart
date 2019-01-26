package q.rest.cart.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name="crt_discount")
public class Discount implements Serializable {

    @Id
    @SequenceGenerator(name = "crt_discount_id_seq_gen", sequenceName = "crt_discount_id_seq", initialValue=1000, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crt_discount_id_seq_gen")
    @Column(name="id")
    private long id;

    @Column(name="dicount_type")
    private char discountType;//D= delivery value , P = discount percentage.

    @Column(name="status")
    private char status;//A=active, U=used, E=expired, D=deactivated

    @Column(name="created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name="expire")
    @Temporal(TemporalType.TIMESTAMP)
    private Date expire;

    @Column(name="created_by")
    private int createdBy;

    @Column(name="code")
    private String code;

    @Column(name="name")
    private String name;//-name associated to the promocode (by default q.parts promocode)

    @Column(name="name_ar")
    private String nameAr;//name associated to the promocode in arabic (by default q.parts promocode)

    @Column(name="reusable")
    private boolean reusable;//cannot be reused after first usage

    @Column(name="customer_specific")
    private boolean customerSpecific;//-- is only for one customer

    @Column(name="customer_id")
    private Long customerId;//only if it is for one customer

    @Column(name="percentage")
    private Double percentage;//--only if type is p (percentage on purchased products)

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public char getDiscountType() {
        return discountType;
    }

    public void setDiscountType(char discountType) {
        this.discountType = discountType;
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

    public Date getExpire() {
        return expire;
    }

    public void setExpire(Date expire) {
        this.expire = expire;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameAr() {
        return nameAr;
    }

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
    }

    public boolean isReusable() {
        return reusable;
    }

    public void setReusable(boolean reusable) {
        this.reusable = reusable;
    }

    public boolean isCustomerSpecific() {
        return customerSpecific;
    }

    public void setCustomerSpecific(boolean customerSpecific) {
        this.customerSpecific = customerSpecific;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }


    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
