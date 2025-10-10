import json
import os
import urllib3
from datetime import datetime

# Initialize HTTP client
http = urllib3.PoolManager()

# Environment variables
# For local testing with ngrok: https://your-ngrok-url.ngrok.io/api/v1/users
# For AWS deployment: https://your-api-gateway.com/api/v1/users or internal service URL
USER_SERVICE_URL = os.environ.get('USER_SERVICE_URL', 'http://localhost:8084/api/v1/users')
USER_SERVICE_API_KEY = os.environ.get('USER_SERVICE_API_KEY', '')  # Optional: for internal API auth

def lambda_handler(event, context):
    """
    Cognito Post-Confirmation trigger handler.
    Automatically creates a user in the User Service after successful registration.
    
    Event structure:
    {
        "triggerSource": "PostConfirmation_ConfirmSignUp",
        "request": {
            "userAttributes": {
                "sub": "uuid",
                "email": "user@example.com",
                "name": "John Doe",
                ...
            }
        },
        "response": {}
    }
    """
    
    try:
        # Extract user attributes from Cognito event
        user_attributes = event['request']['userAttributes']
        cognito_sub = user_attributes.get('sub')
        email = user_attributes.get('email')
        name = user_attributes.get('name', '')
        family_name = user_attributes.get('family_name', '')
        
        # Validate required fields
        if not cognito_sub or not email:
            print(f"ERROR: Missing required fields. sub={cognito_sub}, email={email}")
            return event  # Return event to allow Cognito to proceed
        
        # Sanitize name fields - remove any non-letter/space characters
        import re
        def sanitize_name(name_str):
            # Remove any character that's not a letter or space
            sanitized = re.sub(r'[^a-zA-Z\s]', '', name_str)
            # Remove extra spaces and strip
            sanitized = ' '.join(sanitized.split())
            return sanitized if sanitized else None
        
        # Prepare user data for User Service
        # Construct full name from name and family_name, sanitizing to remove invalid chars
        sanitized_name = sanitize_name(name) if name else None
        sanitized_family_name = sanitize_name(family_name) if family_name else None
        
        if sanitized_name and sanitized_family_name:
            full_name = f"{sanitized_name} {sanitized_family_name}"
        elif sanitized_name:
            full_name = sanitized_name
        elif sanitized_family_name:
            full_name = sanitized_family_name
        else:
            # Fallback: Extract username from email and capitalize it
            full_name = email.split('@')[0].replace('.', ' ').replace('_', ' ').title()
            full_name = sanitize_name(full_name) or "User"
        
        user_data = {
            "cognitoSub": cognito_sub,  # Link to Cognito identity
            "email": email,
            "fullName": full_name,
            "role": "USER"  # Default role - matches your Role enum
        }
        
        # Call User Service to create the user
        headers = {
            'Content-Type': 'application/json',
            'X-User-Id': cognito_sub  # Required by your User Service controller
        }
        
        # Add API key if configured (for internal service-to-service auth)
        if USER_SERVICE_API_KEY:
            headers['X-API-Key'] = USER_SERVICE_API_KEY
        
        response = http.request(
            'POST',
            USER_SERVICE_URL,
            body=json.dumps(user_data).encode('utf-8'),
            headers=headers,
            timeout=5.0
        )
        
        if response.status == 201 or response.status == 200:
            print(f"SUCCESS: User created in User Service. Cognito sub={cognito_sub}, email={email}")
        elif response.status == 409:
            print(f"INFO: User already exists in User Service. Cognito sub={cognito_sub}, email={email}")
        else:
            print(f"ERROR: Failed to create user in User Service. Status={response.status}, Response={response.data.decode('utf-8')}")
        
    except Exception as e:
        print(f"ERROR: Exception occurred while creating user: {str(e)}")
        # Don't fail the Cognito confirmation process even if User Service call fails
        # You can implement retry logic or dead-letter queue here
    
    # Always return the event to allow Cognito to complete the confirmation
    return event
