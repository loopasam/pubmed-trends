# Data

- `NCITNCBO.owl`: ontology used for the benchmark
- `distribution-length-NCIT-concepts-without-stop-words.csv`: distribution of the length of the NCIT concepts, only concepts without stop words in them are considered. Build using:
```
SELECT totalLength, COUNT(OntologyTerm.id) 
FROM OntologyTerm
WHERE lengthWithoutStopWords = totalLength 
GROUP BY totalLength ORDER BY totalLength;
```
- `distribution-length-NCIT-concepts-with-stop-words.csv`: distribution of the length of the NCIT concepts, only concepts with stop words in them are considered. Sum of the concepts with stop words (13632) + concepts without stop words (93290) = total NCIT concepts (106922). Build using:
```
SELECT totalLength, COUNT(OntologyTerm.id) 
FROM OntologyTerm
WHERE lengthWithoutStopWords != totalLength 
GROUP BY totalLength ORDER BY totalLength;
```
- `distribution-frequency-in-corpus-all-shingles.csv`: 
