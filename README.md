pubmed-trends
=============

Trends and concept extraction from PubMed

export _JAVA_OPTIONS="-Xms2G -Xmx6G"

## Histogram frequency distribution

SELECT frequency, COUNT(Phrase.id) FROM Phrase GROUP BY frequency ORDER BY frequency;

