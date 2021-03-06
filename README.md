# Valtionavustus

Valtionavustusten hakemiseen, käsittelyyn ja myöntämiseen tarkoitetut palvelut.

# Riippuvuudet

* Node.js tai io.js
* Leiningen (Clojure build tool): http://leiningen.org/
* Postgres

Riippuvuudet asentuvat ajamalla komennot:

    ./lein deps

ja

    npm install

Kehitysaikaisesti hyödyllisiä työkaluja:

* FakeSMTP: https://nilhcem.github.io/FakeSMTP/

# Tietokanta

Luo paikallinen postgres-datahakemisto

    initdb -d postgres

Käynnistä tietokantaserveri

    postgres -D postgres

Luo käyttäjä ```va``` (salasana: va)

    createuser -s va -P

Luo ```va-dev``` tietokanta

    createdb -E UTF-8 va-dev

Luo skeema ja lataa initial data (myös serverin start ajaa automaattisesti migraatiot)

    ./lein dbmigrate

Tietokannan saa kokonaan tyhjättyä ajamalla

    ./lein dbclear

# Käynnistys

*Huom*: trampoline on tarpeellinen, koska muuten JVM:n shutdown-hookkia ei
ajeta. Tämä taas jättää mahdollisesti resursseja vapauttamatta. Uberjarin
kautta ajaessa ongelmaa ei ole.

Paikallisesti:

    ./lein trampoline run

Kannan tyhjäys ja käynnistys paikallisesti:

    ./lein trampoline do dbclear, run

Tuotantoversiona:

    ./lein uberjar
    CONFIG=config/prod.edn java -jar target/uberjar/oph-valtionavustus-0.1.0-SNAPSHOT-standalone.jar

**Huom:** Jar-tiedoston versio voi vaihtua

Eri ympäristön voi ottaa käyttöön seuraavasti

    ./lein with-profile dev run
    ./lein with-profile test run
    ./lein with-profile prod run

# Dokumentaatio

Swagger-pohjainen API-dokumentaatio löytyy osoitteesta http://localhost:8080/doc

# Testien ajo

Testien ajo (ajaa myös mocha testit):

    ./lein with-profile test spec -f d

tai (automaattisesti)

    ./lein with-profile test spec -a
    
Ajettavaa testisettiä voi rajata tiettyyn tägiin lisäämällä esimerkiksi parametrin 
```-t ui``` tai ```-t server``` .  

Huom: Vaikka ```lein run``` lataa automaattisesti muuttuneet koodit,
Javascriptin automaattikäännöstä varten pitää käynnistää erilliseen
terminaaliin oma watch komento:

    npm run watch

## Mocha testit

Voit ajaa selaimella osoitteessa http://localhost:8080/test/runner.html

# Konfiguraatiot

Eri konfiguraatiot sijaitsevat hakemistossa ```config```.

Konfiguraatiotiedostot ovat EDN-formaatissa, joka on yksinkertaistettu
Clojure-tyyppinen tietorakenne.

# Muut komennot

Ovatko riippuvuudet päivittyneet?

    ./lein ancient
