package q.rest.cart.filter;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class LoggingFilter implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        System.out.println("=====");
        System.out.println(requestContext.getUri());
        ObjectMapper om = new ObjectMapper();
        System.out.println(om.writeValueAsString(requestContext.getEntity()));
        System.out.println(requestContext.getStringHeaders());
        System.out.println("=====");
    }
}
