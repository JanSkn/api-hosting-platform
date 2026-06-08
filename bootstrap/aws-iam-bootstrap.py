# /// script
# requires-python = ">=3.12"
# dependencies = [
#    "boto3~=1.42.5",
# ]
# ///

import sys
import json
from getpass import getpass
from pathlib import Path
import configparser
import time
import boto3
from botocore.exceptions import ClientError

ALLOWED_ENVS = {"stg": "071308038858", "prod": "591292939760"}


# aws-vault add only creates the credentials in the OS key chain, we need to update the ~/.aws/config
#
# This base profile is the storage for the keys
# [profile <base-profile>]
# region = eu-central-1
# output = json
#
# This is the profile for the role to assume
# [profile <specific-role-profile>]
# source_profile = <base-profile>
# role_arn = arn:aws:iam::<account-id>:role/<role-name>
def update_aws_config(env, account_id, role_name):
    config_path = Path.home() / ".aws" / "config"
    config = configparser.ConfigParser()
    config.optionxform = str

    if config_path.exists():
        config.read(config_path)

    base_profile = f"base-{env}"
    admin_role_profile = f"bootstrap-admin-{env}"
    role_arn = f"arn:aws:iam::{account_id}:role/{role_name}"

    section_base = f"profile {base_profile}"
    if not config.has_section(section_base):
        config.add_section(section_base)
    config.set(section_base, "region", "eu-central-1")
    config.set(section_base, "output", "json")

    section_role = f"profile {admin_role_profile}"
    if not config.has_section(section_role):
        config.add_section(section_role)
    config.set(section_role, "source_profile", base_profile)
    config.set(section_role, "region", "eu-central-1")
    config.set(section_role, "role_arn", role_arn)

    config_path.parent.mkdir(parents=True, exist_ok=True)
    with open(config_path, "w") as configfile:
        config.write(configfile)
    print(f"✅ ~/.aws/config updated for '{env}'.")


def main():
    if len(sys.argv) < 2:
        print("❌ Use [stg|prod]")
        sys.exit(1)

    env = sys.argv[1].lower()
    if env not in ALLOWED_ENVS:
        print(f"❌ Only {list(ALLOWED_ENVS.keys())} are allowed")
        sys.exit(1)

    account_id = ALLOWED_ENVS[env]
    role_name = "BootstrapAdminRole"
    user_name = "bootstrap-admin"
    role_arn = f"arn:aws:iam::{account_id}:role/{role_name}"

    root_key_id = getpass(prompt="Root Access Key ID:")
    root_secret_key = getpass(prompt="Root Secret Access Key:")

    if not root_key_id or not root_secret_key:
        print("❌ Keys must not be empty.")
        sys.exit(1)

    iam = boto3.client(
        "iam", aws_access_key_id=root_key_id, aws_secret_access_key=root_secret_key
    )

    try:
        print(f"Creating role: {role_name}...")
        root_trust_policy = {
            "Version": "2012-10-17",
            "Statement": [
                {
                    "Effect": "Allow",
                    "Principal": {"AWS": f"arn:aws:iam::{account_id}:root"},
                    "Action": "sts:AssumeRole",
                }
            ],
        }

        iam.create_role(
            RoleName=role_name, AssumeRolePolicyDocument=json.dumps(root_trust_policy)
        )

        iam.attach_role_policy(
            RoleName=role_name, PolicyArn="arn:aws:iam::aws:policy/AdministratorAccess"
        )
        print("✅ Role created")

        print(f"Creating user: {user_name}...")
        iam.create_user(UserName=user_name)

        print("Waiting 10 seconds for AWS IAM replication...")
        time.sleep(10)

        scoped_trust_policy = {
            "Version": "2012-10-17",
            "Statement": [
                {
                    "Effect": "Allow",
                    "Principal": {"AWS": f"arn:aws:iam::{account_id}:user/{user_name}"},
                    "Action": "sts:AssumeRole",
                }
            ],
        }
        iam.update_assume_role_policy(
            RoleName=role_name, PolicyDocument=json.dumps(scoped_trust_policy)
        )

        print("Creating assume-role policy...")
        user_inline_policy = {
            "Version": "2012-10-17",
            "Statement": [
                {"Effect": "Allow", "Action": "sts:AssumeRole", "Resource": role_arn}
            ],
        }
        iam.put_user_policy(
            UserName=user_name,
            PolicyName="BootstrapAssumeRole",
            PolicyDocument=json.dumps(user_inline_policy),
        )
        print("✅ User created")

        print("Creating access key...")
        key_response = iam.create_access_key(UserName=user_name)
        access_key_id = key_response["AccessKey"]["AccessKeyId"]
        secret_access_key = key_response["AccessKey"]["SecretAccessKey"]

        print("\n⚠️ Warning: these keys have full access.")
        print(f"\nAccess Key ID:\n{access_key_id}")
        print(f"Secret Access Key:\n{secret_access_key}\n")

        update_aws_config(env, account_id, role_name)

        print("Cleaning up: Deleting provided Root Access Keys...")
        iam.delete_access_key(AccessKeyId=root_key_id)
        print("✅ Root Access Keys successfully deleted from AWS.")

        print("\n⚠️  Now do")
        print("   aws-vault add <base-profile-name>")

    except ClientError as e:
        print(f"❌ AWS Error: {e.response['Error']['Message']}")
        sys.exit(1)
    except Exception as e:
        print(f"❌ Unexpected Error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
