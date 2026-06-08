import { Amplify } from "aws-amplify";
import { config, isLocalStack } from "../config";

export const configureAmplify = () => {
  Amplify.configure({
    Auth: {
      Cognito: {
        userPoolId: config.USER_POOL_ID,
        userPoolClientId: config.USER_POOL_CLIENT_ID,
        ...(isLocalStack ? {
          userPoolEndpoint: "http://localhost:4566",
          region: config.AWS_REGION,
        } : {
          region: config.AWS_REGION,
        }),
      },
    },
  });
};
