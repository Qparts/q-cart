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
import java.util.*;

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
    @Path("wallets-report/year/{year}/month/{month}/wallet-type/{walletType}/method/{method}")
    public Response getQuotationsReport(@PathParam(value = "year") int year,
                                        @PathParam(value = "month") int month,
                                        @PathParam(value="walletType") String walletType,
                                        @PathParam(value = "method") String method){
        try{
            Date from = Helper.getFromDate(month, year);
            Date to = Helper.getToDate(month, year);
            String jpql = "select b from CustomerWallet b where b.created between :value0 and :value1 and b.walletType ";
            jpql += (walletType.equals("A") ? "!= :value2" : "= :value2");
            jpql += " and b.method ";
            jpql += (method.equals("A") ? "!= :value3" : "= :value3");
            jpql += " and b.walletType != :value4 order by b.created asc";//sales wallet is removed from report
            List<CustomerWallet> customerWallets = dao.getJPQLParams(CustomerWallet.class, jpql, from, to, walletType.charAt(0), method.charAt(0), 'S');
            return Response.status(200).entity(customerWallets).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }


    private void prepareCartProductCompares(List<CartProduct> cps) {
        for (var cp : cps) {
            cp.setCartProductCompares(new ArrayList<>());
            var list = dao.getCondition(CartProductCompare.class, "cartProductId", cp.getId());
            cp.setCartProductCompares(list);
        }
    }



    @SecuredUser
    @POST
    @Path("cart/wire-transfer")
    public Response createCartWireTransfer(@HeaderParam("Authorization") String header, Cart cart) {
        try {
            //check if cart is not redundant
            if (isCartRedudant(cart.getCustomerId(), new Date())) {
                return Response.status(429).entity("Too many requests").build();
            }
            createCart(header, cart, 'W');
            CartWireTransferRequest wireTransfer = createWireTransferRequest(cart);
            updateCartStatus(cart, 'T');
            async.sendWireTransferEmail(header, cart, wireTransfer);
            async.broadcastToNotification("wireRequests," + async.getWireRequestCount());
            return Response.status(200).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }

    private void updateCartStatus(Cart cart, char status) {
        cart.setStatus(status);//wire transfer status
        dao.update(cart);
    }

    private CartWireTransferRequest createWireTransferRequest(Cart cart) {
        CartWireTransferRequest wireTransfer = new CartWireTransferRequest();
        wireTransfer.setAmount(cart.getGrandTotal());
        wireTransfer.setCartId(cart.getId());
        wireTransfer.setCreated(new Date());
        wireTransfer.setCreatedBy(cart.getCreatedBy());
        wireTransfer.setCustomerId(cart.getCustomerId());
        wireTransfer.setProcessed(null);
        wireTransfer.setProcessedBy(null);//
        wireTransfer.setStatus('N');//new, nothing to process
        dao.persist(wireTransfer);
        return wireTransfer;
    }


    private void createCart(String header, Cart cart, char paymentMethod){
        WebApp webApp = getWebAppFromAuthHeader(header);
        if(cart.getAppCode() == 0) {
            cart.setAppCode(webApp.getAppCode());
        }
        cart.setCreated(new Date());
        cart.setStatus('I');//Initial cart, nothing to process yet until its N
        cart.setVatPercentage(0.05);
        cart.setPaymentMethod(paymentMethod);
        dao.persist(cart);
        for (CartProduct cp: cart.getCartProducts()) {
            cp.setCartId(cart.getId());
            cp.setSalesPrice(Helper.round(cp.getSalesPrice(), 2));
            cp.setCreated(new Date());
            cp.setStatus('N');//new, nothing to process yet
            dao.persist(cp);
        }
        cart.getCartDelivery().setCartId(cart.getId());
        cart.getCartDelivery().setCreated(new Date());
        cart.getCartDelivery().setStatus('N');
        dao.persist(cart.getCartDelivery());
    }


    @SecuredUser
    @PUT
    @Path("cart-product/sold")
    public Response updateCartProduct(long cartProductId){
        try{
            var cp = dao.find(CartProduct.class, cartProductId);
            cp.setStatus('S');
            dao.update(cp);
            checkAwaitingCartStatus(cp.getCartId());
            return Response.status(201).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }

    }



    @SecuredUser
    @GET
    @Path("wallets/{customerId}")
    public Response getCustomerWallets(@PathParam(value = "customerId") long customerId) {
        try{
            List<CustomerWallet> wallets = dao.getCondition(CustomerWallet.class, "customerId", customerId);
            return Response.status(200).entity(wallets).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }



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
            Number numberNegative = dao.findJPQLParams(Number.class, negativeSql, customerId, 'R', 'S');//refund or sold
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
            if(refund.isRefundProducts()){
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
            if(refund.isRefundDelivery()){
                total += refund.getDeliveryFees();
                CartDelivery cartDelivery = dao.findCondition(CartDelivery.class, "cartId", cart.getId());
                cartDelivery.setStatus('R');
                dao.update(cartDelivery);
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
            checkAwaitingCartStatus(cart.getId());
            return Response.status(201).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }

    private void checkAwaitingCartStatus(long cartId){
        String sql = "select b from CartProduct b where b.cartId = :value0 and b.status = :value1";//check if there is any new cart product
        List<CartProduct> cartProducts = dao.getJPQLParams(CartProduct.class, sql, cartId, 'N');
        if(cartProducts.isEmpty()){
            //check if there are any sold items
            sql = "select b from CartProduct b where b.cartId = :value0 and b.status =:value1";
            List<CartProduct> soldProducts = dao.getJPQLParams(CartProduct.class, sql, cartId, 'S');
            if(soldProducts.isEmpty()){
                //check if there is delivery fee standing
                sql = "select b from CartDelivery b where b.cartId = :value0 and b.status =:value1";
                CartDelivery cartDelivery = dao.findJPQLParams(CartDelivery.class, sql, cartId, 'N');//there is waiting cart
                if(cartDelivery == null){
                    Cart cart = dao.find(Cart.class, cartId);
                    cart.setStatus('R');//completely refunded
                    dao.update(cart);
                }
            }else{
                Cart cart = dao.find(Cart.class, cartId);
                cart.setStatus('S');//set cart as all sold
                dao.update(cart);
            }
        }
    }


    @SecuredUser
    @GET
    @Path("carts/customer/{customerId}")
    public Response getCustomerCarts(@PathParam(value = "customerId") long customerId){
        try {
            String jpql = "select b from Cart b where b.customerId =:value0 order by b.created";//N
            List<Cart> carts = dao.getJPQLParams(Cart.class, jpql, customerId);
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
    @Path("carts/awaiting")
    public Response getAwaitingCarts(){
        try {
            String jpql = "select b from Cart b where b.status in (:value0 , :value1) order by b.created";//N
            List<Cart> carts = dao.getJPQLParams(Cart.class, jpql, 'N', 'S');
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
    @Path("cart/{cartId}")
    public Response getCart(@PathParam(value = "cartId") long cartId){
        try{
            Cart cart = dao.find(Cart.class, cartId);
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
    @GET
    @Path("cart-products/sold/customer/{customerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCartSoldProducts(@PathParam(value = "customerId") long customerId) {
        try {
            String hql = "select b from CartProduct b where b.status = :value0 and b.cartId in ("
                    + "select c from Cart c where c.customerId = :value1)";
            List<CartProduct> cartProducts = dao.getJPQLParams(CartProduct.class, hql, 'S', customerId);
            for(CartProduct cp : cartProducts){
                Cart cart = dao.find(Cart.class, cp.getCartId());
                initCart(cart);
                cp.setCart(cart);
            }
            return Response.status(200).entity(cartProducts).build();
        } catch (Exception ex) {
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
    @Path("wallet/sales")
    public Response createSalesWallet(CustomerWallet customerWallet){
        try{
            customerWallet.setCreated(new Date());
            customerWallet.setWalletType('S');
            double amount = Helper.round(customerWallet.getAmount(), 2);
            customerWallet.setAmount(amount);
            System.out.println("3 transaction id = " + customerWallet.getTransactionId());
            dao.persist(customerWallet);
            return Response.status(201).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @POST
    @Path("wallet/sales-return")
    public Response createSalesReturnWallet(CustomerWallet customerWallet){
        try{
            double amount = Helper.round(customerWallet.getAmount(), 2);
            customerWallet.setAmount(amount);
            customerWallet.setCreated(new Date());
            customerWallet.setWalletType('T');
            dao.persist(customerWallet);
            return Response.status(201).build();
        } catch (Exception ex){
            return Response.status(500).build();
        }
    }


    @SecuredUser
    @PUT
    @Path("cart-delivery/sales")
    public Response updateCartDelivery(Map<String,Object> map){
        try{
            if(map != null){
                long id = ((Number) map.get("id")).longValue();
                CartDelivery cd = dao.find(CartDelivery.class, id);
                if(cd.getStatus() == 'N'){
                    cd.setStatus('S');
                    dao.update(cd);
                }
            }
            return Response.status(201).build();
        }catch (Exception ex){
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
                double amount = Helper.round(fwwt.getWallet().getAmount(),2);
                fwwt.getWallet().setAmount(amount);
                dao.persist(fwwt.getWallet());
                if(fwwt.getWireTransfer().getCart().getStatus() == 'T') {
                    fwwt.getWireTransfer().getCart().setStatus('N');
                    dao.update(fwwt.getWireTransfer().getCart());
                }
            }
            async.broadcastToNotification("wireRequests," + async.getWireRequestCount());
            async.broadcastToNotification("processCarts," + async.getProcessCartCount());
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



    @SecuredUser
    @POST
    @Path("cart-product-compare")
    public Response createWalletItemVendors(Set<CartProductCompare> cpcs) {
        try {
            for (var cpc : cpcs) {
                if (cpc.getCost() != null) {
                    var cpcCheck = dao.findTwoConditions(CartProductCompare.class, "vendorId",
                            "cartProductId", cpc.getVendorId(), cpc.getCartProductId());
                    if (cpcCheck== null) {
                        cpc.setDate(new Date());
                        dao.persist(cpc);
                    } else {
                        if (!cpcCheck.getCost().equals(cpc.getCost())) {
                            cpcCheck.setCost(cpc.getCost());
                            cpcCheck.setDate(new Date());
                            dao.update(cpcCheck);
                        }
                    }
                }
            }
            return Response.status(201).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(500).build();
        }
    }




    @POST
    @Path("new-shipment")
    @SecuredUser
    public Response createEmptyWallet(Map<String,Object> map) {
        try {
            Long customerId = ((Number) map.get("customerId")).longValue();
            Long addressId = ((Number) map.get("addressId")).longValue();
            Shipment s = new Shipment();
            s.setCustomerId(customerId);
            s.setCreatedBy(0);
            s.setCourierId(0);
            s.setAddressId(addressId);
            s.setTrackable(false);
            s.setShipmentFees(0);
            s.setStatus('W');//Waiting for update
            dao.persist(s);
            return Response.status(200).entity(s.getId()).build();
        }catch(Exception ex) {
            ex.printStackTrace();
            return Response.status(500).build();
        }
    }

    @SecuredUser
    @PUT
    @Path("shipment")
    public Response createShipment(@HeaderParam("Authorization") String authHeader, Shipment shipment) {
        try {
            shipment.setCreated(new Date());
            shipment.setStatus('S');//Shipped
            dao.update(shipment);
            createShipmentItems(shipment);
            updateCart(shipment);
            System.out.println("updated cart");
            async.notifyShipment(authHeader, shipment);
            return Response.status(201).build();
        }catch(Exception ex) {
            return Response.status(500).build();
        }
    }

    private void createShipmentItems(Shipment shipment) {
        for(ShipmentItem item :  shipment.getShipmentItems()) {
            item.setShipped(new Date());
            item.setShipmentId(shipment.getId());
            item.setStatus('S');//shipped
            dao.persist(item);
        }
    }

    @SecuredUser
    @GET
    @Path("/shipments/year/{param}/month/{param2}/courier/{param3}/cart/{param4}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWalletsReport(@PathParam(value = "param") int year,
                                     @PathParam(value = "param2") int month,
                                     @PathParam(value = "param3") int courierId,
                                     @PathParam(value = "param4") long cartId) {
        try {
            Date from = Helper.getFromDate(month, year);
            Date to = Helper.getToDate(month, year);


            List<Shipment> shipments= new ArrayList<>();
            String jpql = "select b from Shipment b where b.created between :value0 and :value1";

            if (courierId == 0 && cartId == 0) {
                jpql = jpql + " order by b.created asc";
                shipments = dao.getJPQLParams(Shipment.class, jpql, from, to);
            } else if(courierId > 0 && cartId == 0){
                jpql = jpql + " and b.courierId = :value2";
                jpql = jpql + " order by b.created asc";
                shipments = dao.getJPQLParams(Shipment.class, jpql, from, to, courierId);
            }else if(courierId == 0 && cartId > 0) {
                jpql = jpql + " and b.id in (select c.shipmentId from ShipmentItem c where c.cartProduct.cartId = :value2)";
                jpql = jpql + " order by b.created asc";
                shipments = dao.getJPQLParams(Shipment.class, jpql, from, to, cartId);
            }
            else if (courierId > 0 && cartId > 0) {
                jpql = jpql + " and b.id in (select c.shipmentId from ShipmentItem c where c.cartProduct.cartId = :value2)";
                jpql = jpql + " and b.courierId = :value3 ";
                jpql = jpql + " order by b.created asc";
                shipments = dao.getJPQLParams(Shipment.class, jpql, from, to, cartId, courierId);
            }

            for(Shipment sh : shipments) {
                List<ShipmentItem> items = dao.getCondition(ShipmentItem.class, "shipmentId", sh.getId());
                sh.setShipmentItems(items);
            }

            return Response.status(200).entity(shipments).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(500).build();
        }
    }




    private void updateCart(Shipment shipment) {
        Set<Long> set = new HashSet<Long>();
        for(ShipmentItem item :  shipment.getShipmentItems()) {
            CartProduct cp = dao.find(CartProduct.class, item.getCartProductId());
            set.add(cp.getCartId());
            cp.setStatus('H');
            dao.update(cp);
        }

        for(Long cartId : set) {
            List<CartProduct> wis = dao.getTwoConditions(CartProduct.class, "cartId", "status", cartId, 'S');
            if(wis.isEmpty()) {
                Cart cart = dao.find(Cart.class, cartId);
                cart.setStatus('H');
                dao.update(cart);
            }
        }
    }



    private void initCart(Cart cart) throws Exception{
        var cartProducts = dao.getCondition(CartProduct.class, "cartId", cart.getId());
        prepareCartProductCompares(cartProducts);
        var cartComments = dao.getCondition(CartComment.class, "cartId", cart.getId());
        var cartDelivery = dao.findCondition(CartDelivery.class, "cartId", cart.getId());
        cart.setCartProducts(cartProducts);
        cart.setCartComments(cartComments);
        cart.setCartDelivery(cartDelivery);
    }

    private boolean isRedudant(CartComment comment) {
        String jpql = "select b from CartComment b where b.createdBy = :value0 and b.text = :value1 and b.cartId = :value2 and b.created between :value3 and :value4";
        Date previous = Helper.addSeconds(comment.getCreated(), -10);
        List<CartComment> comments = dao.getJPQLParams(CartComment.class, jpql, comment.getCreatedBy(), comment.getText(), comment.getCartId(),  previous, comment.getCreated());
        return comments.size() > 0;
    }


    // check idempotency of a cart
    private boolean isCartRedudant(long customerId, Date created) {
        String jpql = "select b from Cart b where b.customerId = :value0 and b.created between :value1 and :value2";
        Date previous = Helper.addSeconds(created, -20);
        List<Cart> carts = dao.getJPQLParams(Cart.class, jpql, customerId, previous, created);
        return (carts.size() > 0);
    }

    // qualified
    private WebApp getWebAppFromAuthHeader(String authHeader) {
        try {
            String[] values = authHeader.split("&&");
            String secret = values[2].trim();
            WebApp webApp = dao.findTwoConditions(WebApp.class, "appSecret", "active", secret, true);
            if (webApp == null) {
                throw new NotAuthorizedException("Unauthorized Access");
            }
            // Validate app secret
            return webApp;
        } catch (Exception ex) {
            return null;
        }
    }



}
