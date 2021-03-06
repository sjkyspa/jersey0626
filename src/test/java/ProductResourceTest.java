import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.thoughtworks.com.domain.Product;
import org.thoughtworks.com.exception.ProductNotFoundException;
import org.thoughtworks.com.provider.ProductRepository;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductResourceTest extends JerseyTest {


    @Mock
    ProductRepository productRepository;

    @Captor
    ArgumentCaptor<Product> productCaptor;
    @Test
    public void should_return_200_when_get_product() {

        Response response = target("/products/1").request().accept(MediaType.APPLICATION_JSON_TYPE).get();
        assertEquals(response.getStatus(), 200);
    }


    @Test
    public void should_get_all_products() {

        when(productRepository.getAllProducts()).thenReturn(asList(new Product(1, "productName1"), new Product(2, "productName2")));
        Response response = target("/products").request().accept(MediaType.APPLICATION_JSON_TYPE).get();
        assertEquals(response.getStatus(), 200);

        List products = response.readEntity(List.class);
        assertEquals(products.size(), 2);
        Map<String, Object> product = (Map<String, Object>) products.get(0);
        assertThat(product.get("uri").toString(), endsWith("/products/1"));
        assertThat(product.get("name").toString(), is("productName1"));
    }


    @Test
    public void should_return_404_when_can_not_find_product() {
        when(productRepository.getProductById(eq(2))).thenThrow(ProductNotFoundException.class);
        Response response = target("/products/2").request().accept(MediaType.APPLICATION_JSON_TYPE).get();
        assertEquals(response.getStatus(), 404);
    }

    @Test
    public void should_create_product() {
        Form createProductRequest = new Form();
        createProductRequest.param("name", "productName");
        when(productRepository.createProduct(productCaptor.capture())).thenReturn(2);
        Response response = target("/products").request().post(Entity.form(createProductRequest));
        assertEquals(response.getStatus(), 201);
        verify(productRepository).createProduct(productCaptor.capture());
        assertThat(productCaptor.getValue().getName(), is("productName"));
        assertThat(response.getLocation().toString(), endsWith("/products/2"));
    }

    @Override
    protected Application configure() {
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.packages("org.thoughtworks.com");
        resourceConfig.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(productRepository).to(ProductRepository.class);
            }
        });
        return resourceConfig;
    }
}
