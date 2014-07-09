pubmed-trends
=============

Trends and concept extraction from PubMed

export _JAVA_OPTIONS="-Xms2G -Xmx6G"

## Histogram frequency distribution

SELECT frequency, COUNT(Phrase.id) FROM Phrase GROUP BY frequency ORDER BY frequency;

## On crunch:
mongod --dbpath /nfs/research2/textmining/croset/git/mongodb/db -f mongodb.conf

## Data size
- All citations needed in mongoDB: 9.949GB (round 4-5 hours)
- Index now and t-1: 
