package q.rest.cart.operation;

import q.rest.cart.dao.DAO;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/internal/api/v2/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CartInternalApiV2 {

    @EJB
    private DAO dao;




}
