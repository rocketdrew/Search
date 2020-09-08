# Search
Search engine on corpus of Wikipedia pages


Written Questions:
1. Problematic Queries: We can mitigate the risk of misinformation associated
 with these data voids in a few different ways. One possibility is to store a 
file containing problematic terms that users should not ever query; when 
populating wordsNeeded, every term could be checked against this file, and if 
there is a match, never added to wordsNeeded. A less authoritarian modification
 would be to flag these problematic terms in the query, and then require a 
minimal pageRank score for the pages that contain the problematic term, in order
 for those pages to make it into the final 10 top search results. This would 
help guarantee the legitimacy of the search results.

Fragmented Concepts: echo chambers are stronger when the language used by the 
Left and the Right differs. Though there will always be fringe terms that 
mainstream pundits, writers, and politicians never pick up, a greater effort can 
certainly be made to co-opt the other side’s language. Republicans can take a 
page from Warren’s book and tell voters about what they’re “fighting” for, and 
Democrats can use Republican language like “choice” and “fake news.” Journalists
 can use the language of both sides, including if they’re just using quotes from
 speakers across the political spectrum. This would help break down the walls of
 our echo chambers, by directing users who input queries with politicized 
language to pages that feature liberal as well as conservative perspectives.


2. A current vulnerability is that PageRank is optional, so there may be no 
check on authority for some queries. This could lead to low quality or non 
authoritative pages being returned, which may contain inaccurate or misleading 
information.  Making the PageRank weight in the query a function of the idf (so
 fewer relevant documents in the corpus makes it more necessary that those 
documents are credible), as well as making the Page Rank mandatory, might help 
remedy this situation.

A possibility for what to do when there is little content to return is send a 
warning to the user that there is scarce relevant content, and that whatever was
 returned may be inaccurate or misleading.


3. To boost its own pages, an organization should not link to any pages outside,
 but they want outside pages, especially authoritative pages, to link to their
 pages as directly as possible. The organization then faces a tradeoff between 
more links vs fewer links between pages on their own site, but they can solve 
this optimization problem to maximize their average PageRank, or the average for
 a selection of pages that they really want to boost. If an inaccurate page has
 gone viral, the links from social media sites would boost the pagerank, and 
thus the relevance of the false article, though probably the ranks of each of 
those links is low because nothing links to them. Someone attempting to research
 the story would be very likely to see the inaccurate article.  


Instructions For Use:
To use the search engine, a user opens a command-line interface and they open
 the Index.scala file. They specify four arguments: the .xml file they’re using
 for their corpus, then the <titles>.txt, a file to store titles and IDs of 
pages in the corpus; <docs>.txt, a file to store the pageRank values of the 
documents in the corpus, and <words>.txt, a file to store the term frequencies
 of the words in the corpus. 

Once indexing is complete, the user opens the Query.scala file. They specify 
three or four arguments: the <titles>.txt file, <docs>.txt file, and <words>.txt
 file to read from, with an optional first argument “--pagerank”, indicating
 whether or not they would like to use page rank in their search. If so, the
 final determination of relevance will be the product of the pagerank and the
 query’s TFIDF, if not, relevance will be based solely on TFIDF. A REPL is used
 to continuously prompt the user to provide a search query, and the top ten most
 relevant results from the corpus are returned until the user enters “:quit”
 into the REPL.


Design Overview:
Indexer:
The indexer’s design is split into three parts corresponding to its main
 functions: populating the titles file, populating the words file, and 
populating the docs file.

Titles File:
The indexer fills in the <titles>.txt file with the id of each document and its
 corresponding title. This is done by parsing the xml file for each of the ids 
and titles, adding each (id, title) pair to a hashmap, and writing that 
information into the <titles>.txt file.

Words File:
The indexer fills in the <words>.txt file with the words from the corpus, the
 ids of pages that those words appear on, and the term frequency of the words on
 those pages. This is accomplished by parsing the xml file for the text from 
each page, parsing that text for words and links, and adding the words relevant
 to searching to a list called wordsNeeded. The words in the title are also 
added to wordsNeeded. For each page, the word count of each word is recorded,
 and is compared with the most frequently occurring word for term frequency (TF)
 calculation before it is put into the hashmap. This hashmap will contain each 
relevant word in the corpus (not stop words or words with more than a stem), and
 will map these words to a hashmap mapping an id to the term frequency of that 
word on that page. This hashmap is then read into the <words>.txt file.

Docs File:
The indexer fills in the <docs>.txt file by writing a hashmap to represent [ID
 -> pagerank] using the method provided in the File I/O class. To compute these
 pageranks, we first set up a nested hashmap to represent [document j -> 
[document k -> weight given from k to j]], where all the weights are initialized 
according to the equation provided in the handout. In determining these weights,
 we deal with a number of special cases: we make sure when we’re generating 
