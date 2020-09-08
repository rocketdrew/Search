package search.sol

import java.io.{FileNotFoundException, IOException}

import search.src.{FileIO, PorterStemmer, StopWords}

import scala.collection.mutable
import scala.collection.mutable.HashMap
import scala.util.matching.Regex
import scala.xml.{Node, NodeSeq}

/**
  * Class to index xml files into text files for the querier to use
  */
class Index(wikiToUse: String, titleIndex: String,
            documentIndex: String, wordIndex: String) {
  //hashmap mapping ids to titles, to be written into title file later
  val hashMapForTitleFile: mutable.HashMap[Int,
    String] = new mutable.HashMap[Int, String]()

  //hashmap mapping words to ids to
  // doc freqs (tfs), to be written into word file later
  val hashMapForWordsFile: mutable.HashMap[String,
    mutable.HashMap[Int, Double]] =
  new HashMap[String, HashMap[Int, Double]]

  //hashmap for mapping ids to the names of the links on the page
  val idsToLinkNames: mutable.HashMap[Int, List[String]] =
    new mutable.HashMap[Int, List[String]]()

  //hashmaps for calculating pageranks
  val hmForMaxFreqs: mutable.HashMap[Int, Double] = new HashMap()

  val idsToPageRankMapr: mutable.HashMap[Int, Double] = new HashMap()

  val idsToPageRankMaprprime: mutable.HashMap[Int, Double] = new HashMap()

  //hashmap for weight that k gives j
  val idJtoidKtoWeight: mutable.HashMap[Int, mutable.HashMap[Int, Double]] =
    new HashMap[Int, HashMap[Int, Double]]


  //***************************************
  //WRITING TO TITLE FILE
  //***************************************

  //the main node for the corpus
  val mainNode: Node = xml.XML.loadFile(wikiToUse)

  //get the ids from wiki
  val idSeq: NodeSeq = mainNode \ "page" \ "id"

  //get the titles from wiki
  val titleSeq: NodeSeq = mainNode \ "page" \ "title"

  //populates id -> title hashmap
  makePairList(idSeq, titleSeq)

  //print to Title file
  FileIO.printTitleFile(titleIndex, hashMapForTitleFile)


  //populates the hashmap mapping ids to titles,
  // to be written into title file later
  //assumes that lists are equal length and in original order
  def makePairList(loi: NodeSeq, lot: NodeSeq): Unit = {
    if (loi.length != lot.length) {
      throw new RuntimeException("Mismatch between number of titles and ids")
    } else {
      var loii = loi
      var lott = lot
      while (loii.length != 0) {
        hashMapForTitleFile.put(loii.head.text.trim.toInt,
          lott.head.text.toString.trim)
        loii = loii.tail
        lott = lott.tail
      }
    }

    //    if (loi.length != 0) {
    //      hashMapForTitleFile.put(loi.head.text.trim.toInt,
    //        lot.head.text.toString.trim)
    //
    //      makePairList(loi.tail, lot.tail)
    //    }
  }


  //***************************************
  //WRITING TO WORDS FILE
  //***************************************


  //gets text from each page (parsing)
  val pageContentSeq: NodeSeq = mainNode \\ "text"

  //create a new regex to match links and words
  val regex = new Regex("""\[\[[^\[]+?\]\]|[^\W_]+'[^\W_]+|[^\W_]+""")


  //to keep track of each id
  var toIterateThroughBelow: NodeSeq = mainNode \ "page" \ "id"

  //initialize idsToLinkNames
  for (idd <- hashMapForTitleFile) {
    idsToLinkNames.put(idd._1, List())
  }

  //for each page
  for (nde <- pageContentSeq) {

    // Call findAllMatchIn to get an iterator of Matches
    val matchesIterator = regex.findAllMatchIn(nde.text.toString.trim)

    // Convert the Iterator to a List and extract the matched substrings
    val matchesList = matchesIterator.toList.map { aMatch => aMatch.matched }


    //now must get the words needed from the links

    //List of all the words from this page; links are turned to words
    var wordsNeeded: List[String] = List()


    //id of the document we are currently on
    val currId = toIterateThroughBelow.head.text.trim.toInt


    for (mtch <- matchesList) {
      //if it is a link:
      // [[anything]] or [[anything|something]]  or [[anything:something]]
      if (mtch.matches("""\[\[.+\]\]""")) {
        var inLink = mtch
        inLink = inLink.replaceAll("""\[\[""", "")
        inLink = inLink.replaceAll("""\]\]""", "")
        inLink = inLink.trim.toLowerCase

        //bool if title in corpus
        val inCorpNotPipe =
          hashMapForTitleFile.values.exists(_.toLowerCase == inLink)

        //bool for if not on same page
        val NotSamePage = hashMapForTitleFile.get(currId) match {
          case None => throw new RuntimeException("id and title should be here")
          case Some(titl) => titl.toLowerCase != inLink
        }


        //if metapage link, want both words
        if (inLink.contains(':')) {
          //this part stored for page rank if not already there:
          // vvvvvvvv

          //if doesnt link to self and link is in corpus
          if (NotSamePage && inCorpNotPipe) {
            idsToLinkNames.get(currId) match {
              case None => throw new RuntimeException(
                "Shouldnt reach here if initialized as above")
              case Some(lot) => if (!lot.contains(inLink)) {
                idsToLinkNames.update(currId, inLink :: lot)
              }
            }
          }
          //^^^^^^^^^

          val listForMetapageLink = inLink.split(
            ":").toList.flatMap(n => n.split(" ").toList).map(r => r.trim)
          wordsNeeded = wordsNeeded ::: listForMetapageLink
        } else if (inLink.contains('|')) {
          //if pipe link, want second word (after pipe)
          val listForPipeLink = inLink.split("|")

          //bool for if not on same page with pipe
          val PipeNotSamePage = hashMapForTitleFile.get(currId) match {
            case None => throw new RuntimeException(
              "id and title should be here")
            case Some(titl) =>
              titl.toLowerCase != listForPipeLink(0).toLowerCase
          }

          //bool for if in corpus or not
          val inCorpPipe =
            hashMapForTitleFile.values.exists(_.toLowerCase ==
              listForPipeLink(0).toLowerCase)

          //this part stored for page rank if not already there
          // vvvvvvvv
          if (PipeNotSamePage && inCorpPipe) {
            idsToLinkNames.get(currId) match {
              case None => throw new RuntimeException(
                "Shouldnt reach here if initialized as above")
              case Some(lot) =>
                if (!lot.contains(listForPipeLink(0).toLowerCase)) {
                  idsToLinkNames.update(currId,
                    listForPipeLink(0).toLowerCase :: lot)
                }
            }
          }
          //^^^^^^^^^
          wordsNeeded = listForPipeLink(1).toLowerCase.split(
            " ").toList.map(r => r.trim) ::: wordsNeeded
        }
        //else its just [[word]]
        else {

          //this part stored for page rank if not already there
          // vvvvvvvvv
          if (NotSamePage && inCorpNotPipe) {
            idsToLinkNames.get(currId) match {
              case None => throw new RuntimeException(
                "Shouldnt reach here if initialized as above")
              case Some(lot) => if (!lot.contains(inLink)) {
                idsToLinkNames.update(currId, inLink :: lot)

              }
            }
          }
          //^^^^^^^^^

          wordsNeeded = inLink.split(
            " ").toList.map(r => r.trim) ::: wordsNeeded

        }
        //else its not a link and just a word
      } else {
        wordsNeeded = mtch.trim.toLowerCase.split(
          " ").toList.map(r => r.trim) ::: wordsNeeded
      }
    }


    //adds title to wordsneeded
    hashMapForTitleFile.get(currId) match {
      case None => throw new RuntimeException(
        "Id not found when searching for title")
      case Some(titl) => wordsNeeded = titl.trim.split(" "
      ).toList.map(r => r.trim).map(n => n.toLowerCase) ::: wordsNeeded
    }

    //stem and remove stop words from words in this page
    wordsNeeded =
      wordsNeeded.filter(n =>
        !StopWords.isStopWord(n)).map(w => PorterStemmer.stem(w))


    //hashmap to keep track of the highest occuring word frequency
    val tempMap: mutable.HashMap[String, Double] = new HashMap()


    for (wrd <- wordsNeeded) {
      tempMap.get(wrd) match {
        case None => tempMap.put(wrd, 1.0)
        case Some(fre) => tempMap.update(wrd, fre + 1)
      }

    }


    var maxCount: Double = 1.0

    if (tempMap.size != 0) {


      maxCount = tempMap.maxBy { case (key, value) => value }._2
    }
    //store maxcount in hmForMaxFreqs
    hmForMaxFreqs.put(currId, maxCount)


    for (wrd <- wordsNeeded) {
      hashMapForWordsFile.get(wrd) match {
        case None =>
          val newmp = new HashMap[Int, Double]()
          newmp.put(currId, 1.0 / maxCount)
          hashMapForWordsFile.put(wrd, newmp)
        case Some(idToFreqTable) => idToFreqTable.get(currId) match {
          case None => idToFreqTable.put(currId, 1.0 / maxCount)
          case Some(freq) =>
            idToFreqTable.update(currId, freq + (1.0 / maxCount))
        }
      }
    }

    toIterateThroughBelow = toIterateThroughBelow.tail
  }


  //print to word File
  FileIO.printWordsFile(wordIndex, hashMapForWordsFile)


  //***************************************
  //WRITING TO DOCS FILE (PageRank Part)
  //***************************************

  //epsilon for weight calculation
  val eps = 0.15

  //sigma for euclidean distance calculation
  val sig = 0.001


  //initialize idsToPageRankr and rprime

  val corpusSize = hashMapForTitleFile.size


  for (idd <- hashMapForTitleFile) {
    //initialize idJtoidKtoWeight
    idJtoidKtoWeight.put(idd._1, new HashMap[Int, Double]())
    //idJtoidKtoWeight + (idd._1 -> new HashMap())

    //page j
    var contVal = ""

    hashMapForTitleFile.get(idd._1) match {
      case None => throw new RuntimeException("Should be here...")
      case Some(fre) => contVal = fre
    }

    for (k <- idsToLinkNames) {
      var resultWeight = 0.0

      //if links to nothing, links to everything once (minus self)
      if (idd._1 == k._1) {
        resultWeight = eps / corpusSize
      } else if (k._2.isEmpty) {
        resultWeight = (eps / corpusSize) + ((1 - eps) / (corpusSize - 1))
      } else if (k._2.contains(contVal.toLowerCase())) {
        resultWeight = (eps / corpusSize) + ((1 - eps) / k._2.length)
      } else {
        resultWeight = eps / corpusSize
      }


      idJtoidKtoWeight.get(idd._1) match {
        case None => throw new RuntimeException("should have been initialized")
        case Some(kToWeight) => kToWeight.get(k._1) match {
          case None => kToWeight.put(k._1, resultWeight)
          //kToWeight + (k._1 -> resultWeight)
          case Some(weightPresent) =>
            throw new RuntimeException(
              "Hashmap just created; no k to weight mapping should exist yet")
        }
      }
    }


    //initialize r
    idsToPageRankMapr.put(idd._1, 0)

    //initialize r prime
    idsToPageRankMaprprime.put(idd._1, 1.0 / corpusSize)
  }
  //weights, r, and r prime now initialized

  while (eucDist() > sig) {
    //make r into what r prime is
    for (idd <- idsToPageRankMaprprime) {
      idsToPageRankMapr.update(idd._1, idd._2)
    }

    //for length of corpus(j)
    for (iddj <- idsToPageRankMapr) {
      //make rprime 0
      idsToPageRankMaprprime.update(iddj._1, 0.0)
      //idsToPageRankMaprprime(iddj._1) = 0.0


      //for length of corpus(k)
      for (iddk <- idsToPageRankMapr) {
        var rpj = 0.0
        var rpjForCalculation = 0.0
        var weightJKForCalculation = 0.0
        var rkForCalculation = 0.0

        //update rpjFor Calculation
        idsToPageRankMaprprime.get(iddj._1) match {
          case None => throw new RuntimeException(
            "Missing initialization of r in algorithm for pagerank")
          case Some(rSubj) => rpjForCalculation = rSubj
        }

        //update weight for calculation
        idJtoidKtoWeight.get(iddj._1) match {
          case None => throw new RuntimeException(
            "Missing initialization of r in algorithm for pagerank")
          case Some(kweightmap) => kweightmap.get(iddk._1) match {
            case None => throw new RuntimeException(
              "Missing initialization of r in algorithm for pagerank")
            case Some(wjk) => weightJKForCalculation = wjk
          }
        }

        //update rk for calculation
        idsToPageRankMapr.get(iddk._1) match {
          case None => throw new RuntimeException(
            "Missing initialization of r in algorithm for pagerank")
          case Some(rSubk) => rkForCalculation = rSubk
        }

        rpj = rpjForCalculation + (weightJKForCalculation * rkForCalculation)
        idsToPageRankMaprprime.update(iddj._1, rpj)
      }

    }


  }

  //at this point, page rank is done being calculated

  //write to doc file
  FileIO.printDocumentFile(documentIndex,
    hmForMaxFreqs, idsToPageRankMaprprime)


  //^^^^^^^^

  //calculates Euclidean distance
  def eucDist(): Double = {
    var toReturnBeforeRoot = 0.0
    for (idd <- idsToPageRankMapr) {
      var currR = 0.0
      var currRprime = 0.0

      idsToPageRankMapr.get(idd._1) match {
        case None => throw new RuntimeException(
          "Missing initialization of r")
        case Some(rSubi) => currR = rSubi
      }

      idsToPageRankMaprprime.get(idd._1) match {
        case None => throw new RuntimeException(
          "Missing initialization of r prime")
        case Some(rPrimeSubi) => currRprime = rPrimeSubi
      }


      val rMinusrprimeSquared: Double =
        (currRprime - currR) * (currRprime - currR)

      toReturnBeforeRoot += rMinusrprimeSquared
    }

    return Math.sqrt(toReturnBeforeRoot)
  }

}

object Index {
  def main(args: Array[String]) {
    try {
      // Index the corpus provided
      val corpus = 0
      val titleIndex = 1
      val docIndex = 2
      val wordIndex = 3
      if (args.size == 4) {
        val ind: Index = new Index(args(corpus), args(titleIndex),
          args(docIndex), args(wordIndex))
      } else {
        println("Incorrect arguments. Please use <corpus>.xml <titleIndex> "
          + "<documentIndex> <wordIndex>")
        System.exit(1)
      }

    } catch {
      case _: FileNotFoundException =>
        println("One (or more) of the files were not found")
      case _: IOException => println("Error: IO Exception")
    }
  }
}


