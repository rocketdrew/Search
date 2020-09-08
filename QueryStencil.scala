package search.sol

import java.io._

import search.src.{FileIO, PorterStemmer, StopWords}

import scala.collection.mutable.HashMap

/**
 * Represents a query REPL built off of a specified index
 *
 * @param titleIndex    - the filename of the title index
 * @param documentIndex - the filename of the document index
 * @param wordIndex     - the filename of the word index
 * @param usePageRank   - true if page rank is to be incorporated into scoring
 */
class Query(titleIndex: String, documentIndex: String, wordIndex: String,
            usePageRank: Boolean) {

  // Maps the document ids to the title for each document(check)
  private val idsToTitle = new HashMap[Int, String]

  // Maps the document ids to the euclidean normalization for each document
  private val idsToMaxFreqs = new HashMap[Int, Double]

  // Maps the document ids to the page rank for each document
  private val idsToPageRank = new HashMap[Int, Double]

  // Maps each word to its inverse document frequency (check)
  private val wordToInvFreq = new HashMap[String, Double]

  // Maps each word to a map of document IDs and frequencies of documents that
  // contain that word (tf)(check)
  private val wordsToDocumentFrequencies =
  new HashMap[String, HashMap[Int, Double]]


  /**
   * Handles a single query and prints out results
   *
   * @param userQuery - the query text
   */
  private def query(userQuery: String) {

    //***************************************
    //PREPARING THE QUERY
    //***************************************

    //makes query words lowercase and removes leading/trailing whitespace
    val lowerCaseUserQuery = userQuery.toLowerCase.trim
    //gets each word in query into an array
    val userQueryArray = lowerCaseUserQuery.split(" ")
    //remove stop words from query
    val noStops = userQueryArray.filter(n => !StopWords.isStopWord(n))
    //stem user query
    val stemmedQuery = PorterStemmer.stemArray(noStops)
    //list to keep track of top ten highest scoring documents
    //elements are stored as IdTotalScoreTuple s,
    // which are data structure representing this: (id, TotalrelevanceToQuery)
    var idToQueryRelToReturnList: List[IdTotalScoreTuple] = List()


    //********************************************
    //CHECKING EACH PAGE FOR THE TOP 10 RESULTS
    //********************************************

    //loop through each page
    for ((k, v) <- idsToTitle) {
      //get current page id
      val currPageId = k
      //initialize relevance score for current page
      var currPageRelScore: Double = 0.0

      //loop through each word in the query (still on current page^)
      for (wrd <- stemmedQuery) {
        //gets word's term freq for current page, or 0.0 if not in map
        var wrdTf: Double = 0.0

        wordsToDocumentFrequencies.get(wrd) match {
          case None => 0
          case Some(idToFreqTable) => idToFreqTable.get(currPageId) match {
            case None => 0
            case Some(freq) => wrdTf = freq
          }
        }

        //gets word's inv doc freq for current page, or 0.0 if not in map
        var wrdIdf: Double = 0.0
        wordToInvFreq.get(wrd) match {
          case None => 0
          case Some(dbl) => wrdIdf = dbl
        }

        //adds the current word's (tf * idf) to
        // the current page's relevance score,
        // then loop for current query word ends,
        //and it goes on to the next word in the query
        currPageRelScore += wrdTf * wrdIdf
      }

      //At this point, one page has its full relevance score to each word
      // in the query, not including page rank.

      //if usePageRank is true, multiplies relevance score by pagerank
      if (usePageRank) {
        var pageRankToUse = 1.0
        idsToPageRank.get(currPageId) match {
          case None => throw new RuntimeException(
            "Error: Id found in other tables not found in pagerank table")
          case Some(pr) => pageRankToUse = pr
        }
        //println(currPageId + " " + pageRankToUse)
        currPageRelScore *= pageRankToUse
      }

      //Now the page's relevance score is complete.
      //if this page's RelScore is in the
      //top ten, the top ten list (idToQueryRelToReturnList)
      //will be updated

      //the tuple of id and total score for this page
      val thisPagesIdAndScore = new IdTotalScoreTuple(
        currPageId, currPageRelScore)


      //if the list is less than 10 elements long, it is
      // automatically in the top ten (for now)
      //ONLY IF RELSCORE != 0
      if (idToQueryRelToReturnList.length < 10 && currPageRelScore != 0) {
        //cons tuple to list, then sort
        idToQueryRelToReturnList =
          (thisPagesIdAndScore :: idToQueryRelToReturnList).sortBy(
            tupl => tupl.relToQuer).reverse

        //if the list has ten items, check the last (least relevant) item, and
        // if the current page's tuple has greater relevance, replace it
      } else if (idToQueryRelToReturnList.length == 10 &&
        idToQueryRelToReturnList(9).relToQuer < thisPagesIdAndScore.relToQuer) {
        idToQueryRelToReturnList = idToQueryRelToReturnList.take(9)
        idToQueryRelToReturnList =
          (thisPagesIdAndScore :: idToQueryRelToReturnList).sortBy(
            tupl => tupl.relToQuer).reverse
      }

      //here is where loop over the current page ends,
      // and the next page's relevance will be calculated,
      // resulting in it possibly being placed in the top ten list
    }

    //***************************************
    //PRINTING THE SEARCH RESULT
    //***************************************

    //At this point, the top ten list is complete, and will be printed out
    val listOfIdToReturn = idToQueryRelToReturnList.map(n => n.id)


    printResults(listOfIdToReturn.toArray)


  }

  /**
   * Format and print up to 10 results from the results list
   *
   * @param results - an array of all results
   */
  private def printResults(results: Array[Int]) {
    if (results.length == 0) {
      println("Could not find any relevant pages for your search :(")
    }
    for (i <- 0 until Math.min(10, results.size)) {
      println("\t" + (i + 1) + " " + idsToTitle(results(i)))
    }
  }

  def readFiles(): Unit = {
    FileIO.readTitles(titleIndex, idsToTitle)
    FileIO.readDocuments(documentIndex, idsToMaxFreqs, idsToPageRank)
    FileIO.readWords(wordIndex, wordsToDocumentFrequencies)

    //added to fill in wordToInvFreqvvvvvvvvvv

    //number of total documents
    val numOfTotDoc = idsToTitle.size.toDouble

    for (wrd <- wordsToDocumentFrequencies.keys) {
      var numDocsWthWrd = 0.0

      wordsToDocumentFrequencies.get(wrd) match {
        case None => throw new RuntimeException(
          "Should be initialized already")
        case Some(mp) => numDocsWthWrd = mp.size
      }

      val idfCalculated = Math.log(numOfTotDoc / numDocsWthWrd)
      wordToInvFreq.put(wrd, idfCalculated)

    }
  }

  /**
   * Starts the read and print loop for queries
   */
  def run() {
    val inputReader = new BufferedReader(new InputStreamReader(System.in))

    // Print the first query prompt and read the first line of input
    print("search> ")
    var userQuery = inputReader.readLine()

    // Loop until there are no more input lines (EOF is reached)
    while (userQuery != null) {
      // If ":quit" is reached, exit the loop
      if (userQuery == ":quit") {
        inputReader.close()
        return
      }

      // Handle the query for the single line of input
      query(userQuery)

      // Print next query prompt and read next line of input
      print("search> ")
      userQuery = inputReader.readLine()
    }

    inputReader.close()
  }
}

object Query {
  def main(args: Array[String]) {
    try {
      // Run queries with page rank
      var pageRank = false
      var titleIndex = 0
      var docIndex = 1
      var wordIndex = 2
      if (args.size == 4 && args(0) == "--pagerank") {
        pageRank = true;
        titleIndex = 1
        docIndex = 2
        wordIndex = 3
      } else if (args.size != 3) {
        println("Incorrect arguments. Please use [--pagerank] <titleIndex> "
          + "<documentIndex> <wordIndex>")
        System.exit(1)
      }
      val query: Query = new Query(args(titleIndex), args(docIndex),
        args(wordIndex), pageRank)
      query.readFiles()
      query.run()
    } catch {
      case _: FileNotFoundException =>
        println("One (or more) of the files were not found")
      case _: IOException => println("Error: IO Exception")
    }
  }
}