package q.rest.cart.operation;

import q.rest.cart.dao.DAO;
import q.rest.cart.filter.LoggingFilter;
import q.rest.cart.filter.SecuredCustomer;
import q.rest.cart.helper.AppConstants;
import q.rest.cart.helper.Helper;
import q.rest.cart.model.entity.*;
import q.rest.cart.model.moyasar.*;
import q.rest.cart.model.publiccontract.CartItemRequest;
import q.rest.cart.model.publiccontract.CartRequest;

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

@Path("/internal/api/v2/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CartApiV2 implements Serializable {

    @EJB
    private DAO dao;

    @SecuredCustomer
    @POST
    @Path("cart/wire-transfer")
    public Response createCartWireTransfer(@HeaderParam("Authorization") String header, CartRequest cartRequest) {
        try {
            //check if same customer is creating the call and that prices are valid
            if (!isValidCustomerOperation(header, cartRequest.getCustomerId())
                    || !isValidPrices(header, cartRequest)) {
                return Response.status(401).entity("invalid access").build();
            }

            //check if cart is not redundant
            if (isRedudant(cartRequest.getCustomerId(), new Date())) {
                return Response.status(429).entity("Too many requests").build();
            }
            Cart cart = createAndPrepareCartForPayment(header, cartRequest, 'W');
            double amount = calculateAmount(cart);
            createWireTransferRequest(cart.getId(), cartRequest.getCustomerId(), amount);
            updateCartStatus(cart, 'T');
            Map<String, Object> map = new HashMap<>();
            map.put("cartId", cart.getId());
            return Response.status(201).entity(map).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }


    @SecuredCustomer
    @POST
    @Path("cart/credit-card")
    public Response createCartCreditCart(@HeaderParam("Authorization") String header, CartRequest cartRequest) {
        try {
            if (!isValidCreditCardInfo(cartRequest)) {
                return Response.status(400).entity("Invalid credit card information").build();
            }
            //check if same customer is creating the call and that prices are valid
            if (!isValidCustomerOperation(header, cartRequest.getCustomerId())
                    || isValidPrices(header, cartRequest)) {
                return Response.status(401).entity("Invalid access").build();
            }
            //check if cart is not redundant
            if (isRedudant(cartRequest.getCustomerId(), new Date())) {
                return Response.status(429).entity("Too many requests").build();
            }
            //check payment method
            Cart cart = createAndPrepareCartForPayment(header, cartRequest, 'C');
            double amount = calculateAmount(cart);
            PaymentResponseCC ccr = createMoyassarCreditCardRequest(cartRequest, cart, amount);
            if(ccr == null || ccr.getStatus().equals("failed")){
                return Response.status(401).entity("Payment refused").build();
            }

            Map<String, Object> map = new HashMap<>();
            map.put("cartId", cart.getId());

            if(ccr.getStatus().equals("succeeded")){
                return Response.status(200).entity(map).build();
            }
            //initiated!
            map.put("transactionUrl", ccr.getSource().getTransactionURL());
            return Response.status(202).entity(map).build();

        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }

    private Cart createAndPrepareCartForPayment(String header, CartRequest cartRequest, char paymentMethod){
        Cart cart = createCart(header, cartRequest.getCustomerId(), paymentMethod);
        List<CartProduct> cartProducts = createCartProducts(cart.getId(), cartRequest.getCartItems());
        cart.setCartProducts(cartProducts);
        CartDelivery cartDelivery = createCartDelivery(cart.getId(), cartRequest);
        cart.setCartDelivery(cartDelivery);
        CartDiscount cartDiscount = createCartDiscount(cart.getId(), cartRequest.getCustomerId(), cartRequest.getDiscountId());
        cart.setCartDiscount(cartDiscount);
        return cart;
    }


    private double calculateAmount(Cart cart) {
        double productsPrice = 0;
        double discountAmount = 0;
        double deliveryCharges = cart.getCartDelivery().getDeliveryCharges();

        for (CartProduct cp : cart.getCartProducts()) {
            productsPrice += cp.getSalesPrice() * cp.getQuantity();
        }

        if (cart.getCartDiscount() != null) {
            Discount discount = dao.find(Discount.class, cart.getCartDiscount().getDiscountId());
            if (discount.getDiscountType() == 'P') {
                discountAmount = discount.getPercentage() * productsPrice;
            } else if (discount.getDiscountType() == 'D') {
                discountAmount = cart.getCartDelivery().getDeliveryCharges();
            }
        }
        return productsPrice + discountAmount + deliveryCharges;
    }


    private void updateCartStatus(Cart cart, char status) {
        cart.setStatus(status);//wire transfer status
        dao.update(cart);
    }

    private Cart createCart(String header, long customerId, char paymentMethod) {
        WebApp webApp = getWebAppFromAuthHeader(header);
        Cart cart = new Cart();
        cart.setAppCode(webApp.getAppCode());
        cart.setCreated(new Date());
        cart.setCreatedBy(0);
        cart.setCustomerId(customerId);
        cart.setStatus('I');//Initial cart, nothing to process yet until its N
        cart.setVatPercentage(0.05);
        cart.setPaymentMethod(paymentMethod);
        dao.persist(cart);
        return cart;
    }

    private void createWireTransferRequest(long cartId, long customerId, double amount) {
        CartWireTransferRequest wireTransfer = new CartWireTransferRequest();
        wireTransfer.setAmount(amount);
        wireTransfer.setCartId(cartId);
        wireTransfer.setCreated(new Date());
        wireTransfer.setCreatedBy(0);
        wireTransfer.setCustomerId(customerId);
        wireTransfer.setProcessed(null);
        wireTransfer.setProcessedBy(null);//
        wireTransfer.setStatus('N');//new, nothing to process
        dao.persist(wireTransfer);
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
        paymentRequest.setCallbackUrl(AppConstants.getPaymentCallbackUrl(cart.getId()));
        paymentRequest.setCurrency("SAR");
        paymentRequest.setDescription("QParts Cart # " + cart.getId());
        Response r = this.postSecuredRequestAndLog(AppConstants.MOYASAR_API_URL, paymentRequest, Helper.getMoyaserSecurityHeader());
        if (r.getStatus() == 200) {
            PaymentResponseCC ccr = r.readEntity(PaymentResponseCC.class);
            CartGatewayFirstResponse cg = new CartGatewayFirstResponse(ccr, cart, 0);
            dao.persist(cg);
            if(ccr.getStatus().equals("succeeded")){
                //fund wallet
                this.fundWalletByCreditCard(amount, ccr, 0, cart.getCustomerId());
                //update cart status
                this.updateCartStatus(cart, 'N');
            }
            return ccr;
        }
        return null;
    }

    private List<CartProduct> createCartProducts(long cartId, List<CartItemRequest> items) {
        List<CartProduct> cartProducts = new ArrayList<>();
        for (CartItemRequest cir : items) {
            CartProduct cp = new CartProduct();
            cp.setCartId(cartId);
            cp.setCreated(new Date());
            cp.setCreatedBy(0);
            cp.setQuantity(cir.getQuantity());
            cp.setProductId(cir.getProductId());
            cp.setStatus('N');//new, nothing to process yet
            dao.persist(cp);
            cartProducts.add(cp);
        }
        return cartProducts;
    }

    private CartDelivery createCartDelivery(long cartId, CartRequest cartRequest) {
        CartDelivery cartDelivery = new CartDelivery();
        cartDelivery.setAddressId(cartRequest.getAddressId());
        cartDelivery.setCartId(cartId);
        cartDelivery.setCreated(new Date());
        cartDelivery.setCreatedBy(0);
        cartDelivery.setDeliveryCharges(cartRequest.getDeliveryCharges());
        cartDelivery.setPreferredCuorier(cartRequest.getPreferredCuorier());
        cartDelivery.setStatus('N');//new, nothing to process yet
        dao.persist(cartDelivery);
        return cartDelivery;
    }

    private CartDiscount createCartDiscount(long cardId, long customerId, Long discountId) {
        CartDiscount cartDiscount = null;
        boolean validDiscount = true;
        try {
            Discount discount = dao.find(Discount.class, discountId);
            if (discount.isCustomerSpecific()) {
                if (customerId != discount.getCustomerId()) {
                    validDiscount = false;
                }
            }
            if (!discount.isReusable()) {
                if (discount.getStatus() != 'N') {
                    validDiscount = false;
                }
            }
            if (discount.getExpire().before(new Date())) {
                validDiscount = false;
            }
        } catch (Exception ex) {
            validDiscount = false;
        }

        //move ahead with discount
        if (validDiscount) {
            Discount discount = dao.find(Discount.class, discountId);
            cartDiscount = new CartDiscount();
            cartDiscount.setCartId(cardId);
            cartDiscount.setCreated(new Date());
            cartDiscount.setCreatedBy(0);
            cartDiscount.setDiscountId(discount.getId());
            dao.persist(cartDiscount);
        }

        return cartDiscount;
    }

    @SecuredCustomer
    @POST
    @Path("confirm-payment/credit-card")
    public Response confirmCreditCardPayment(@HeaderParam("Authorization") String header) {
        try {

  //          isValidCustomerOperation(header, customerId);
            //check if same customer is creating the call

            //check if amount matches the requested amount in moyasser request object

            //fund wallet with the amount
//            fundWalletByCreditCard(amount, ccr, createdBy)
            //respond with 201 accepted
            return Response.status(500).build();
        } catch (Exception ex) {
            return Response.status(500).build();
        }
    }


    private void fundWalletByCreditCard(double amount, PaymentResponseCC ccr, int createdBy, long customerId){
        CustomerWallet wallet = new CustomerWallet();
        wallet.setAmount(amount);
        wallet.setBankId(null);
        wallet.setCcCompany(ccr.getSource().getCompany());
        wallet.setCreated(new Date());
        wallet.setCreatedBy(0);
        wallet.setCreditCharges(ccr.getFee());
        wallet.setCurrency("SAR");
        wallet.setCustomerId(customerId);
        wallet.setGateway("Moyasar");
        wallet.setMethod('C');//credit card
        wallet.setTransactionId(ccr.getId());
        wallet.setWalletType('P');
    }

    private boolean isValidCustomerOperation(String header, long customerId) {
        Response r = this.getSecuredRequest(AppConstants.getValidateCusomer(customerId), header);
        return r.getStatus() == 100;
    }

    private boolean isValidCreditCardInfo(CartRequest cartRequest) {
        return true;
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


}
