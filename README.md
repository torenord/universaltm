# Universal TM

En turingmaskinsimulator til bruk i kurset INF2080 på Universitetet i
Oslo. Koden er en videreutvikling av kode skrevet av Andreas Nakkerud.

## Filformatet

Turingmaskinener skal følge dette formatet:
- Første linjer skal inneholde antall tilstander i maskinen.
- Tomme linjer eller linjer som begynner med "---" er kommentarer og ignoreres.
- Andre linjer består av 5 felter adskilt av mellomrom.
 - Første felt er tallet til tilstanden (0, 1, 2, ...)
 - Andre felt er inputsymbolet lest fra tapen.
 - Tredje felt er neste tilstand maskinen skal gå i (-2 er aksept, -1 er rejekt).
 - Fjerde felt er outputsymbolet skrevet på tapen.
 - Femte felt er retningen tapehodet skal bevege seg (L er venstre, R er høyre).

Følgende er Sipsers eksempel 3.7 kodet i filformatet:

```
5

--- Eksempel 3.7 i Sipser --------------

--- q1 ---
0 0 1 _ R

--- q2 ---
1 _ -2 _ R
1 x 1 x R
1 0 2 x R

--- q3 ---
2 _ 4 _ L
2 x 2 x R
2 0 3 0 R

--- q4 ---
3 x 3 x R
3 0 2 x R

--- q5 ---
4 _ 1 _ R
4 x 4 x L
4 0 4 0 L
```

## Bruk

Turingmaskinen kan nå testes ved å kjøre UniversalTM med en
inputstreng og et maks antall steg før maskinen stoppes.

```
$ java UniversalTM M2.txt "0000" 100
--- Started on input "0000" --------------------------------

 Step | Configuration
------+-----------------------------------------------------
    0 | [0] 0  0  0  0
    1 |    [1] 0  0  0
    2 |     x [2] 0  0
    3 |     x  0 [3] 0
    4 |     x  0  x [2]
    5 |     x  0 [4] x
    6 |     x [4] 0  x
    7 |    [4] x  0  x
    8 | [4]    x  0  x
    9 |    [1] x  0  x
   10 |     x [1] 0  x
   11 |     x  x [2] x
   12 |     x  x  x [2]
   13 |     x  x [4] x
   14 |     x [4] x  x
   15 |    [4] x  x  x
   16 | [4]    x  x  x
   17 |    [1] x  x  x
   18 |     x [1] x  x
   19 |     x  x [1] x
   20 |     x  x  x [1]
   21 |     x  x  x    [-2]

--- Accepted input "0000" in 21 steps ----------------------
```

## Hvordan kjøre på tom streng?

```
$ java UniversalTM M2.txt "" 100
--- Started on input "" ------------------------------------

 Step | Configuration
------+-----------------------------------------------------
    0 | [0]
    1 | [-1]

--- Rejected input "" in 1 step ----------------------------
```
