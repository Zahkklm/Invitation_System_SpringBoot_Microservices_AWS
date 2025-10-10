# AWS Lambda - Cognito Post-Confirmation Trigger

This Lambda function automatically creates a user in your User Service when a user confirms their registration in AWS Cognito.

## Setup Instructions

### 1. Create the Lambda Function
1. Go to AWS Lambda Console
2. Create a new function:
   - **Runtime:** Python 3.11
   - **Function name:** `CognitoPostConfirmationUserSync`
   - **Execution role:** Create a new role with basic Lambda permissions

### 2. Configure Environment Variables
Add these environment variables to your Lambda function:
- `USER_SERVICE_URL`: Your User Service endpoint (e.g., `https://your-api-gateway.com/api/v1/users` or internal service URL)
- `USER_SERVICE_API_KEY`: (Optional) API key for authenticating internal service calls

### 3. Configure Cognito Trigger
1. Go to your Cognito User Pool
2. Navigate to **User pool properties** → **Lambda triggers**
3. Add trigger:
   - **Trigger type:** Sign-up
   - **Sign-up:** Post confirmation trigger
   - **Lambda function:** Select `CognitoPostConfirmationUserSync`

### 4. IAM Permissions
Ensure your Lambda execution role has:
- Basic Lambda execution permissions (CloudWatch Logs)
- (Optional) VPC access if your User Service is in a private VPC
- No additional Cognito permissions needed (trigger is invoked automatically)

### 5. Network Configuration
- If your User Service is behind a VPC, configure Lambda VPC settings
- If using API Gateway with authentication, ensure Lambda can reach it
- Consider using AWS PrivateLink or VPC endpoints for secure internal communication

## How It Works

1. User completes registration in Cognito (confirms email/phone)
2. Cognito invokes this Lambda function with user attributes
3. Lambda extracts user data (Cognito sub, email, name)
4. Lambda calls your User Service POST /api/v1/users endpoint
5. User Service creates a new user record with Cognito sub as reference
6. Lambda returns success to Cognito (always succeeds even if User Service fails)

## User Service Endpoint Expected

Your User Service should accept POST requests like:

```json
POST /api/v1/users
{
  "cognitoSub": "uuid-from-cognito",
  "email": "user@example.com",
  "fullName": "John Doe",
  "status": "ACTIVE",
  "role": "USER",
  "createdBy": "uuid-from-cognito",
  "updatedBy": "uuid-from-cognito"
}
```

## Error Handling

- If User Service is unavailable, the Lambda logs the error but returns success to Cognito
- This ensures user registration is not blocked by User Service issues
- Consider implementing:
  - DLQ (Dead Letter Queue) for failed syncs
  - Retry logic with exponential backoff
  - CloudWatch alarms for monitoring failures

## Testing

Test the Lambda function with a sample Cognito event:

```json
{
  "version": "1",
  "triggerSource": "PostConfirmation_ConfirmSignUp",
  "region": "eu-north-1",
  "userPoolId": "eu-north-1_Cso4LZ1w5",
  "userName": "testuser",
  "request": {
    "userAttributes": {
      "sub": "12345678-1234-1234-1234-123456789012",
      "email": "testuser@example.com",
      "name": "Test User",
      "email_verified": "true"
    }
  },
  "response": {}
}
```

## Monitoring

- Check CloudWatch Logs for Lambda execution logs
- Monitor User Service for new user creation
- Set up CloudWatch alarms for Lambda errors or timeouts

## Security Best Practices

- Use environment variables for sensitive data
- Rotate API keys regularly if using internal API authentication
- Use VPC endpoints for private communication
- Enable CloudWatch Logs encryption
- Use IAM roles with least privilege
