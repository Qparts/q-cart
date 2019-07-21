package q.rest.cart.operation;

import q.rest.cart.dao.DAO;
import q.rest.cart.filter.LoggingFilter;
import q.rest.cart.filter.Secured;
import q.rest.cart.filter.SecuredCustomer;
import q.rest.cart.filter.ValidApp;
import q.rest.cart.helper.AppConstants;
import q.rest.cart.helper.Helper;
import q.rest.cart.model.entity.*;
import q.rest.cart.model.moyasar.*;
import q.rest.cart.model.publiccontract.*;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.*;

@Path("/api/v2/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CartApiV2 implements Serializable {

    @EJB
    private DAO dao;

    @EJB
    private AsyncService async;


    @Secured
    @POST
    @Path("cart/wire-transfer")
    public Response createCartWireTransfer(@HeaderParam("Authorization") String header, CartRequest cartRequest) {
        try {
            //check if same customer is creating the call and that prices are valid
            if(cartRequest.getCreatedBy() == null && cartRequest.getAppCode() == null){
                if (!isValidCustomerOperation(header, cartRequest.getCustomerId())
                        || !isValidPrices(header, cartRequest)) {
                    return Response.status(401).entity("invalid access").build();
                }
            }

            if(!isWalletAmountValid(cartRequest.getCustomerId(), cartRequest.getWalletAmount())){
                return Response.status(400).build();
            }
            //check if cart is not redundant
            if (isRedudant(cartRequest.getCustomerId(), new Date())) {
                return Response.status(429).entity("Too many requests").build();
            }
            Cart cart = createCart(header, cartRequest, 'W');
            createCartProducts(cart, cartRequest.getCartItems());
            createCartDelivery(cart, cartRequest);
            createUsedWallets(cart, cartRequest);
            double amount = Helper.round(cart.getGrandTotalWithUsedWallet(), 2);
            CartWireTransferRequest wireTransfer = createWireTransferRequest(cart.getId(), 0, cartRequest.getCustomerId(), amount, 'F', "cart");
            updateCartStatus(cart, 'T');
            updateUsedWallet(cart, 'T', true);//lock and set to transfer status
            Map<String, Object> map = new HashMap<>();
            map.put("cartId", cart.getId());
            async.sendWireTransferEmail(header, cart, wireTransfer);
            async.broadcastToNotification("wireRequests," + async.getWireRequestCount());
            return Response.status(200).entity(map).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }

    //in qetaa and dashboard only now
    @Secured
    @POST
    @Path("quotation/wire-transfer")
    public Response createQuotationWireTransfer(@HeaderParam("Authorization") String header, QuotationPaymentRequest qpr){
        try{
            System.out.println("received");
            WebApp webApp = getWebAppFromAuthHeader(header);
            //check if same customer is creating the call and that prices are valid
            if(webApp.getAppCode() == 3) {
                if (!isValidCustomerOperation(header, qpr.getCustomerId())) {
                    return Response.status(401).entity("invalid access").build();
                }
            }
            CartWireTransferRequest wireTransfer = createWireTransferRequest(0, qpr.getQuotationId(), qpr.getCustomerId(), qpr.getAmount(), 'F', "quotation");
            async.sendWireTransferEmail(header, qpr.getQuotationId(), qpr.getCustomerId(), wireTransfer);
            async.broadcastToNotification("wireRequests," + async.getWireRequestCount());
            return Response.status(201).build();
        }catch (Exception ex){
            ex.printStackTrace();
            return Response.status(500).build();
        }
    }


    //in qetaa only now
    @SecuredCustomer
    @POST
    @Path("quotation/credit-card")
    public Response createQuotationCreditCard(@HeaderParam("Authorization") String header, QuotationPaymentRequest qpr){
        try{
            if (!isValidCreditCardInfo(qpr.getCardHolder())) {
                return Response.status(400).entity("Invalid credit card information").build();
            }
            //check if same customer is creating the call and that prices are valid
            if (!isValidCustomerOperation(header, qpr.getCustomerId())) {
                return Response.status(401).entity("Invalid access").build();
            }
            //check payment method
            PaymentResponseCC ccr = createMoyassarCreditCardRequest(qpr);
            if(ccr == null || ccr.getStatus().equals("failed")){
                return Response.status(401).entity("Payment refused").build();
            }
            Map<String, Object> map = new HashMap<>();
            if(ccr.getStatus().equals("succeeded")){
                return Response.status(200).entity(map).build();
            }
            //initiated!
            map.put("transactionUrl", ccr.getSource().getTransactionURL());
            map.put("quotationId", qpr.getQuotationId());
            return Response.status(202).entity(map).build();

        }catch (Exception ex){
            ex.printStackTrace();
            return Response.status(500).build();
        }
    }


    @SecuredCustomer
    @POST
    @Path("cart/credit-card")
    public Response createCartCreditCard(@HeaderParam("Authorization") String header, CartRequest cartRequest) {
        try {
            if (!isValidCreditCardInfo(cartRequest)) {
                return Response.status(400).entity("Invalid credit card information").build();
            }
            if(!isWalletAmountValid(cartRequest.getCustomerId(), cartRequest.getWalletAmount())){
                return Response.status(400).build();
            }
            //check if same customer is creating the call and that prices are valid
            if (!isValidCustomerOperation(header, cartRequest.getCustomerId())
                    || !isValidPrices(header, cartRequest)) {
                return Response.status(401).entity("Invalid access").build();
            }


            //check if cart is not redundant
            if (isRedudant(cartRequest.getCustomerId(), new Date())) {
                return Response.status(429).entity("Too many requests").build();
            }
            //check payment method

            Cart cart = createCart(header, cartRequest, 'C');
            createCartProducts(cart, cartRequest.getCartItems());
            createCartDelivery(cart, cartRequest);
            createUsedWallets(cart, cartRequest);
            double amount = Helper.round(cart.getGrandTotalWithUsedWallet(), 2);
            PaymentResponseCC ccr = createMoyassarCreditCardRequest(cartRequest, cart, amount);
            if(ccr == null || ccr.getStatus().equals("failed")){
                this.updateCartStatus(cart, 'F');
                return Response.status(401).entity("Payment refused").build();
            }

            updateLocked(cart);
            Map<String, Object> map = new HashMap<>();
            map.put("cartId", cart.getId());

            if(ccr.getStatus().equals("succeeded")){
                async.broadcastToNotification("processCarts," + async.getProcessCartCount());
                return Response.status(200).entity(map).build();
            }
            //initiated!
            map.put("transactionUrl", ccr.getSource().getTransactionURL());
            return Response.status(202).entity(map).build();

        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }

    private void updateLocked(Cart cart){
        for(CartUsedWallet cartUsedWallet : cart.getCartUsedWallets()){
            cartUsedWallet.getCustomerWallet().setLocked(true);
            dao.update(cartUsedWallet.getCustomerWallet());
        }
    }


    @SecuredCustomer
    @PUT
    @Path("payment/3dsecure-response")
    public Response confirmCreditCardPayment(@HeaderParam("Authorization") String header, ThreeDConfirmRequest _3d) {
        try {
            //isValidCustomerOperation(header, customerId);
            //check if same customer is creating the call
            String jpql = "select b from CartGatewayFirstResponse b where b.gPaymentId = :value0 and b.customerId = :value1 and b.status = :value2";
            CartGatewayFirstResponse gateway = dao.findJPQLParams(CartGatewayFirstResponse.class, jpql, _3d.getId(), _3d.getCustomerId(), 'I');
            if(_3d.getType() == null || _3d.getType() == 'C'){
                Cart cart = dao.find(Cart.class, _3d.getCartId());
                //check if amount matches the requested amount in moyasser request object
                if(_3d.getStatus().equals("paid")){
                    double temp = gateway.getgFee();
                    double fee = temp /100D;
                    temp = gateway.getgAmount();
                    double amount = temp /100D;
                    //fund wallet with the amount
                    fundWalletByCreditCard(amount, fee, gateway.getgCompany(), gateway.getgPaymentId(), 0 , gateway.getCustomerId(), true);
                    gateway.setStatus('P');
                    dao.update(gateway);
                    this.updateCartStatus(cart, 'N');
                }
                else{
                    gateway.setStatus('F');
                    dao.update(gateway);
                    this.updateCartStatus(cart, 'F');
                }
                //respond with 201 accepted
                async.broadcastToNotification("processCarts," + async.getProcessCartCount());
            }
            else if (_3d.getType() == 'Q'){
                if(_3d.getStatus().equals("paid")){
                    double temp = gateway.getgFee();
                    double fee = temp /100D;
                    temp = gateway.getgAmount();
                    double amount = temp /100D;
                    //fund wallet with the amount
                    fundWalletByCreditCard(amount, fee, gateway.getgCompany(), gateway.getgPaymentId(), 0 , gateway.getCustomerId(), false);
                    gateway.setStatus('P');
                    dao.update(gateway);
                }
                else {
                    gateway.setStatus('F');
                    dao.update(gateway);
                }
            }
            return Response.status(201).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }

    @ValidApp
    @GET
    @Path("banks")
    public Response getActiveBanksCustomer() {
        try {
            List<PublicBank> banks = dao.getCondition(PublicBank.class, "customerStatus", 'A');
            return Response.status(200).entity(banks).build();
        }catch(Exception ex) {
            return Response.status(500).build();
        }
    }

    @SecuredCustomer
    @GET
    @Path("wallet-amount/{customerId}")
    public Response getAvailableWalletAmount(@HeaderParam("Authorization") String header, @PathParam(value = "customerId") long customerId){
        try{
            if(!isValidCustomerOperation(header, customerId)){
                return Response.status(401).entity("invalid access").build();
            }
            List<CustomerWallet> wallets = dao.getTwoConditions(CustomerWallet.class, "customerId", "locked", customerId, false);
            double amount = 0;
            for(var w : wallets){
                amount += w.getAmount();
            }
            Map<String,Object> map = new HashMap<>();
            map.put("amount", amount);
            return Response.status(200).entity(map).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }


    @SecuredCustomer
    @GET
    @Path("discount/promocode/{param}")
    public Response searchDiscountPromoCode(@HeaderParam("Authorization") String header, @PathParam(value = "param") String code){
        try{
            WebApp webApp = getWebAppFromAuthHeader(header);
            String jpql = "select b from Discount b where b.code = :value0 " +
                    "and b.expire > :value1 " +
                    "and b.appCode = :value2 " +
                    "and b.status = :value3";
            Discount discount = dao.findJPQLParams(Discount.class, jpql , code, new Date(), webApp.getAppCode(), 'A');
            if(discount == null){
                return Response.status(404).build();
            }
            if(discount.isCustomerSpecific()){
                long customerId = getCustomerIdFromAuthHeader(header);
                if(!discount.getCustomerId().equals(customerId)){
                    return Response.status(404).build();
                }
            }
            return Response.status(200).entity(discount).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }



    private void createUsedWallets(Cart cart, CartRequest cartRequest){
        cart.setCartUsedWallets(new ArrayList<>());
        if(cartRequest.getWalletAmount() > 0){
            double temp = cartRequest.getWalletAmount();
            int index = 0;
            List<CustomerWallet> wallets = dao.getTwoConditions(CustomerWallet.class, "customerId" , "locked", cartRequest.getCustomerId(), false);
            while(temp > 0){
                CustomerWallet wallet = wallets.get(index);
                CartUsedWallet usedWallet = new CartUsedWallet();
                usedWallet.setCartId(cart.getId());
                usedWallet.setCreatedBy(cart.getCreatedBy());
                usedWallet.setCustomerWallet(wallet);
                usedWallet.setStatus('I');//initiated
                dao.persist(usedWallet);
                cart.getCartUsedWallets().add(usedWallet);
                temp -= wallet.getAmount();
                index++;
            }
        }
    }


    private void updateCartStatus(Cart cart, char status) {
        cart.setStatus(status);//wire transfer status
        dao.update(cart);
    }

    private void updateUsedWallet(Cart cart, char status, boolean locked){
        for(CartUsedWallet cuw : cart.getCartUsedWallets()){
            cuw.setStatus(status);
            dao.update(cuw);
            cuw.getCustomerWallet().setLocked(locked);
            dao.update(cuw.getCustomerWallet());
        }
    }

    private Cart createCart(String header, CartRequest cartRequest, char paymentMethod) {
        Cart cart = new Cart();
        if(cartRequest.getAppCode() == null) {
            cart.setAppCode(getWebAppFromAuthHeader(header).getAppCode());
            cart.setCreatedBy(0);
        }
        else{
            cart.setAppCode(cartRequest.getAppCode());
            cart.setCreatedBy(cartRequest.getAppCode());
        }
        cart.setCreated(new Date());
        cart.setCustomerId(cartRequest.getCustomerId());
        cart.setStatus('I');//Initial cart, nothing to process yet until its N
        cart.setVatPercentage(0.05);
        cart.setPaymentMethod(paymentMethod);
        if(cartRequest.getDiscountId() != null){
            Discount discount = dao.find(Discount.class, cartRequest.getDiscountId());
            cart.setDiscount(discount);
        }
        dao.persist(cart);
        return cart;
    }

    private CartWireTransferRequest createWireTransferRequest(long cartId, long quotationId, long customerId, double amount, char wireType, String paymentPurpose) {
        CartWireTransferRequest wireTransfer = new CartWireTransferRequest();
        wireTransfer.setAmount(Helper.round(amount, 2));
        wireTransfer.setCartId(cartId);
        wireTransfer.setQuotationId(quotationId);
        wireTransfer.setCreated(new Date());
        wireTransfer.setCreatedBy(0);
        wireTransfer.setCustomerId(customerId);
        wireTransfer.setProcessed(null);
        wireTransfer.setProcessedBy(null);//
        wireTransfer.setStatus('N');//new, nothing to process
        wireTransfer.setWireType(wireType);
        wireTransfer.setPaymentPurpose(paymentPurpose);
        dao.persist(wireTransfer);
        return wireTransfer;
    }





    private PaymentResponseCC createMoyassarCreditCardRequest(QuotationPaymentRequest qpr) {
        RequestSourceCC cc = new RequestSourceCC();
        cc.setCvc(qpr.getCardHolder().getCcCvc());
        cc.setMonth(qpr.getCardHolder().getCcMonth());
        cc.setYear(qpr.getCardHolder().getCcYear());
        cc.setType(qpr.getSourceString());
        cc.setName(qpr.getCardHolder().getCcName());
        cc.setNumber(qpr.getCardHolder().getCcNumber());

        PaymentRequest paymentRequest = new PaymentRequestCC();
        ((PaymentRequestCC) paymentRequest).setSource(cc);
        paymentRequest.setAmount(Helper.paymentIntegerFormat(qpr.getAmount()));
        paymentRequest.setCallbackUrl(AppConstants.getQuotationPaymentCallbackUrl(qpr.getQuotationId(), 3));
        paymentRequest.setCurrency("SAR");
        paymentRequest.setDescription("Quotation # " + qpr.getQuotationId());
        Response r = this.postSecuredRequestAndLog(AppConstants.MOYASAR_API_URL, paymentRequest, Helper.getMoyaserSecurityHeader());
        if (r.getStatus() == 200 || r.getStatus() == 201) {
            PaymentResponseCC ccr = r.readEntity(PaymentResponseCC.class);
            CartGatewayFirstResponse cg = new CartGatewayFirstResponse(ccr, qpr.getQuotationId(), qpr.getCustomerId(), 0);
            dao.persist(cg);
            if(ccr.getStatus().equals("succeeded")){
                this.fundWalletByCreditCard(qpr.getAmount(), ccr.getFee(), ccr.getSource().getCompany(), ccr.getId(), 0, qpr.getCustomerId(), false);
            }
            return ccr;
        }
        else{

        }
        return null;
    }


    private PaymentResponseCC createMoyassarCreditCardRequest(CartRequest cartRequest, Cart cart, double amount) {
        RequestSourceCC cc = new RequestSourceCC();
        cc.setCvc(cartRequest.getCcCvc());
        cc.setMonth(cartRequest.getCcMonth());
        cc.setYear(cartRequest.getCcYear());
        cc.setType("creditcard");
        cc.setName(cartRequest.getCcName());
        cc.setNumber(cartRequest.getCcNumber());

        PaymentRequest paymentRequest = new PaymentRequestCC();
        ((PaymentRequestCC) paymentRequest).setSource(cc);
        paymentRequest.setAmount(Helper.paymentIntegerFormat(amount));
        paymentRequest.setCallbackUrl(AppConstants.getPaymentCallbackUrl(cart));
        paymentRequest.setCurrency("SAR");
        paymentRequest.setDescription("Cart # " + cart.getId());
        Response r = this.postSecuredRequestAndLog(AppConstants.MOYASAR_API_URL, paymentRequest, Helper.getMoyaserSecurityHeader());
        if (r.getStatus() == 200 || r.getStatus() == 201) {
            PaymentResponseCC ccr = r.readEntity(PaymentResponseCC.class);
            CartGatewayFirstResponse cg = new CartGatewayFirstResponse(ccr, cart, 0);
            dao.persist(cg);
            if(ccr.getStatus().equals("succeeded")){
                //fund wallet
                this.fundWalletByCreditCard(amount, ccr.getFee(), ccr.getSource().getCompany(), ccr.getId(), 0, cart.getCustomerId(), true);
                //update cart status
                this.updateCartStatus(cart, 'N');
            }
            return ccr;
        }
        else{

        }
        return null;
    }

    private void createCartProducts(Cart cart, List<CartItemRequest> items) {
        List<CartProduct> cartProducts = new ArrayList<>();
        for (CartItemRequest cir : items) {
            CartProduct cp = new CartProduct();
            cp.setCartId(cart.getId());
            cp.setSalesPrice(Helper.round(cir.getSalesPrice(),2));
            cp.setCreated(new Date());
            cp.setCreatedBy(0);
            cp.setQuantity(cir.getQuantity());
            cp.setProductId(cir.getProductId());
            if(cir.getDiscountId() != null){
                Discount discount = dao.find(Discount.class, cir.getDiscountId());
                cp.setDiscount(discount);
            }
            cp.setStatus('N');//new, nothing to process yet
            dao.persist(cp);
            cartProducts.add(cp);
        }
        cart.setCartProducts(cartProducts);
    }

    private void createCartDelivery(Cart cart, CartRequest cartRequest) {
        CartDelivery cartDelivery = new CartDelivery();
        cartDelivery.setAddressId(cartRequest.getAddressId());
        cartDelivery.setCartId(cart.getId());
        cartDelivery.setCreated(new Date());
        cartDelivery.setCreatedBy(0);
        cartDelivery.setDeliveryCharges(cartRequest.getDeliveryCharges());
        cartDelivery.setPreferredCuorier(cartRequest.getPreferredCuorier());
        cartDelivery.setStatus('N');//new, nothing to process yet
        dao.persist(cartDelivery);
        cart.setCartDelivery(cartDelivery);
    }



    private void fundWalletByCreditCard(double amount, double fee, String ccCompany, String transactionId, int createdBy, long customerId, boolean locked){
        CustomerWallet wallet = new CustomerWallet();
        amount = Helper.round(amount, 2);
        wallet.setAmount(amount);
        wallet.setBankId(null);
        wallet.setCcCompany(ccCompany);
        wallet.setCreated(new Date());
        wallet.setCreatedBy(0);
        wallet.setCreditCharges(fee);
        wallet.setCurrency("SAR");
        wallet.setCustomerId(customerId);
        wallet.setGateway("Moyasar");
        wallet.setMethod('C');//credit card
        wallet.setTransactionId(transactionId);
        wallet.setWalletType('P');
        wallet.setLocked(locked);
        dao.persist(wallet);
    }


    private boolean isValidCustomerOperation(String header, long customerId) {
        Response r = this.getSecuredRequest(AppConstants.getValidateCustomer(customerId), header);
        return r.getStatus() == 204;
    }



    private boolean isValidCreditCardInfo(CartRequest cartRequest) {
        boolean valid = true;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, cartRequest.getCcYear());
        calendar.set(Calendar.MONTH, cartRequest.getCcMonth());
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Calendar current = Calendar.getInstance();
        if(calendar.before(current)){
            valid = false;
        }

        if(!cartRequest.getCcCvc().matches("([0-9]{3})")){
            valid = false;
        }

        if(!cartRequest.getCcName().trim().contains(" ")){
            valid = false;
        }

        if(!cartRequest.getCcNumber().matches("^(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14})$")){
            valid = false;
        }
        return valid;
    }



    private boolean isValidCreditCardInfo(CardHolder cardHolder) {
        boolean valid = true;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, cardHolder.getCcYear());
        calendar.set(Calendar.MONTH, cardHolder.getCcMonth());
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        Calendar current = Calendar.getInstance();
        if(calendar.before(current)){
            valid = false;
        }

        if(!cardHolder.getCcCvc().matches("([0-9]{3})")){
            valid = false;
        }

        if(!cardHolder.getCcName().trim().contains(" ")){
            valid = false;
        }

        if(!cardHolder.getCcNumber().matches("^(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14})$")){
            valid = false;
        }
        return valid;
    }

    private boolean isWalletAmountValid(long customerId, double walletAmount){
        List<CustomerWallet> customerWallets = dao.getTwoConditions(CustomerWallet.class, "customerId", "locked", customerId, false);
        double availableAmount = 0;
        for(CustomerWallet cw : customerWallets){
            availableAmount += cw.getAmount();
        }
        return (walletAmount == availableAmount);
    }

    private boolean isValidPrices(String header, CartRequest cartRequest) {
        return true;
    }

    // check idempotency of a cart
    private boolean isRedudant(long customerId, Date created) {
        // if a cart was created less than 1 minute ago, then do not do
        String jpql = "select b from Cart b where b.customerId = :value0 and b.created between :value1 and :value2";
        // String sql = " select * from crt_cart "
        Date previous = Helper.addSeconds(created, -20);
        List<Cart> carts = dao.getJPQLParams(Cart.class, jpql, customerId, previous, created);
        return carts.size() > 0;
    }


    public <T> Response postSecuredRequest(String link, T t, String authHeader) {
        Invocation.Builder b = ClientBuilder.newClient().target(link).request();
        b.header(HttpHeaders.AUTHORIZATION, authHeader);
        Response r = b.post(Entity.entity(t, "application/json"));
        return r;
    }


    public <T> Response postSecuredRequestAndLog(String link, T t, String authHeader) {
        Invocation.Builder b = ClientBuilder.newClient().target(link).register(LoggingFilter.class).request();
        b.header(HttpHeaders.AUTHORIZATION, authHeader);
        Response r = b.post(Entity.entity(t, "application/json"));
        return r;
    }


    public <T> Response getSecuredRequest(String link, String authHeader) {
        Invocation.Builder b = ClientBuilder.newClient().target(link).request();
        b.header(HttpHeaders.AUTHORIZATION, authHeader);
        Response r = b.get();
        return r;
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


    private long getCustomerIdFromAuthHeader(String authHeader) {
        try {
            String[] values = authHeader.split("&&");
            String customerId = values[1].trim();
            return Long.parseLong(customerId);
        } catch (Exception ex) {
            return 0;
        }
    }


}
