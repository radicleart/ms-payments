# Payments Service

## Build

Build and skip/not skip tests

```bash
mvn -Dmaven.test.skip=true clean install
```

Build push the image;

```bash
docker build .
docker tag mijoco/ms_payments  mijoco/ms_payments
docker push mijoco/ms_payments:latest

```

Build push the image;

```bash
docker build .
docker tag mijoco/ms_payments  mijoco/ms_payments
docker push mijoco/ms_payments:latest

```