wordsNeeded for the corpus that the links to other titles 1) don’t refer to the
 document that contains them, 2) do refer to pages found in the corpus, 3) don’t
 contain duplicate links. When initializing the weights we account for the 4th
 special case by setting the weight for a document with no (valid) links as if
 they link everywhere. Having initialized the weights, we then run the algorithm
 from the handout: we set up two arrays, r, which is initialized to 0s, and r’,
 which is initialized to 1/(corpus size). We then manipulate r’ with an outer 
loop through documents j and an inner loop through the documents k that give
 their rank to j. For each k, we add to the rank of j the product of the weight
 from that k to that j and the pagerank of document k. Following this process 
for every document j, we compare r and r’: if the euclidean distance is larger
 than the sigma value we set (sig = ___), we set r = r’ and repeat the process
 for the rankings in r’. The loop concludes when the Euclidean distance between
 r and r’ is less than sigma, at which point we write the r’ pageranks to the 
<docs> file.


The <titles>, <words>, and <docs> files are the bridge between the two halves of
 this program: the Indexer and the Querier. The Indexer takes the xml files and
 transcribes the relevant information to these text files for the Querier to use
 later. The Querier reads in from these files and uses the information therein 
to return the document titles most relevant to the user-entered search query.


Querier:
The role of the querier is to accept query searches, which are one or more words
 inputted into a REPL, and return the top 10 most relevant results from the 
corpus. It begins by repopulating data structures to contain the information 
necessary for this task by running methods from the File I/O class on the 
corresponding .txt file (<titles>, <words>, <docs>). The querier then takes the
 inputted query and prepares it for comparison by trimming it, stemming it, and
 removing stop words. The relevance score of each page (taken from the text 
files) is determined using the TF score (from the indexer) and the IDF score 
(from the readFiles method of the querier). If page rank was desired from the
 initial input to the querier, the page rank (passed in from the docs file from
 the indexer) will also be used to calculate relevance. After the relevance of a 
page to the query is calculated, it is checked against a list of title/relevance
 score pairs to be returned. This list will accumulate the top 10 documents (or
 less, if fewer than 10 relevant documents) and return them to the user. The 
REPL then repeats until the user enters “:quit”.


Extra/failed features:
None


Known bugs: 
Our querier returns slightly different results in a slightly different 
order than the examples provided in the project handout. For example, when we 
search for the term "cats" in MedWiki, we get Kiritimati, Kattegat, Lynx, 
Morphology, and Northern Mariana Islands, but we also get some others, like
Isle of Man and Nirvana. For the search "ruler" we see a similar pattern: the
first six results listed on the handout appear in our results, but we also 
see a few different pages. We spent multiple hours scouring our code for what
might be responsible, but we couldn't find anything. We feel that our querier
still returns relevant pages.



Testing:
To test the indexer, we indexed a number of corpuses, then examined the 
resulting titles, docs, and words files. (The wikis we used in testing are 
included in the handin). We indexed the PageRankWiki to confirm that page 100
 produced a substantially higher rank than the other pages in the corpus, and 
that the page ranks summed to 1. We also indexed multiple corpuses containing 
three pages to test different arrangements of links between the three. For
 example, we tested a case when each page linked to one adjacent page, multiple
 cases when two pages linked to each other, and cases where pages linked to 
nothing at all. In cases when the link relationships among the pages were 
symmetric, we ensured all pages received equal ranks. In the cases when the 
links were not symmetric, eg two pages link to a third, we ensured that the 
ranks aligned with our expectations based on the pagerank algorithm, eg ranks 
were higher on the pages receiving more links. 


To test the querier, we wrote three classes: emptyQueryTest, unitQueryTest, and
 fullQueryTest. The empty case is a corpus containing no pages. When we index 
using this corpus, all 3 text files contain nothing. Querying any term returns
 nothing. unitQueryTest is a corpus containing a single document, which contains
 a single word: “alone”. As expected, each of the text files contain a single 
line of information. Importantly, querying the term “alone” still returns 
nothing because idf = log(n/ni), where n is the size of the corpus and ni is the
 number of pages that contain the queried term. In the single-page case, these 
numbers are equal, so we take the log of 1, which is always zero. We then 
multiply the tf by the idf, so the product is zero as well. Pages must have a
 nonzero score to be returned in the querier’s results, so the unit test, like
 the empty case, does not ever print results. The fullQueryTest is a corpus 
containing 10 pages. There are 11 total words in the corpus: zeroth, first, 
second, third, fourth, fifth, sixth, seventh, eighth, ninth, and page. We made 
sure the three text files reflected our expectations for a corpus of this size.
 We set up the TF scores so that when querying without pageRank, the top result
 will always be the page corresponding to the queried ordinal number; ie, the
 first result will be page 4 when querying “fourth”. After confirming this, we
 queried again using pageRank, and confirmed that the order of the results 
returned for every page was completely different than it had been without 
pageRank, but in accordance with the page ranks assigned to each page from the
 setup method. For example, we assigned page 4 the greatest pageRank score, so 
page 4 regularly appeared in the top search results for different terms.


Collaborators:
Victor Mora
