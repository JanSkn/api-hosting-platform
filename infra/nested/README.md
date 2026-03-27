# Cognito
Cognito usually needs username + pw for Sign Up

Cognito User Pool entry has a required username that can't be changed.

When 
```yaml
UsernameAttributes:
    - email
```

then, email is the primary way

In the frontend:

```js
const response = await signUp({
  username, // <--- username is email because UsernameAttributes is set to email
  password,
  options: {
    userAttributes: { // attributes stored for a user
      email: username
      anyDefaultAttribute: "abc"
    }
  }
});
```

Example user as no custom username:
```json
{
    "Username": "b9ac08a9-a2be-443c-8fe5-bd9340896c8e",
    "Attributes": [
        {
            "Name": "anyDefaultAttribute",
            "Value": "abc"
        },
        {
            "Name": "email",
            "Value": "user@email.com"
        },
        {
            "Name": "sub",
            "Value": "b9ac08a9-a2be-443c-8fe5-bd9340896c8e"
        },
        {
            "Name": "email_verified",
            "Value": "false"
        }
    ],
    "UserCreateDate": 1774456785.318067,
    "UserLastModifiedDate": 1774456785.318067,
    "Enabled": true,
    "UserStatus": "CONFIRMED"
}
```