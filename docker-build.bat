gradle clean bootJar && ^
docker build --no-cache --tag payment-calculator:latest .\ && ^
cmd /k