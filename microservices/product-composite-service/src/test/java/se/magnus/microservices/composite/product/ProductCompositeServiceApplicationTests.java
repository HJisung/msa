package se.magnus.microservices.composite.product;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.composite.product.ProductAggregate;
import se.magnus.api.composite.product.RecommendationSummary;
import se.magnus.api.composite.product.ReviewSummary;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.review.Review;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.composite.product.services.ProductCompositeIntegration;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
class ProductCompositeServiceApplicationTests {

	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID = 3;

	@Autowired private WebTestClient client;

	@MockitoBean private ProductCompositeIntegration compositeIntegration;

	@BeforeEach
	void setUp() {

		when(compositeIntegration.getProduct(PRODUCT_ID_OK))
				.thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-address"));
		when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
				.thenReturn(singletonList(new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mock address")));
		when(compositeIntegration.getReviews(PRODUCT_ID_OK))
				.thenReturn(singletonList(new Review(PRODUCT_ID_OK, 1, "author", "subject", "content", "mock address")));

		when(compositeIntegration.getProduct(PRODUCT_ID_NOT_FOUND))
				.thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

		when(compositeIntegration.getProduct(PRODUCT_ID_INVALID))
				.thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));
	}

	@Test
	void contextLoads() {}

	@Test
	void getProductById() {

		client.get()
				.uri("/product-composite/" + PRODUCT_ID_OK)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
				.jsonPath("$.recommendations.length()").isEqualTo(1)
				.jsonPath("$.reviews.length()").isEqualTo(1);
	}

	@Test
	void getProductNotFound() {

		client.get()
				.uri("/product-composite/" + PRODUCT_ID_NOT_FOUND)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isNotFound()
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
				.jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
	}

	@Test
	void getProductWhenIntegrationReturnsNull() {
		int productId = 4;
		when(compositeIntegration.getProduct(productId)).thenReturn(null);
		client.get().uri("/product-composite/" + productId)
				.accept(APPLICATION_JSON).exchange()
				.expectStatus().isNotFound()
				.expectBody().jsonPath("$.message")
				.isEqualTo("No product found for productId: " + productId);
	}

	@Test
	void getProductInvalidInput() {

		client.get()
				.uri("/product-composite/" + PRODUCT_ID_INVALID)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(UNPROCESSABLE_CONTENT)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
				.jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
	}

    @Test
    void createCompositeProduct1() {
        ProductAggregate body = new ProductAggregate(1, "name", 1, null, null, null);
        postAndVerifyProduct(body);
        verify(compositeIntegration).createProduct(any(Product.class));
    }

    @Test
    void createCompositeProduct2() {
        ProductAggregate body = new ProductAggregate(1, "name", 1,
            singletonList(new RecommendationSummary(1, "a", 1, "c")),
            singletonList(new ReviewSummary(1, "a", "s", "c")), null);
        postAndVerifyProduct(body);
        verify(compositeIntegration).createProduct(any(Product.class));
        verify(compositeIntegration).createRecommendation(any(Recommendation.class));
        verify(compositeIntegration).createReview(any(Review.class));
    }

    @Test
    void deleteCompositeProduct() {
        ProductAggregate body = new ProductAggregate(1, "name", 1,
            singletonList(new RecommendationSummary(1, "a", 1, "c")),
            singletonList(new ReviewSummary(1, "a", "s", "c")), null);
        postAndVerifyProduct(body);
        deleteAndVerifyProduct(1);
        deleteAndVerifyProduct(1);
        verify(compositeIntegration, times(2)).deleteProduct(1);
        verify(compositeIntegration, times(2)).deleteRecommendations(1);
        verify(compositeIntegration, times(2)).deleteReviews(1);
    }

    private void postAndVerifyProduct(ProductAggregate body) {
        client.post().uri("/product-composite")
            .contentType(APPLICATION_JSON).bodyValue(body)
            .exchange().expectStatus().isOk().expectBody().isEmpty();
    }

    private void deleteAndVerifyProduct(int productId) {
        client.delete().uri("/product-composite/" + productId)
            .exchange().expectStatus().isOk().expectBody().isEmpty();
    }
}
