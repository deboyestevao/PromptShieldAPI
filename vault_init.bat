@echo off
set VAULT_ADDR=http://127.0.0.1:8200
set VAULT_TOKEN=hvs.GdNCnNYLb9xn7hXjgZObAGUm

timeout /t 5

curl --header "X-Vault-Token: %VAULT_TOKEN%" ^
     --request POST ^
     --data "{\"data\": {\"AZURE_API_KEY\": \"6iX2AXkvGRFCOWsgU3o3h42aPlvIQJ0gq3IxCrBjR1cpSMmCeThBJQQJ99BFACfhMk5XJ3w3AAAAACOG9Rt9\"}}" ^
     %VAULT_ADDR%/v1/secret/data/application
