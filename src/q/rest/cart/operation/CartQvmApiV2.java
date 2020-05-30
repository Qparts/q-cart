package q.rest.cart.operation;

import q.rest.cart.dao.DAO;
import q.rest.cart.filter.Secured;
import q.rest.cart.filter.SecuredVendor;
import q.rest.cart.model.entity.PurchaseOrder;
import q.rest.cart.model.entity.PurchaseOrderItem;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@Path("/internal/api/v2/qvm/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CartQvmApiV2 {


    @EJB
    private DAO dao;

    @EJB
    private AsyncService async;


    @SecuredVendor
    @Path("purchase-order")
    @POST
    public Response createPurchaseOrder(PurchaseOrder po){
        try{
            po.setCreated(new Date());
            dao.persist(po);
            for(var item : po.getItems()){
                item.setCreated(new Date());
                item.setPurchaseOrderId(po.getId());
                dao.persist(item);
            }
            //send email notification to target vendor;
            return Response.status(200).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }


    @SecuredVendor
    @Path("purchase-order")
    @PUT
    public Response updatePurchaseOrder(PurchaseOrder po){
        try{
            dao.update(po);
            //send email notification to vendor;
            return Response.status(201).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }

    @SecuredVendor
    @Path("purchase-order/target/{vendorId}")
    @GET
    public Response getPendingTargetPurchaseOrders(@PathParam(value = "vendorId") int targetId){
        try{
            String sql = "select b from PurchaseOrder b where b.targetVendorId = :value0  order by created desc";
            List<PurchaseOrder> pos = dao.getJPQLParams(PurchaseOrder.class, sql, targetId);
            for(PurchaseOrder po : pos){
                List<PurchaseOrderItem> pois = dao.getCondition(PurchaseOrderItem.class, "purchaseOrderId", po.getId());
                po.setItems(pois);
            }
            return Response.status(200).entity(pos).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }

    @SecuredVendor
    @Path("purchase-order/vendor/{vendorId}")
    @GET
    public Response getVendorPurchaseOrders(@PathParam(value = "vendorId") int vendorId){
        try{
            String sql = "select b from PurchaseOrder b where b.vendorId = :value0 order by created desc";
            List<PurchaseOrder> pos = dao.getJPQLParams(PurchaseOrder.class, sql, vendorId);
            for(PurchaseOrder po : pos){
                List<PurchaseOrderItem> pois = dao.getCondition(PurchaseOrderItem.class, "purchaseOrderId", po.getId());
                po.setItems(pois);
            }
            return Response.status(200).entity(pos).build();
        }catch (Exception ex){
            return Response.status(500).build();
        }
    }



}
