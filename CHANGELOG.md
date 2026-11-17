# Changelog

## Pending

First public release of the ElastiCache integration clients for Java, Python and Node.js.

### Features

* All: Install into the DynamoDB client you already use: supported requests go through an ElastiCache Serverless for Valkey cache with DynamoDB caching enabled, and the rest of your code stays the same.
* All: Choose which tables and operations are cached, and how long entries live, with cache policy overrides.
* All: Keep serving requests from DynamoDB while the cache is unreachable, for a configurable fallback window, and reconnect in the background.
* All: Authenticate to the cache with IAM, using the DynamoDB client's region and credentials, and refresh the token in the background.
* All: Retries work as they do without the cache: the SDK's retry settings decide whether, and how many times, a request is retried.
* Java: Synchronous and asynchronous DynamoDB clients (AWS SDK for Java v2).
* Python: boto3 DynamoDB clients.
* Node.js: AWS SDK for JavaScript v3 DynamoDB clients.
