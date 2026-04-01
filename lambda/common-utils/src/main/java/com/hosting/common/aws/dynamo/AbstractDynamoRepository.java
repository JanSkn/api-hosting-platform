package com.hosting.common.aws.dynamo;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.IgnoreNullsMode;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

// DynamoDB exceptions are handled globally by DynamoDbExceptionMapper when called by
// core-backend-lambda
public abstract class AbstractDynamoRepository<T> {

  protected DynamoDbTable<T> table;
  private final Class<T> entityClass;

  // We inject the DynamoDbEnhancedClient in the concrete subclass rather than here
  // because CDI (Quarkus) creates beans from concrete classes. Additionally, the concrete class
  // must provide specific arguments (TABLE_NAME and entity class) to the superclass constructor.
  protected AbstractDynamoRepository(
      DynamoDbEnhancedClient enhancedClient, String tableName, Class<T> entityClass) {
    this.table = enhancedClient.table(tableName, TableSchema.fromBean(entityClass));
    this.entityClass = entityClass;
  }

  public Optional<T> get(String partitionKey, String sortKey) {
    return Optional.ofNullable(
        table.getItem(r -> r.key(k -> k.partitionValue(partitionKey).sortValue(sortKey))));
  }

  public Optional<List<T>> getByUserId(String userId) {
    List<T> items =
        table.query(QueryConditional.keyEqualTo(k -> k.partitionValue(userId))).items().stream()
            .collect(Collectors.toList());
    return Optional.of(items);
  }

  /**
   * @warning Avoid using this for large datasets, as sorting occurs in memory after the DynamoDB
   *     query.
   */
  public Optional<List<T>> getByUserId(String userId, Comparator<T> comparator) {
    List<T> items =
        table.query(QueryConditional.keyEqualTo(k -> k.partitionValue(userId))).items().stream()
            .sorted(comparator)
            .collect(Collectors.toList());
    return Optional.of(items);
  }

  /**
   * @warning This method will overwrite existing item with a completely new item.
   */
  public void put(T entity) {
    table.putItem(entity);
  }

  /** Updates all NON-NULL attributes of the item. */
  public void update(T entity) {
    // ignoreNullsMode is needed to prevent null attributes in the entity from overwriting existing
    // attributes in the item
    table.updateItem(b -> b.item(entity).ignoreNullsMode(IgnoreNullsMode.SCALAR_ONLY));
  }

  public void delete(T entity) {
    table.deleteItem(entity);
  }
}
