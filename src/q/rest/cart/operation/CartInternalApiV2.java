package q.rest.cart.operation;

import q.rest.cart.dao.DAO;
import q.rest.cart.filter.SecuredUser;
import q.rest.cart.helper.Helper;
import q.rest.cart.model.entity.*;
import q.rest.cart.model.privatecontract.FundWalletWireTransfer;

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
            String jpql = "select b from Cart b where status = :value0 order by b.created";//N
            List<Cart> carts = dao.getJPQLParams(Cart.class, jpql, 'I');
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
