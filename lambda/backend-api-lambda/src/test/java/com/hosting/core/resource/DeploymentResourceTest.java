package com.hosting.core.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hosting.common.aws.DeploymentService;
import com.hosting.common.aws.dynamo.models.Deployment;
import com.hosting.common.dto.CreateDeploymentRequest;
import com.hosting.common.dto.UploadUrlResponse;
import com.hosting.common.enums.DeploymentEnums.Runtime;
import com.hosting.common.enums.DeploymentEnums.Status;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class DeploymentResourceTest {

  @InjectMock
  ClaimsContext claimsMock;

  @InjectMock
  DeploymentService deploymentServiceMock;

  private static final String USER_ID = "test-user-123";
  private static final String DEPLOYMENT_ID = "mock-deployment-uuid";

  @BeforeEach
  public void setup() {
    when(claimsMock.getUserId()).thenReturn(USER_ID);
    when(claimsMock.getRequestId()).thenReturn("test-request-456");
  }

  @Test
  public void testDeploymentFlow() {
    // 1. Initialize Deployment
    CreateDeploymentRequest createRequest = new CreateDeploymentRequest("test-deployment", Runtime.NODEJS_20_X, null);
    when(deploymentServiceMock.initializeDeployment(eq(USER_ID), any(CreateDeploymentRequest.class)))
        .thenReturn(DEPLOYMENT_ID);

    given()
        .contentType(ContentType.JSON)
        .body(createRequest)
        .when()
        .post("/api/v1/deployments/initialize")
        .then()
        .statusCode(200)
        .body("deploymentId", is(DEPLOYMENT_ID));

    verify(deploymentServiceMock).initializeDeployment(eq(USER_ID), any(CreateDeploymentRequest.class));

    // 2. Generate Upload URL
    UploadUrlResponse uploadUrlResponse = new UploadUrlResponse("https://mock-s3-presigned-url.com", 3600);
    when(deploymentServiceMock.generateUploadUrl(eq(USER_ID), eq(DEPLOYMENT_ID)))
        .thenReturn(uploadUrlResponse);

    given()
        .queryParam("deploymentId", DEPLOYMENT_ID)
        .when()
        .get("/api/v1/deployments/upload-url")
        .then()
        .statusCode(200)
        .body("uploadUrl", is("https://mock-s3-presigned-url.com"))
        .body("expiresInSeconds", is(3600));

    verify(deploymentServiceMock).generateUploadUrl(eq(USER_ID), eq(DEPLOYMENT_ID));

    // 3. Set Status
    doNothing().when(deploymentServiceMock).setDeploymentStatus(eq(USER_ID), eq(DEPLOYMENT_ID), eq(Status.UPLOADING));

    given()
        .queryParam("status", Status.UPLOADING.name())
        .when()
        .patch("/api/v1/deployments/{deploymentId}/status", DEPLOYMENT_ID)
        .then()
        .statusCode(200)
        .body(is("Deployment status updated"));

    verify(deploymentServiceMock).setDeploymentStatus(eq(USER_ID), eq(DEPLOYMENT_ID), eq(Status.UPLOADING));

    // 4. Trigger Deployment
    doNothing().when(deploymentServiceMock).triggerDeployment(eq(USER_ID), eq(DEPLOYMENT_ID));

    given()
        .when()
        .post("/api/v1/deployments/{deploymentId}/trigger", DEPLOYMENT_ID)
        .then()
        .statusCode(200)
        .body(is("Deployment job enqueued"));

    verify(deploymentServiceMock).triggerDeployment(eq(USER_ID), eq(DEPLOYMENT_ID));
  }

  @Test
  public void testGetDeployments() {
    Deployment deployment = new Deployment();
    deployment.setUserId(USER_ID);
    deployment.setDeploymentId(DEPLOYMENT_ID);
    deployment.setName("test-deployment");
    deployment.setStatus(Status.INITIALIZED);
    deployment.setRuntime(Runtime.NODEJS_20_X);

    when(deploymentServiceMock.getDeployments(eq(USER_ID)))
        .thenReturn(Optional.of(List.of(deployment)));

    given()
        .when()
        .get("/api/v1/deployments")
        .then()
        .statusCode(200)
        .body("[0].deploymentId", is(DEPLOYMENT_ID))
        .body("[0].name", is("test-deployment"))
        .body("[0].status", is(Status.INITIALIZED.name()));

    verify(deploymentServiceMock).getDeployments(eq(USER_ID));
  }

  @Test
  public void testGetDeploymentsNotFound() {
    when(deploymentServiceMock.getDeployments(eq(USER_ID)))
        .thenReturn(Optional.empty());

    given()
        .when()
        .get("/api/v1/deployments")
        .then()
        .statusCode(404)
        .body(is("Deployments not found"));
  }

  @Test
  public void testGetDeployment() {
    Deployment deployment = new Deployment();
    deployment.setUserId(USER_ID);
    deployment.setDeploymentId(DEPLOYMENT_ID);
    deployment.setName("test-deployment");
    deployment.setStatus(Status.INITIALIZED);
    deployment.setRuntime(Runtime.NODEJS_20_X);

    when(deploymentServiceMock.getDeployment(eq(USER_ID), eq(DEPLOYMENT_ID)))
        .thenReturn(Optional.of(deployment));

    given()
        .when()
        .get("/api/v1/deployments/{deploymentId}", DEPLOYMENT_ID)
        .then()
        .statusCode(200)
        .body("deploymentId", is(DEPLOYMENT_ID))
        .body("name", is("test-deployment"));

    verify(deploymentServiceMock).getDeployment(eq(USER_ID), eq(DEPLOYMENT_ID));
  }

  @Test
  public void testGetDeploymentNotFound() {
    when(deploymentServiceMock.getDeployment(eq(USER_ID), eq(DEPLOYMENT_ID)))
        .thenReturn(Optional.empty());

    given()
        .when()
        .get("/api/v1/deployments/{deploymentId}", DEPLOYMENT_ID)
        .then()
        .statusCode(404)
        .body(is("Deployment not found"));
  }

  @Test
  public void testDeleteDeployment() {
    doNothing().when(deploymentServiceMock).deleteDeployment(eq(USER_ID), eq(DEPLOYMENT_ID));

    given()
        .when()
        .delete("/api/v1/deployments/{deploymentId}", DEPLOYMENT_ID)
        .then()
        .statusCode(200)
        .body(is("Deployment deleted"));

    verify(deploymentServiceMock).deleteDeployment(eq(USER_ID), eq(DEPLOYMENT_ID));
  }
}
