gradle clean bootJar && ^
docker build --no-cache --tag payment-calculator-backend:latest .\ && ^
cmd /k