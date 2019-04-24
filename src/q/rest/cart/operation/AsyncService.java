package q.rest.cart.operation;

import q.rest.cart.dao.DAO;
import q.rest.cart.helper.AppConstants;
import q.rest.cart.model.entity.Bank;
import q.rest.cart.model.entity.Cart;
import q.rest.cart.model.entity.CartWireTransferRequest;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless
public class AsyncService implements Serializable {

    @EJB
    private DAO dao;



    @Asynchronous
    public void sendWireTransferEmail(String authHeader, Cart cart, CartWireTransferRequest wire){
        List<Bank> banks = dao.getCondition(Bank.class, "customerStatus", 'A');
        Map<String, Object> map = new HashMap<String,Object>();
        map.put("cartId", cart.getId());
        map.put("customerId", cart.getCustomerId());
        map.put("wireTransferId", wire.getId());
        map.put("amount", wire.getAmount());
        map.put("banks", banks);
        Response r = postSecuredRequest(AppConstants.POST_WIRE_TRANSFER_EMAIL, map, authHeader);
    }

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


}
