# Proxy Web Client

This extension enables you to develop your web client without constraints.
You just need to code it without care of credentials, proxy, etc.

Just configure it and you are ready to go... ;)

## Method available

example of configuration for the proxy use method impl by default

### Altitude


### Axione


### Basic
```yaml
        sfra:
          method: Basic
          baseUrl: ${SFR_BASE_URL}
          useProxy: true #if use proxy
          config:
            username: ${SFR_USERNAME}
            password: ${SFR_PASSWORD}

```

### Bouygues
```yaml
        bouygues:
          method: Bouygues
          baseUrl: ${BOUYGUES_BASE_URL}
          useProxy: true #if use proxy
          config:
            clientId: ${CLIENT_ID_BOUY}
            clientSecret: ${SECRET_BOUY}
            tokenUrl: ${BOUYGUES_TOKEN_URL} #optional
```

### Free
```yaml
        free:
          method: Free
          baseUrl: ${FREE_BASE_URL}
          useProxy: true #if use proxy
          config:
            login: ${FREE_LOGIN}
            password: ${FREE_PASSWORD}
            tokenUrl: ${FREE_TOKEN_URL}
```
### Gatape
```yaml
       ftel:
          method: Gatape
          baseUrl: ${GATAPE_BASE_URL}
          useProxy: true #if use proxy
          config:
            clientId: ${CLIENT_ID_GATAPE}
            clientSecret: ${SECRET_GATAPE}
            tokenUrl: ${GATAPE_TOKEN_URL} #optional
          ssl:
            trustStore:
              value: ${TRUSTSTORE_FILEJKS_OR_PATH_PEM_OR_BASE64}
```
### Iftr
```yaml
       iftr:
          method: Iftr
          baseUrl: ${IFTR_BASE_URL}
          useProxy: true #if use proxy
          config:
            login: ${IFTR_LOGIN}
            password: ${IFTR_PASSWORD}
            tokenUrl: ${IFTR_TOKEN_URL}
```
### None
```yaml
      none:
          method: None
          baseUrl: ${NONE_BASE_URL}
          useProxy: true #if use proxy
```
### X509
```yaml
      sdif:
          method: None
          baseUrl: ${SDAIF_BASE_URL}
          useProxy: true  #if use proxy
          ssl:
            trustStore:
              value: ${TRUSTSTORE_FILEJKS_OR_PATH_PEM_OR_BASE64}
              password: ${TRUSTSTORE_PASSWORD} # optional
            x509PemFile: # if set certificat to file
              keyPem: ${SDAIF_KEY_PEM}
              certPem: ${SDAIF_CERT_PEM}
            x509Pem: # if set certificat to base64
              keyPem: ${SDAIF_KEY_PEM}
              certPem: ${SDAIF_CERT_PEM}
            keystore: # if set certificat to keystore jks
              value: ${TRUSTSTORE_FILE_JKS}
              password: ${TRUSTSTORE_PASSWORD} # optional
          extraHeaders:
            x-api-key:
              value: ${SDAIF_API_KEY}
```
### OrangeDeveloper

## Issues

Please fell free to start a discussion or open an issue
if you have any question or suggestion.

## Contributing

Contributions and pull requests from the community are welcome.
