# 🚀 AWS Setup Guide - Before Deployment

## ⚠️ AWS Credentials Required

Before deploying to AWS, you need to configure your AWS credentials.

---

## Step 1: Get AWS Credentials

### Option A: Create New AWS Account (Recommended for Testing)

1. Go to https://aws.amazon.com/
2. Click "Create an AWS Account"
3. Follow the signup process (requires credit card, but Free Tier is available)
4. Once logged in, go to IAM Console: https://console.aws.amazon.com/iam/

### Option B: Use Existing AWS Account

If you already have an AWS account, proceed to Step 2.

---

## Step 2: Create IAM User with Access Keys

1. **Go to IAM Console**: https://console.aws.amazon.com/iam/
2. **Click "Users"** in the left sidebar
3. **Click "Create user"**
4. **User name**: `digitopia-deploy` (or any name you like)
5. **Select**: "Provide user access to AWS Management Console" (optional)
6. **Click "Next"**
7. **Attach policies directly**: Select these policies:
   - `AmazonEC2ContainerRegistryFullAccess`
   - `AmazonECS_FullAccess`
   - `CloudWatchLogsFullAccess`
   - `IAMFullAccess`
   - `AmazonVPCFullAccess`
   - `ElasticLoadBalancingFullAccess`
   - Or simply: `AdministratorAccess` (easiest for testing)
8. **Click "Next"** → **"Create user"**
9. **Click on the user** you just created
10. **Go to "Security credentials" tab**
11. **Scroll to "Access keys"** section
12. **Click "Create access key"**
13. **Select use case**: "Command Line Interface (CLI)"
14. **Check the confirmation box**
15. **Click "Next"** → **"Create access key"**
16. **⚠️ IMPORTANT**: Copy both:
    - **Access key ID** (e.g., `AKIAIOSFODNN7EXAMPLE`)
    - **Secret access key** (e.g., `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY`)
17. **Download .csv file** (for backup)

---

## Step 3: Configure AWS CLI

### Install AWS CLI (if not already installed)

**Windows (PowerShell):**
```powershell
# You already have awscli installed!
aws --version
```

If not installed:
```powershell
pip install awscli
```

### Configure Credentials

**Run this command:**
```powershell
aws configure
```

**Enter the following when prompted:**
```
AWS Access Key ID [None]: <paste your Access Key ID>
AWS Secret Access Key [None]: <paste your Secret Access Key>
Default region name [None]: us-east-1
Default output format [None]: json
```

**Example:**
```
AWS Access Key ID [None]: AKIAIOSFODNN7EXAMPLE
AWS Secret Access Key [None]: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
Default region name [None]: us-east-1
Default output format [None]: json
```

---

## Step 4: Verify Configuration

```powershell
# Test if credentials work
aws sts get-caller-identity
```

**Expected output:**
```json
{
    "UserId": "AIDAI...",
    "Account": "123456789012",
    "Arn": "arn:aws:iam::123456789012:user/digitopia-deploy"
}
```

✅ If you see this, your credentials are configured correctly!

---

## Step 5: Deploy to AWS

Now you can run the deployment script:

```powershell
.\deploy-copilot.ps1
```

Or manually:

```powershell
# Initialize application
copilot app init digitopia

# Create environment
copilot env init --name production --profile default --default-config

# Deploy environment
copilot env deploy --name production

# Initialize services
copilot svc init --name api-gateway --svc-type "Load Balanced Web Service" --dockerfile api-gateway/Dockerfile

# Deploy service
copilot svc deploy --name api-gateway
```

---

## 🎯 Quick Reference Commands

### Check AWS Configuration
```powershell
aws configure list
aws sts get-caller-identity
```

### View/Edit Credentials
```powershell
# Credentials are stored here:
notepad $env:USERPROFILE\.aws\credentials

# Configuration is stored here:
notepad $env:USERPROFILE\.aws\config
```

### Reset Configuration
```powershell
aws configure
# Re-enter your credentials
```

---

## 💰 Cost Reminder

**Before deploying, remember:**
- Testing locally with `docker-compose up` is **FREE**
- AWS deployment costs **~$0.39/hour** or **~$291/month** if left running
- **Clean up immediately after testing** to avoid charges:
  ```powershell
  copilot app delete
  ```

---

## 🆘 Troubleshooting

### "AWS credentials are misconfigured or missing"

**Solution:**
```powershell
aws configure
# Enter your credentials again
```

### "Unable to locate credentials"

**Check if credentials file exists:**
```powershell
Test-Path $env:USERPROFILE\.aws\credentials
```

**If False, create it manually:**
```powershell
mkdir $env:USERPROFILE\.aws
notepad $env:USERPROFILE\.aws\credentials
```

**Add this content:**
```ini
[default]
aws_access_key_id = YOUR_ACCESS_KEY_ID
aws_secret_access_key = YOUR_SECRET_ACCESS_KEY
```

### "Request timeout" or "context deadline exceeded"

**Possible causes:**
1. Incorrect credentials
2. Network/firewall blocking AWS API calls
3. AWS service temporarily unavailable

**Solution:**
1. Verify credentials: `aws sts get-caller-identity`
2. Check internet connection
3. Try different AWS region: `aws configure set region us-west-2`

---

## 📚 Next Steps

Once credentials are configured:

1. ✅ **Test locally first** (FREE):
   ```powershell
   docker-compose up
   ```

2. ✅ **Deploy to AWS** (when ready):
   ```powershell
   .\deploy-copilot.ps1
   copilot svc deploy --all
   ```

3. ✅ **Clean up** (don't forget!):
   ```powershell
   copilot app delete
   ```

---

## 🔒 Security Best Practices

- ✅ Never commit `.aws/credentials` to Git
- ✅ Use IAM users with minimal permissions (not root account)
- ✅ Rotate access keys regularly
- ✅ Enable MFA on your AWS account
- ✅ Delete IAM user access keys when no longer needed

---

## 🎁 AWS Free Tier

If you're a new AWS user (first 12 months):
- ✅ 750 hours/month of RDS (db.t2.micro/t3.micro)
- ✅ 750 hours/month of Application Load Balancer
- ✅ 100 GB data transfer out
- ✅ 5 GB CloudWatch Logs

**This can reduce your cost from $0.39/hour to $0.18/hour!**

---

**Ready?** Configure your credentials and deploy! 🚀
