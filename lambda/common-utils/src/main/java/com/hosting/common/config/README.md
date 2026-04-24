# Service-Specific Configurations

The configuration is decentralized into service-specific classes to align with the Principle of Least Privilege. In our SAM template, each Lambda function is granted only the specific IAM policies required for its task (e.g., S3 access for the API, but CodeBuild access only for the Dispatcher).

By distributing the configuration:

- Policy Alignment: We ensure that the Java code only attempts to access resources and environment variables for which the specific Lambda has explicit IAM permissions.

- Resilience: This prevents initialization errors in functions that lack certain environment variables because they are purposefully restricted from accessing those services.

- Clean Architecture: It maintains a 1:1 mapping between the environment variables defined in the template.yml, the IAM policies granted, and the Java configuration classes.