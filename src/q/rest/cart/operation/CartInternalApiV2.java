package q.rest.cart.operation;

import q.rest.cart.dao.DAO;
import q.rest.cart.filter.SecuredUser;
import q.rest.cart.helper.Helper;
import q.rest.cart.model.entity.*;
import q.rest.cart.model.privatecontract.FundWalletWireTransfer;
import q.rest.cart.model.privatecontract.RefundCartRequest;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@Path("/internal/api/v2/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CartInternalApiV2 {

    @EJB
    private DAO dao;

    @EJB
    private AsyncService async;

    @SecuredUser
    @GET
    @Path("wallet-live/{customerId}")
    public Response getNetWalletAmount(@PathParam(value = "customerId") long customerId) {
        try{
            String positiveSql = "select sum(b.amount) from CustomerWallet b where b.customerId = :value0 and b.walletType = :value1 ";
            String negativeSql = "select sum(c.amount) from CustomerWallet c where c.customerId = :value0 and c.walletType in (:value1 , :value2)";
            Number numberPositive = dao.findJPQLParams(Number.class, positiveSql, customerId, 'P');
            if(numberPositive == null){
                numberPositive = 0;
            }
            Number numberNegative = dao.findJPQLParams(Number.class, negativeSql, customerId, 'R', 'S');
            if(numberNegative == null){
                numberNegative = 0;
            }
            double net = numberPositive.doubleValue() - numberNegative.doubleValue();
            return Response.status(200).entity(net).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }

    @SecuredUser
    @POST
    @Path("empty-wallet")
    public Response createEmptyWallet(long customerId){
        try {
            CustomerWallet customerWallet = new CustomerWallet();
            customerWallet.setCustomerId(customerId);
            customerWallet.setAmount(0);
            customerWallet.setMethod('W');
            customerWallet.setCreditCharges(0);
            customerWallet.setCreatedBy(0);
            customerWallet.setWalletType('Z');
            dao.persist(customerWallet);
            return Response.status(200).entity(customerWallet.getId()).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }

    @SecuredUser
    @PUT
    @Path("cart-refund/wire-transfer")
    public Response refundCartByWireTransfer(RefundCartRequest refund){
        try{
            Cart cart = dao.find(Cart.class, refund.getCartId());
            CustomerWallet wallet = dao.findTwoConditions(CustomerWallet.class, "id", "walletType", refund.getWalletId(), 'Z');
            if(wallet == null){
                return Response.status(404).build();
            }
            double total = 0;
            if(refund.getRefundItemType() == 'P'){
                for(CartProduct newCp : refund.getCartProducts()){
                    CartProduct origCp = dao.find(CartProduct.class, newCp.getId());
                    if(origCp.getQuantity() == newCp.getQuantity()){
                        total += origCp.getSalesPrice() * origCp.getQuantity();
                        origCp.setStatus('R');//all refunded
                        dao.update(origCp);
                    }
                    else if(origCp.getQuantity() > newCp.getQuantity()){
                        total += origCp.getSalesPrice() * newCp.getQuantity();
                        origCp.setQuantity(origCp.getQuantity() - newCp.getQuantity());
                        dao.update(origCp);//deduct refunded products and update
                        newCp.setStatus('R');//create refunded products new entry
                        newCp.setId(0);
                        newCp.setCreatedBy(refund.getCreatedBy());
                        newCp.setCreated(new Date());
                        newCp.setCartId(origCp.getCartId());
                        dao.persist(newCp);
                    }
                }
            }
            double vat = total * 0.05;
            wallet.setCreated(new Date());
            wallet.setWalletType('R');
            wallet.setAmount(total + vat);
            wallet.setMethod('W');
            wallet.setCreatedBy(refund.getCreatedBy());
            wallet.setTransactionId("refund via wire");
            wallet.setCurrency("SAR");
            wallet.setCreditCharges(0);
            wallet.setBankId(refund.getBankId());
            dao.update(wallet);
            return Response.status(201).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @GET
    @Path("carts/awaiting")
    public Response getAwaitingCarts(){
        try {
            String jpql = "select b from Cart b where b.status = :value0 order by b.created";//N
            List<Cart> carts = dao.getJPQLParams(Cart.class, jpql, 'N');
            for(Cart cart: carts){
                initCart(cart);
            }
            return Response.status(200).entity(carts).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }

    @SecuredUser
    @GET
    @Path("cart/{cartId}/awaiting")
    public Response getAwaitingCart(@PathParam(value = "cartId") long cartId){
        try{
            String jpql = "select b from Cart b where b.id = :value0 and b.status = :value1";
            Cart cart = dao.findJPQLParams(Cart.class, jpql, cartId, 'N');
            initCart(cart);
            return Response.status(200).entity(cart).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @GET
    @Path("carts/initiated")
    public Response getInitiatedCarts(){
        try {
            String jpql = "select b from Cart b where status in ( :value0 , :value1) order by b.created";//N
            List<Cart> carts = dao.getJPQLParams(Cart.class, jpql, 'I', 'T');
            for(Cart cart: carts){
                initCart(cart);
            }
            return Response.status(200).entity(carts).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }

    @SecuredUser
    @GET
    @Path("wire-transfers/awaiting")
    public Response getAwaitingWireTransfers(){
        try {
            String jpql = "select b from CartWireTransferRequest b where status = :value0 order by b.created";//N
            List<CartWireTransferRequest> wires = dao.getJPQLParams(CartWireTransferRequest.class, jpql, 'N');
            for(CartWireTransferRequest wire : wires){
                Cart cart = dao.find(Cart.class, wire.getCartId());
                initCart(cart);
                wire.setCart(cart);
            }
            return Response.status(200).entity(wires).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }



    @SecuredUser
    @GET
    @Path("wire-transfer/{id}/awaiting")
    public Response getAwaitingWireTransfer(@PathParam(value = "id") long wireId){
        try {
            String jpql = "select b from CartWireTransferRequest b where b.id = :value0 and status = :value1";//N
            CartWireTransferRequest wire = dao.findJPQLParams(CartWireTransferRequest.class, jpql, wireId, 'N');
            Cart cart = dao.find(Cart.class, wire.getCartId());
            initCart(cart);
            wire.setCart(cart);
            return Response.status(200).entity(wire).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }

    @SecuredUser
    @POST
    @Path("comment")
    public Response createComment(CartComment comment){
        try {
            comment.setCreated(new Date());
            if(!isRedudant(comment)){
                dao.persist(comment);
            }
            return Response.status(200).entity(comment).build();
        }catch(Exception ex){
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @POST
    @Path("bank")
    public Response createPayment(Bank bank) {
        try {
            String jpql = "select b from Bank b where b.name = :value0 and b.nameAr = :value1 and b.iban = :value2 and b.account = :value3";
            List<Bank> banks = dao.getJPQLParams(Bank.class, jpql, bank.getName(), bank.getNameAr(), bank.getIban(), bank.getAccount());
            if(banks.isEmpty()){
                dao.persist(bank);
            }
            return Response.status(201).build();
        }catch(Exception ex) {
            return Response.status(500).build();
        }
    }

    @SecuredUser
    @PUT
    @Path("bank")
    public Response updateBank(Bank bank) {
        try {
            dao.update(bank);
            return Response.status(200).build();
        }catch(Exception ex) {
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @GET
    @Path("banks")
    public Response getAllBanks() {
        try {
            String jpql = "select b from Bank b where b.internalStatus != :value0";
            List<Bank> banks = dao.getJPQLParams(Bank.class, jpql, 'X');
            return Response.status(200).entity(banks).build();
        }catch(Exception ex) {
            return Response.status(500).build();
        }
    }

    @SecuredUser
    @DELETE
    @Path("bank")
    public Response deleteBank(Bank bank) {
        try {
            bank.setCustomerStatus('X');
            bank.setInternalStatus('X');
            dao.update(bank);
            return Response.status(201).build();
        }catch(Exception ex) {
            return Response.status(500).build();
        }
    }

    @SecuredUser
    @POST
    @Path("fund-wallet/wire-transfer")
    public Response fundWallet(FundWalletWireTransfer fwwt){
        try {
            fwwt.getWireTransfer().setProcessed(new Date());
            fwwt.getWireTransfer().setStatus('P');
            fwwt.getWallet().setCreated(new Date());
            String jpql = "select b from CustomerWallet b where b.customerId = :value0 and b.transactionId =:value1";
            List<CustomerWallet> check = dao.getJPQLParams(CustomerWallet.class, jpql, fwwt.getWallet().getCustomerId(), fwwt.getWallet().getTransactionId());
            if(check.isEmpty()){
                dao.update(fwwt.getWireTransfer());
                dao.persist(fwwt.getWallet());
                if(fwwt.getWireTransfer().getCart().getStatus() == 'T') {
                    fwwt.getWireTransfer().getCart().setStatus('N');
                    dao.update(fwwt.getWireTransfer().getCart());
                }
            }
            async.broadcastToNotification("wireRequests," + async.getWireRequestCount());
            return Response.status(201).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }

    @SecuredUser
    @PUT
    @Path("cancel-transfer")
    public Response cancelWireTransfer(CartWireTransferRequest wire){
        try{
            wire.setStatus('X');
            wire.setProcessed(new Date());
            dao.update(wire);
            CartComment comment = new CartComment();
            comment.setCreated(new Date());
            comment.setCartId(wire.getCartId());
            comment.setCreatedBy(wire.getCreatedBy());
            comment.setStatus('A');
            comment.setText("Transfer Cancelled");
            comment.setVisibleToCustomer(false);
            dao.persist(comment);
            async.broadcastToNotification("wireRequests," + async.getWireRequestCount());
            return Response.status(201).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @GET
    @Path("carts-notification")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAwaitingWalletsNotification() {
        try {
            String jpql = "select count(b) from Cart b where b.status in (:value0 , :value1 , :value2)";
            Long l = dao.findJPQLParams(Long.class, jpql, 'A', 'P' , 'S');
            if(l == null) {
                l = 0L;
            }
            return Response.status(200).entity(l).build();
        }catch(Exception ex) {
            return Response.status(500).build();
        }
    }



    private void initCart(Cart cart) throws Exception{
        List<CartProduct> cartProducts = dao.getCondition(CartProduct.class, "cartId", cart.getId());
        List<CartComment> cartComments = dao.getCondition(CartComment.class, "cartId", cart.getId());
        CartDelivery cartDelivery = dao.findCondition(CartDelivery.class, "cartId", cart.getId());
        CartDiscount cartDiscount = dao.findCondition(CartDiscount.class, "cartId", cart.getId());
        cart.setCartProducts(cartProducts);
        cart.setCartComments(cartComments);
        cart.setCartDiscount(cartDiscount);
        cart.setCartDelivery(cartDelivery);
    }

    private boolean isRedudant(CartComment comment) {
        String jpql = "select b from CartComment b where b.createdBy = :value0 and b.text = :value1 and b.cartId = :value2 and b.created between :value3 and :value4";
        Date previous = Helper.addSeconds(comment.getCreated(), -10);
        List<CartComment> comments = dao.getJPQLParams(CartComment.class, jpql, comment.getCreatedBy(), comment.getText(), comment.getCartId(),  previous, comment.getCreated());
        return comments.size() > 0;
    }




}
