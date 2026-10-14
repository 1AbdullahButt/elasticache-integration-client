/** ElastiCacheIntegrations client package, public API. */
export { client } from "./client.js";
export type { ElastiCacheIntegrationsClientOptions } from "./client.js";
export type { ReconnectPolicyOptions } from "./session.js";
export {
  ElastiCacheIntegrationsCacheUnavailable,
  ElastiCacheIntegrationsConfigError,
  ElastiCacheIntegrationsError,
} from "./errors.js";

export const version = "0.0.1";
