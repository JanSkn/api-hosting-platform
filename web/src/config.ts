// from public/config.js
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const config = (window as any).APP_CONFIG || {};

export const isLocalStack = config.ENVIRONMENT === "local" || config.ENVIRONMENT === "testing";

export const getApiBaseUrl = (): string => {
  return isLocalStack
    ? `http://localhost:4566/_aws/execute-api/${config.API_ID}/${config.ENVIRONMENT}`
    : `https://${config.API_ID}.execute-api.${config.AWS_REGION}.amazonaws.com/${config.ENVIRONMENT}`;
};
