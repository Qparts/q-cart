package q.rest.cart.model.privatecontract;

import q.rest.cart.model.entity.CartProduct;

import java.util.List;

public class RefundCartRequest {
    private long cartId;
    private long walletId;
    private List<CartProduct> cartProducts;
    private char refundItemType;
    private double deliveryFees;
    private char method;
    private int bankId;
    private int createdBy;

    public long getWalletId() {
        return walletId;
    }

    public void setWalletId(long walletId) {
        this.walletId = walletId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public char getMethod() {
        return method;
    }

    public void setMethod(char method) {
        this.method = method;
    }

    public int getBankId() {
        return bankId;
    }

    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    public long getCartId() {
        return cartId;
    }

    public void setCartId(long cartId) {
        this.cartId = cartId;
    }

    public List<CartProduct> getCartProducts() {
        return cartProducts;
    }

    public void setCartProducts(List<CartProduct> cartProducts) {
        this.cartProducts = cartProducts;
    }

    public char getRefundItemType() {
        return refundItemType;
    }

    public void setRefundItemType(char refundItemType) {
        this.refundItemType = refundItemType;
    }

    public double getDeliveryFees() {
        return deliveryFees;
    }

    public void setDeliveryFees(double deliveryFees) {
        this.deliveryFees = deliveryFees;
    }
}
