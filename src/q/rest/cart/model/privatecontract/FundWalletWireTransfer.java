package q.rest.cart.model.privatecontract;

import q.rest.cart.model.entity.CartWireTransferRequest;
import q.rest.cart.model.entity.CustomerWallet;

public class FundWalletWireTransfer {
    private CartWireTransferRequest wireTransfer;
    private CustomerWallet wallet;


    public CartWireTransferRequest getWireTransfer() {
        return wireTransfer;
    }

    public void setWireTransfer(CartWireTransferRequest wireTransfer) {
        this.wireTransfer = wireTransfer;
    }

    public CustomerWallet getWallet() {
        return wallet;
    }

    public void setWallet(CustomerWallet wallet) {
        this.wallet = wallet;
    }
}
