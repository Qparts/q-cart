package q.rest.cart.helper;

import q.rest.cart.model.entity.Cart;

import java.util.Base64;

public class AppConstants {

    //SERVICES
    private static final String USER_SERVICE =  SysProps.getValue("userService");
    private static final String CUSTOMER_SERVICE = SysProps.getValue("customerService");
    private static final String VENDOR_SERVICE = SysProps.getValue("vendorService");
    private static final String INVOICE_SERVICE = SysProps.getValue("invoiceService");

    private static final String PAYMENT_CALLBACK_URL_BASE = SysProps.getValue("paymentBaseCallbackUrl");
    private static final String PAYMENT_QETAA_CALLBACK_URL_BASE = SysProps.getValue("paymentBaseQetaaCallbackUrl");
    private static final String PAYMENT_QUOTATION_CALLBACK_URL_BASE = SysProps.getValue("paymentBaseQuotationCallbackUrl");
    private static final String PAYMENT_QUOTATION_QETAA_CALLBACK_URL_BASE = SysProps.getValue("paymentBaseQuotationQetaaCallbackUrl");
    public static final String MOYASAR_KEY = SysProps.getValue("moyasarKey");
    public final static String MOYASAR_API_URL = "https://api.moyasar.com/v1/payments";
    public static final String POST_CART_PAYMENT = INVOICE_SERVICE + "payment-order";

    //AWS
    private static final String AMAZON_S3_PATH = SysProps.getValue("amazonS3Path");
    private static final String PRODUCT_BUCKET_NAME =SysProps.getValue("productBucketName");
    private static final String BRAND_BUCKET_NAME = SysProps.getValue("brandBucketName");

    public static final String CUSTOMER_MATCH_TOKEN = CUSTOMER_SERVICE + "match-token";
    public static final String USER_MATCH_TOKEN = USER_SERVICE + "match-token";
    public static final String VENDOR_MATCH_TOKEN = VENDOR_SERVICE + "match-token";
    public static final String USER_MATCH_TOKEN_WS = USER_SERVICE + "match-token/ws";

    public static final String POST_WIRE_TRANSFER_EMAIL = CUSTOMER_SERVICE + "email/wire-transfer";

    public static final String POST_NOTIFY_SHIPMENT = CUSTOMER_SERVICE + "notify-shipment";



    public static final String getCartPaymentAmount(long cartId) {
        return INVOICE_SERVICE + "payment-order/paid-amounts/cart/" + cartId;
    }


    public static final String getValidateCustomer(long customerId){
        return CUSTOMER_SERVICE + "valid-customer/" + customerId;
    }

    public static final String getProductImage(long id){
        return AMAZON_S3_PATH + PRODUCT_BUCKET_NAME + "/" + id + ".png";
    }

    public static final String getBrandImage(long id){
        return AMAZON_S3_PATH + BRAND_BUCKET_NAME + "/" +  id + ".png";
    }

    public static final String getPaymentCallbackUrl(Cart cart){
        if(cart.getAppCode() == 2){
            return PAYMENT_CALLBACK_URL_BASE + "?cartId=" + cart.getId();
        }
        else{
            return PAYMENT_QETAA_CALLBACK_URL_BASE + "?cartId=" + cart.getId();
        }
    }

    public static final String getQuotationPaymentCallbackUrl(long quotationId, int appCode){
        if(appCode == 2){
            return PAYMENT_QUOTATION_CALLBACK_URL_BASE + "?quotationId=" + quotationId;
        }
        else{
            return PAYMENT_QUOTATION_QETAA_CALLBACK_URL_BASE + "?quotationId=" + quotationId;
        }

    }

    public static final String getCourier(int id){
        return VENDOR_SERVICE + "courier/" + id;
    }



}
