package com.hosting.core.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusIntegrationTest
public class DeploymentResourceIT {

  @InjectMock
  ClaimsContext claimsMock;

  private static final String USER_ID = "test-user-123";

  @BeforeEach
  public void setup() {
    when(claimsMock.getUserId()).thenReturn(USER_ID);
    when(claimsMock.getRequestId()).thenReturn("test-request-456");

    cleanDeployments();
  }

  @AfterEach
  public void teardown() {
    cleanDeployments();
  }

  private void cleanDeployments() {
    try {
      List<Map<String, Object>> deployments = given()
          .when()
          .get("/api/v1/deployments")
          .then()
          .statusCode(200)
          .extract()
          .body()
          .jsonPath()
          .getList("");

      if (deployments != null) {
        for (Map<String, Object> dep : deployments) {
          String deploymentId = (String) dep.get("deploymentId");
          if (deploymentId != null) {
            given()
                .when()
                .delete("/api/v1/deployments/{deploymentId}", deploymentId)
                .then()
                .statusCode(200);
          }
        }
      }
    } catch (Exception e) {
      // Ignore cleanup failures
    }
  }

  @Test
  public void testDeploymentFlow() {
    // 1. Initialize Deployment
    Map<String, String> createRequest = Map.of(
        "name", "test-deployment",
        "runtime", "NODEJS_20_X"
    );

    String deploymentId = given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post("/api/v1/deployments/initialize")
        .then()
        .statusCode(200)
        .body("deploymentId", notNullValue())
        .extract()
        .path("deploymentId");

    // 2. Generate Upload URL
    given()
        .queryParam("deploymentId", deploymentId)
        .when()
        .get("/api/v1/deployments/upload-url")
        .then()
        .statusCode(200)
        .body("uploadUrl", notNullValue())
        .body("expiresInSeconds", is(3600));

    // 3. Set Status
    given()
        .queryParam("status", "UPLOADING")
        .when()
        .patch("/api/v1/deployments/{deploymentId}/status", deploymentId)
        .then()
        .statusCode(200)
        .body(is("Deployment status updated"));

    // 4. Trigger Deployment
    given()
        .when()
        .post("/api/v1/deployments/{deploymentId}/trigger", deploymentId)
        .then()
        .statusCode(200)
        .body(is("Deployment job enqueued"));
  }

  @Test
  public void testGetDeployments() {
    Map<String, String> createRequest = Map.of(
        "name", "test-deployment-get",
        "runtime", "NODEJS_20_X"
    );

    String deploymentId = given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post("/api/v1/deployments/initialize")
        .then()
        .statusCode(200)
        .extract()
        .path("deploymentId");

    given()
        .when()
        .get("/api/v1/deployments")
        .then()
        .statusCode(200)
        .body("[0].deploymentId", is(deploymentId))
        .body("[0].name", is("test-deployment-get"))
        .body("[0].status", is("INITIALIZED"));
  }

  @Test
  public void testGetDeploymentsEmpty() {
    given()
        .when()
        .get("/api/v1/deployments")
        .then()
        .statusCode(200)
        .body("size()", is(0));
  }

  @Test
  public void testGetDeployment() {
    Map<String, String> createRequest = Map.of(
        "name", "test-deployment-single",
        "runtime", "NODEJS_20_X"
    );

    String deploymentId = given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post("/api/v1/deployments/initialize")
        .then()
        .statusCode(200)
        .extract()
        .path("deploymentId");

    given()
        .when()
        .get("/api/v1/deployments/{deploymentId}", deploymentId)
        .then()
        .statusCode(200)
        .body("deploymentId", is(deploymentId))
        .body("name", is("test-deployment-single"));
  }

  @Test
  public void testGetDeploymentNotFound() {
    given()
        .when()
        .get("/api/v1/deployments/{deploymentId}", "non-existent-id")
        .then()
        .statusCode(404)
        .body(is("Deployment not found"));
  }

  @Test
  public void testDeleteDeployment() {
    Map<String, String> createRequest = Map.of(
        "name", "test-deployment-delete",
        "runtime", "NODEJS_20_X"
    );

    String deploymentId = given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post("/api/v1/deployments/initialize")
        .then()
        .statusCode(200)
        .extract()
        .path("deploymentId");

    given()
        .when()
        .delete("/api/v1/deployments/{deploymentId}", deploymentId)
        .then()
        .statusCode(200)
        .body(is("Deployment deleted"));
  }
}
