package q.rest.cart.model.publiccontract;

import java.io.Serializable;
import java.util.List;

public class CartRequest implements Serializable {

    private long customerId;
    private long addressId;
    private double deliveryCharges;
    private Long discountId;
    private Integer preferredCuorier;
    private List<CartItemRequest> cartItems;

    private Integer ccMonth;
    private Integer ccYear;
    private String ccName;
    private String ccNumber;
    private String ccCvc;

    public String getCcCvc() {
        return ccCvc;
    }

    public void setCcCvc(String ccCvc) {
        this.ccCvc = ccCvc;
    }

    public Integer getCcMonth() {
        return ccMonth;
    }

    public void setCcMonth(Integer ccMonth) {
        this.ccMonth = ccMonth;
    }

    public Integer getCcYear() {
        return ccYear;
    }

    public void setCcYear(Integer ccYear) {
        this.ccYear = ccYear;
    }

    public String getCcName() {
        return ccName;
    }

    public void setCcName(String ccName) {
        this.ccName = ccName;
    }

    public String getCcNumber() {
        return ccNumber;
    }

    public void setCcNumber(String ccNumber) {
        this.ccNumber = ccNumber;
    }

    public List<CartItemRequest> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItemRequest> cartItems) {
        this.cartItems = cartItems;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
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

    public double getDeliveryCharges() {
        return deliveryCharges;
    }

    public void setDeliveryCharges(double deliveryCharges) {
        this.deliveryCharges = deliveryCharges;
    }

    public Long getDiscountId() {
        return discountId;
    }

    public void setDiscountId(Long discountId) {
        this.discountId = discountId;
    }

}
