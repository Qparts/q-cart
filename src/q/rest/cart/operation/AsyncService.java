package q.rest.cart.operation;

import q.rest.cart.dao.DAO;
import q.rest.cart.helper.AppConstants;
import q.rest.cart.model.entity.*;
import q.rest.cart.model.privatecontract.Courier;
import q.rest.cart.operation.sockets.NotificationsEndPoint;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.*;

@Stateless
public class AsyncService implements Serializable {

    @EJB
    private DAO dao;



    @Asynchronous
    public void sendWireTransferEmail(String authHeader, Cart cart, CartWireTransferRequest wire){
        List<Bank> banks = dao.getCondition(Bank.class, "customerStatus", 'A');
        Map<String, Object> map = new HashMap<String,Object>();
        map.put("cartId", cart.getId());
        map.put("quotationId", wire.getQuotationId());
        map.put("customerId", cart.getCustomerId());
        map.put("wireTransferId", wire.getId());
        map.put("purpose", "cart");
        map.put("amount", wire.getAmount());
        map.put("banks", banks);
        Response r = postSecuredRequest(AppConstants.POST_WIRE_TRANSFER_EMAIL, map, authHeader);
    }



    @Asynchronous
    public void sendWireTransferEmail(String authHeader, long quotationId, long customerId, CartWireTransferRequest wire){
        List<Bank> banks = dao.getCondition(Bank.class, "customerStatus", 'A');
        Map<String, Object> map = new HashMap<String,Object>();
        System.out.println(quotationId);
        map.put("quotationId", quotationId);
        map.put("cartId", wire.getCartId());
        map.put("customerId", customerId);
        map.put("wireTransferId", wire.getId());
        map.put("purpose", "quotation");
        map.put("amount", wire.getAmount());
        map.put("banks", banks);
        Response r = postSecuredRequest(AppConstants.POST_WIRE_TRANSFER_EMAIL, map, authHeader);
    }



    @Asynchronous
    public void notifyShipment(String authHeader, Shipment shipment){
        Courier courier = getCourier(shipment.getCourierId(), authHeader);
        Map<String, Object> map = new HashMap<String,Object>();
        map.put("courierName", courier.getName());
        map.put("courierNameAr", courier.getNameAr());
        map.put("customerId", shipment.getCustomerId());
        map.put("trackReference", shipment.getTrackReference());
        map.put("trackable", shipment.isTrackable());
        map.put("trackLink", courier.getTrackLink());
        map.put("cartNumber", getCartNumbers(shipment.getShipmentItems()));
        map.put("shipmentId", shipment.getId());
        map.put("addressId", shipment.getAddressId());
        Response r = postSecuredRequest(AppConstants.POST_NOTIFY_SHIPMENT, map, authHeader);
        System.out.println("notify shipment response "+r.getStatus());
    }



    /*
    @Asynchronous
    public void sendShipmentSms(Shipment shipment, String authHeader) {
        Courier c = getCourier(shipment.getCourierId(), authHeader);
        String text = "تم شحن القطع الى عنوانك للطلب رقم ";
        text += getCartNumbers(shipment.getShipmentItems());
        if(shipment.isTrackable()) {
            text += " رقم التتبع ";
            text += shipment.getTrackReference().replace(" ", "");
            text += " على الناقل ";
            text += c.getName();
            text += " , رابط التتبع: ";
            text += c.getTrackLink() + " ";
        }
        text += " شكرا لكم, نتمنى أن نكون عند حسن ظنكم";
        this.sendSms(shipment.getCustomerId(), shipment.getId(), text, "shipment", authHeader);
    }
    */

    public int getWireRequestCount(){
        String jpql = "select count(b) from CartWireTransferRequest b where status = :value0";//N
        Number number = dao.findJPQLParams(Number.class, jpql, 'N');
        if(number == null){
            number = 0;
        }
        return number.intValue();
    }

    public int getProcessCartCount(){
        String jpql = "select count(b) from Cart b where b.status = :value0";//N
        Number number = dao.findJPQLParams(Number.class, jpql, 'N');
        if(number == null){
            number = 0;
        }
        return number.intValue();
    }





    private Courier getCourier(int id, String authHeader) {
        Response r = getSecuredRequest(AppConstants.getCourier(id), authHeader);
        Courier c = new Courier();
        if(r.getStatus() == 200) {
            c = r.readEntity(Courier.class);
        }
        return c;
    }


    private String getCartNumbers(List<ShipmentItem> items) {
        String t = "";
        Set<Long> set = new HashSet<Long>();
        for(ShipmentItem shi : items) {
            CartProduct cp = dao.find(CartProduct.class, shi.getCartProductId());
            set.add(cp.getCartId());
        }
        for(Long lon : set) {
            t = lon + "/";
        }
        if(set.size() == 1) {
            t.replace("/", "");
        }
        return t;
    }

    @Asynchronous
    public void broadcastToNotification(String message){
        NotificationsEndPoint.broadcast(message);

    }


    public <T> Response postSecuredRequest(String link, T t, String authHeader) {
        Invocation.Builder b = ClientBuilder.newClient().target(link).request();
        b.header(HttpHeaders.AUTHORIZATION, authHeader);
        Response r = b.post(Entity.entity(t, "application/json"));// not secured
        return r;

    }


    public <T> Response getSecuredRequest(String link, String authHeader) {
        Invocation.Builder b = ClientBuilder.newClient().target(link).request();
        b.header(HttpHeaders.AUTHORIZATION, authHeader);
        Response r = b.get();// not secured
        return r;

    }


}
