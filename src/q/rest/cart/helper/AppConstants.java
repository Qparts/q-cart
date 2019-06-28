package q.rest.cart.helper;

import java.util.Base64;

public class AppConstants {

    //SERVICES
    private static final String USER_SERVICE =  SysProps.getValue("userService");
    private static final String CUSTOMER_SERVICE = SysProps.getValue("customerService");
    private static final String VENDOR_SERVICE = SysProps.getValue("vendorService");

    private static final String PAYMENT_CALLBACK_URL_BASE = SysProps.getValue("paymentBaseCallbackUrl");
//    public final static String MOYASAR_TEST_PUBLISHABLE_KEY = "pk_test_SugzkCgR72VgXxQHiYg7wVYSE7pCzYr5REYwVqQr";
 //   public final static String MOYASAR_LIVE_PUBLISHABLE_KEY = "pk_live_hGjxBboyUBKPghFg8E5q9UPYraJ1ueH7y8QyWPVG";
    public static final String MOYASAR_KEY = SysProps.getValue("moyasarKey");
    public final static String MOYASAR_API_URL = "https://api.moyasar.com/v1/payments";

    //AWS
    private static final String AMAZON_S3_PATH = SysProps.getValue("amazonS3Path");
    private static final String PRODUCT_BUCKET_NAME =SysProps.getValue("productBucketName");
    private static final String BRAND_BUCKET_NAME = SysProps.getValue("brandBucketName");

    public static final String CUSTOMER_MATCH_TOKEN = CUSTOMER_SERVICE + "match-token";
    public static final String USER_MATCH_TOKEN = USER_SERVICE + "match-token";
    public static final String USER_MATCH_TOKEN_WS = USER_SERVICE + "match-token/ws";

    public static final String POST_WIRE_TRANSFER_EMAIL = CUSTOMER_SERVICE + "email/wire-transfer";

    public static final String POST_NOTIFY_SHIPMENT = CUSTOMER_SERVICE + "notify-shipment";



    public static final String getValidateCustomer(long customerId){
        return CUSTOMER_SERVICE + "valid-customer/" + customerId;
    }

    public static final String getProductImage(long id){
        return AMAZON_S3_PATH + PRODUCT_BUCKET_NAME + "/" + id + ".png";
    }

    public static final String getBrandImage(long id){
        return AMAZON_S3_PATH + BRAND_BUCKET_NAME + "/" +  id + ".png";
    }

    public static final String getPaymentCallbackUrl(long cartId){
        return PAYMENT_CALLBACK_URL_BASE + "?cartId=" + cartId;
    }

    public static final String getCourier(int id){
        return VENDOR_SERVICE + "courier/" + id;
    }



}
