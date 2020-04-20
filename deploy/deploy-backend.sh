echo "Enter Environment Profile"
read ENV

# echo "Enter S3 Bucket ARN"
# read S3_ARN

echo "Enter AWS Access Key ID"
read ACCESS_KEY

echo "Enter AWS Secret Access Key"
read SECRET_ACCESS

# echo "Enter Image name (dockerRepo/Image:tag)"
# read IMAGE_NAME

echo "Enter Stripe Key"
read STRIPE_KEY

echo ""
RDS_URL="jdbc:mysql://$(AWS_PROFILE=${ENV} aws rds describe-db-instances --db-instance-identifier mysql-database | jq -r '.DBInstances[0].Endpoint.Address'):3306/csye7200?useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=EST&createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true"
echo "RDS URL: ${RDS_URL}"
echo ""
echo ""
echo ""

# echo "Creating namespace on k8s cluster"
# kubectl create namespace api
# kubectl create namespace ui

echo "Deploying Single Node ElasticSearch Cluster"

kubectl apply -f ./elastic/es-service-account.yaml -n api
kubectl apply -f ./elastic/es-replication-controller.yaml -n api
kubectl apply -f ./elastic/es-service.yaml -n api
sleep 30

ES_URL=$(kubectl get svc elasticsearch -o json -n api| jq -r '.status.loadBalancer.ingress[0].hostname')

echo "Creating MySQL Secrets"

echo "Enter MySQL username"
read RDS_USER

echo "Enter MySQL Password"
read RDS_PASSWORD

kubectl create secret generic credentials \
  --from-literal=user=${RDS_USER} \
  --from-literal=password=${RDS_PASSWORD} \
  --from-literal=host=${RDS_URL} \
  --from-literal=elb=${ES_URL} \
  --from-literal=stripeKey=${STRIPE_KEY} \
  --from-literal=AWS_ACCESS_KEY_ID=${ACCESS_KEY} \
  --from-literal=AWS_SECRET_ACCESS_KEY=${SECRET_ACCESS} \
  -n api

echo "Creating Image Pull Secret"
echo "Enter Docker username"
read DOCKER_USER

echo "Enter DOCKER Password"
read DOCKER_PASSWORD

echo "Enter DOCKER Email"
read DOCKER_EMAIL

kubectl create secret docker-registry mysecret --docker-server=https://index.docker.io/v1/ \
 --docker-username=${DOCKER_USER} \
 --docker-password=${DOCKER_PASSWORD} \
 --docker-email=${DOCKER_EMAIL} \
 -n api

kubectl create secret docker-registry mysecret --docker-server=https://index.docker.io/v1/ \
 --docker-username=${DOCKER_USER} \
 --docker-password=${DOCKER_PASSWORD} \
 --docker-email=${DOCKER_EMAIL} \
 -n ui

echo "Creating Backend Deployment"
kubectl apply -f backend-service-account.yaml -n api

# sed -i "s|placeholder|$IMAGE_NAME|" deployment.yaml
kubectl apply -f deployment.yaml -n api

echo "Exposing Backend Service with Node Port"
kubectl expose deployment backend -n api  --type=NodePort --port 8080 --target-port 8080

kubectl create clusterrolebinding jenkins-default --clusterrole=cluster-admin --serviceaccount=jenkins:default

echo "Creating Ingress for backend"
helm install --name nginx-ingress stable/nginx-ingress --set rbac.create=true --namespace api

kubectl apply -f ./ingress/backend-ingress.yaml -n api

# kubectl get ingress backend-ingress -n api
sleep 20
AWS_PROFILE=${ENV} aws lambda invoke --function-name sql-loader

echo "Done"