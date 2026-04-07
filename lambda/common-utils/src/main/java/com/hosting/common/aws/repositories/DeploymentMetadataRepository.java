package com.hosting.common.aws.repositories;

import com.hosting.common.aws.dynamo.AbstractDynamoRepository;
import com.hosting.common.aws.dynamo.models.Deployment;
import com.hosting.common.config.ProjectConfig;
import com.hosting.common.exceptions.DeploymentLimitReachedException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

@ApplicationScoped
public class DeploymentMetadataRepository extends AbstractDynamoRepository<Deployment> {
  private static final String TABLE_NAME = ProjectConfig.DynamoDB.DEPLOYMENTS_METADATA_TABLE;

  @Inject
  public DeploymentMetadataRepository(DynamoDbEnhancedClient dynamoDbClient) {
    super(dynamoDbClient, TABLE_NAME, Deployment.class);
  }

  // put would overwrite item, but we want to enforce max deployments per user limit, so we need to
  // check before put
  @Override
  public void put(Deployment entity) {
    int numberOfExistingDeployments =
        table
            .query(
                r ->
                    r.queryConditional(
                        QueryConditional.keyEqualTo(k -> k.partitionValue(entity.getUserId()))))
            .items()
            .stream()
            .filter(d -> !d.getDeploymentId().equals(entity.getDeploymentId())) // exclude same
            .toArray()
            .length;

    if (numberOfExistingDeployments >= ProjectConfig.Deployment.MAX_PER_USER) {
      throw new DeploymentLimitReachedException(ProjectConfig.Deployment.MAX_PER_USER);
    }
    table.putItem(entity);
  }
}
