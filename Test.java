If your Azure Kubernetes Service (AKS) is not accepting requests larger than 1MB, the issue is likely due to one of the following factors:

1. Ingress Controller Limits

If you are using NGINX Ingress Controller, it has a default request body size limit of 1MB. You can increase it by adding the following annotation to your Ingress resource:

apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: my-ingress
  annotations:
    nginx.ingress.kubernetes.io/proxy-body-size: "50m"
spec:
  rules:
    - host: myapp.example.com
      http:
        paths:
          - path: /
            pathType: Prefix
            backend:
              service:
                name: my-service
                port:
                  number: 80

🔹 Change "50m" to your desired size, such as "100m" for 100MB.

After updating the Ingress resource, restart the NGINX controller:

kubectl rollout restart deployment nginx-ingress-controller -n kube-system



⸻

2. AKS Load Balancer Limit

If you are using the Azure Application Gateway as an Ingress Controller, you need to update the request-body-size limit in the ConfigMap:

apiVersion: v1
kind: ConfigMap
metadata:
  name: appgw-ingress-config
  namespace: kube-system
data:
  request-body-size: "50m"

After updating, restart the Application Gateway Ingress Controller:

kubectl delete pod -n kube-system -l app=ingress-azure



⸻

3. NGINX Configuration Inside AKS Pods

If your application is running inside an NGINX-based container, modify nginx.conf:

http {
    client_max_body_size 50M;
}

Then restart your pods:

kubectl rollout restart deployment my-deployment



⸻

4. API Gateway (APIM) Limitations

If your request goes through Azure API Management (APIM) before reaching AKS, ensure the content-length limit is configured in Azure API Management policies:

<inbound>
    <base />
    <set-header name="Content-Length" exists-action="override">
        <value>52428800</value> <!-- 50MB -->
    </set-header>
</inbound>



⸻

5. Web Application Firewall (WAF) Rules

If AKS is behind Azure WAF, update the WAF policy:
	1.	Go to Azure Portal → WAF Policy
	2.	Increase the Max Request Body Size (default is 128 KB)
	3.	Save changes and restart your Ingress Controller or Application Gateway

⸻

6. Check Your Application Code

If the API itself enforces a max request size, increase the limit in Spring Boot:

For Spring Boot WebFlux:

spring.codec.max-in-memory-size=50MB
server.tomcat.max-swallow-size=50MB



⸻

Final Steps

✅ After making these changes, restart your AKS pods and test the upload again.

kubectl delete pod --all -n my-namespace

This should resolve the 1MB request size limitation in AKS. Let me know if you need more details!
