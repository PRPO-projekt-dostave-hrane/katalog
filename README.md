# Dostava hrane - katalog

## Domena

Oblačni sistem za dostavo hrane. Stranke pregledujejo restavracije in menije, oddajo in plačajo naročilo, sledijo dostavi in naročilo ocenijo. Restavracije upravljajo svoj meni ter sprejemajo ali zavračajo naročila.

Ta repozitorij vsebuje storitev **katalog**, ki skrbi za restavracije, njihove menije (jedi in cene) in povprečne ocene restavracij. Podatki o restavracijah so vnaprej pripravljeni (testni podatki).

## Člani in lastništvo

| Član | Vloga |
|---|---|
| Matic Klemenc | lastnik storitve |

## Zagon in testi

### Zahteve

- JDK 25
- Maven ni potreben, ker projekt uporablja Maven Wrapper (`mvnw`)

### Gradnja in testi

```shell
./mvnw verify
```

Na Windowsu namesto `./mvnw` uporabi `.\mvnw.cmd`.

Testov še ni; ukaz generira kodo iz pogodbe in jo prevede.

### Zagon

```shell
./mvnw quarkus:dev
```

Storitev teče na `http://localhost:8080`. Swagger UI pa na `http://localhost:8080/q/swagger-ui`.