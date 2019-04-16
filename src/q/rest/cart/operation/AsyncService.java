package q.rest.cart.operation;

import q.rest.cart.dao.DAO;
import q.rest.cart.operation.sockets.NotificationsEndPoint;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.io.Serializable;

@Stateless
public class AsyncService implements Serializable {

    @EJB
    private DAO dao;



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

}
