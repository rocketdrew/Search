package search.sol

import search.src.FileIO

import scala.collection.mutable

/**
 * A file to populate the title, docs, and words files as though they'd been
 * prepared by the indexer to facilitate query testing
 */

/**
 * A class to test an empty  corpus
 *
 * @param titleIndex    - the file containing titles
 * @param documentIndex - the file containing pageranks
 * @param wordIndex     - the file containing TFs
 */
class EmptyQueryTest(titleIndex: String,
                     documentIndex: String, wordIndex: String) {

  val EmptyMapIDtoPR: mutable.HashMap[Int, Double] = new mutable.HashMap

  val EmptyMapIDtoTitle: mutable.HashMap[Int, String] = new mutable.HashMap
  FileIO.printTitleFile(titleIndex, EmptyMapIDtoTitle)

  val EmptyMapIDtoMaxCount: mutable.HashMap[Int, Double] = new mutable.HashMap
  FileIO.printDocumentFile(documentIndex, EmptyMapIDtoMaxCount, EmptyMapIDtoPR)

  val EmptyWordtoDocFreq: mutable.HashMap[String, mutable.HashMap[Int, Double]] = new mutable.HashMap
  FileIO.printWordsFile(wordIndex, EmptyWordtoDocFreq)

}

/**
 * A class to test a single-page corpus
 *
 * @param titleIndex    - the file containing titles
 * @param documentIndex - the file containing pageranks
 * @param wordIndex     - the file containing TFs
 */
class UnitQueryTest(titleIndex: String,
                    documentIndex: String, wordIndex: String) {
  // A corpus with one page
  val UnitMapIDtoPR: mutable.HashMap[Int, Double] = new mutable.HashMap
  UnitMapIDtoPR.put(0, 1.0)

  val UnitMapIDtoTitle: mutable.HashMap[Int, String] = new mutable.HashMap
  UnitMapIDtoTitle.put(0, "Empty Test")
  FileIO.printTitleFile(titleIndex, UnitMapIDtoTitle)

  val UnitMapIDtoMaxCount: mutable.HashMap[Int, Double] = new mutable.HashMap
  UnitMapIDtoMaxCount.put(0, 1)
  FileIO.printDocumentFile(documentIndex, UnitMapIDtoMaxCount, UnitMapIDtoPR)

  val UnitWordtoDocFreq: mutable.HashMap[String, mutable.HashMap[Int, Double]] = new mutable.HashMap
  val aloneFreq: mutable.HashMap[Int, Double] = new mutable.HashMap
  aloneFreq.put(0, 1)
  UnitWordtoDocFreq.put("alone", aloneFreq)
  FileIO.printWordsFile(wordIndex, UnitWordtoDocFreq)
}

/**
 * A class to test a 10-page corpus
 *
 * @param titleIndex    - the file containing titles
 * @param documentIndex - the file containing pageranks
 * @param wordIndex     - the file containing TFs
 */
class FullQueryTest(titleIndex: String,
                    documentIndex: String, wordIndex: String) {
  // A corpus with 10 pages
  val FullMapIDtoPR: mutable.HashMap[Int, Double] = new mutable.HashMap
  FullMapIDtoPR.put(0, .1)
  FullMapIDtoPR.put(1, .1)
  FullMapIDtoPR.put(2, .2)
  FullMapIDtoPR.put(3, .2)
  FullMapIDtoPR.put(4, .25)
  FullMapIDtoPR.put(5, .001)
  FullMapIDtoPR.put(6, .009)
  FullMapIDtoPR.put(7, .04)
  FullMapIDtoPR.put(8, .05)
  FullMapIDtoPR.put(9, .05)

  val FullMapIDtoTitle: mutable.HashMap[Int, String] = new mutable.HashMap
  FullMapIDtoTitle.put(0, "Zeroth Page")
  FullMapIDtoTitle.put(1, "First Page")
  FullMapIDtoTitle.put(2, "Second Page")
  FullMapIDtoTitle.put(3, "Third Page")
  FullMapIDtoTitle.put(4, "Fourth Page")
  FullMapIDtoTitle.put(5, "Fifth Page")
  FullMapIDtoTitle.put(6, "Sixth Page")
  FullMapIDtoTitle.put(7, "Seventh Page")
  FullMapIDtoTitle.put(8, "Eighth Page")
  FullMapIDtoTitle.put(9, "Ninth Page")
  FileIO.printTitleFile(titleIndex, FullMapIDtoTitle)

  val FullMapIDtoMaxCount: mutable.HashMap[Int, Double] = new mutable.HashMap
  FullMapIDtoMaxCount.put(0, 10)
  FullMapIDtoMaxCount.put(1, 11)
  FullMapIDtoMaxCount.put(2, 12)
  FullMapIDtoMaxCount.put(3, 13)
  FullMapIDtoMaxCount.put(4, 14)
  FullMapIDtoMaxCount.put(5, 15)
  FullMapIDtoMaxCount.put(6, 16)
  FullMapIDtoMaxCount.put(7, 17)
  FullMapIDtoMaxCount.put(8, 18)
  FullMapIDtoMaxCount.put(9, 19)
  FileIO.printDocumentFile(documentIndex, FullMapIDtoMaxCount, FullMapIDtoPR)

  val zerothMap: mutable.HashMap[Int, Double] = new mutable.HashMap
  zerothMap.put(0, 1)
  zerothMap.put(1, 0.2)
  zerothMap.put(2, 0.4)
  //  zerothMap.put(3, 0.3)
  zerothMap.put(4, 0.6)
  zerothMap.put(5, 0.7)
  zerothMap.put(6, 0.9)
  //  zerothMap.put(7, 0.3)
  zerothMap.put(8, 0.5)
  zerothMap.put(9, 0.6)

  val firstMap: mutable.HashMap[Int, Double] = new mutable.HashMap
  firstMap.put(0, 0.3)
  firstMap.put(1, 1)
  firstMap.put(2, 0.3)
  firstMap.put(3, 0.5)
  firstMap.put(4, 0.6)
  //  firstMap.put(5, 0.8)
  //  firstMap.put(6, 0.4)
  firstMap.put(7, 0.3)
  firstMap.put(8, 0.5)
  firstMap.put(9, 0.6)

  val secondMap: mutable.HashMap[Int, Double] = new mutable.HashMap
  secondMap.put(0, 0.4)
  secondMap.put(1, 0.5)
  secondMap.put(2, 1.0)
  secondMap.put(3, 0.3)
  //  secondMap.put(4, 0.5)
  secondMap.put(5, 0.7)
  secondMap.put(6, 0.6)
  //  secondMap.put(7, 0.7)
  secondMap.put(8, 0.3)
  secondMap.put(9, 0.2)

  val thirdMap: mutable.HashMap[Int, Double] = new mutable.HashMap
  thirdMap.put(0, .4)
  thirdMap.put(1, .5)
  //  thirdMap.put(2, 1)
  thirdMap.put(3, 1)
  thirdMap.put(4, .6)
  thirdMap.put(5, .4)
  thirdMap.put(6, .7)
  thirdMap.put(7, .1)
  thirdMap.put(8, .2)
  //  thirdMap.put(9, .4)

  val fourthMap: mutable.HashMap[Int, Double] = new mutable.HashMap
  fourthMap.put(0, .4)
  fourthMap.put(1, .4)
  fourthMap.put(2, .3)
  //  fourthMap.put(3, .4)
  fourthMap.put(4, 1)
  fourthMap.put(5, .3)
  fourthMap.put(6, .5)
  //  fourthMap.put(7, .2)
  fourthMap.put(8, .6)
  fourthMap.put(9, .7)

  val fifthMap: mutable.HashMap[Int, Double] = new mutable.HashMap
  fifthMap.put(0, .2)
  fifthMap.put(1, .2)
  fifthMap.put(2, .5)
  fifthMap.put(3, .6)
  fifthMap.put(4, .6)
  fifthMap.put(5, 1)
  //  fifthMap.put(6, .5)
  fifthMap.put(7, .3)
  //  fifthMap.put(8, .6)
  fifthMap.put(9, .6)

  val sixthMap: mutable.HashMap[Int, Double] = new mutable.HashMap
  sixthMap.put(0, .3)
  sixthMap.put(1, .4)
  sixthMap.put(2, .5)
  //  sixthMap.put(3, .2)
  sixthMap.put(4, .7)
  //  sixthMap.put(5, .6)
  sixthMap.put(6, 1)
  sixthMap.put(7, .4)
  sixthMap.put(8, .5)
  sixthMap.put(9, .3)

  val seventhMap: mutable.HashMap[Int, Double] = new mutable.HashMap
  //  seventhMap.put(0, .3)
  seventhMap.put(1, .4)
  seventhMap.put(2, .3)
  seventhMap.put(3, .6)
  seventhMap.put(4, .4)
  //  seventhMap.put(5, .7)
  seventhMap.put(6, .7)
  seventhMap.put(7, 1)
  seventhMap.put(8, .3)
  seventhMap.put(9, .6)

  val eighthMap: mutable.HashMap[Int, Double] = new mutable.HashMap
  eighthMap.put(0, .1)
  eighthMap.put(1, .2)
  eighthMap.put(2, .1)
  //  eighthMap.put(3, .4)
  eighthMap.put(4, .4)
  eighthMap.put(5, .4)
  eighthMap.put(6, .2)
  //  eighthMap.put(7, .3)
  eighthMap.put(8, 1)
  eighthMap.put(9, .1)

  val ninthMap: mutable.HashMap[Int, Double] = new mutable.HashMap
  ninthMap.put(0, .3)
  ninthMap.put(1, .4)
  ninthMap.put(2, .5)
  ninthMap.put(3, .2)
  //  ninthMap.put(4, .6)
  ninthMap.put(5, .3)
  ninthMap.put(6, .3)
  ninthMap.put(7, .2)
  //  ninthMap.put(8, .5)
  ninthMap.put(9, 1)

  val pageMap: mutable.HashMap[Int, Double] = new mutable.HashMap
  //  pageMap.put(0, .1)
  //  pageMap.put(1, .2)
  //  pageMap.put(2, .3)
  //  pageMap.put(3, .4)
  //  pageMap.put(4, .5)
  //  pageMap.put(5, .6)
  pageMap.put(6, .7)
  pageMap.put(7, .8)
  pageMap.put(8, .9)
  pageMap.put(9, .1)

  val FullMapIDtoDocFreq: mutable.HashMap[String, mutable.HashMap[Int, Double]] = new mutable.HashMap
  FullMapIDtoDocFreq.put("zeroth", zerothMap)
  FullMapIDtoDocFreq.put("first", firstMap)
  FullMapIDtoDocFreq.put("second", secondMap)
  FullMapIDtoDocFreq.put("third", thirdMap)
  FullMapIDtoDocFreq.put("fourth", fourthMap)
  FullMapIDtoDocFreq.put("fifth", fifthMap)
  FullMapIDtoDocFreq.put("sixth", sixthMap)
  FullMapIDtoDocFreq.put("seventh", seventhMap)
  FullMapIDtoDocFreq.put("eighth", eighthMap)
  FullMapIDtoDocFreq.put("ninth", ninthMap)
  FullMapIDtoDocFreq.put("page", pageMap)
  FileIO.printWordsFile(wordIndex, FullMapIDtoDocFreq)
}

/**
 * The object that creates the queryTests
 */
object Test {

  /**
   * The method that creates the queryTests
   *
   * @param args - the first arg is either "Empty", "Unit", or "Full"
   *             the rest are the usual files
   */
  def main(args: Array[String]): Unit = {
    val corpus = 0
    val titleIndex = 1
    val docIndex = 2
    val wordIndex = 3

    if (args(corpus) == "Empty") {
      val ind: EmptyQueryTest = new EmptyQueryTest(args(titleIndex), args(docIndex), args(wordIndex))
    }
    if (args(corpus) == "Unit") {
      val ind: UnitQueryTest = new UnitQueryTest(args(titleIndex), args(docIndex), args(wordIndex))
    }
    if (args(corpus) == "Full") {
      val ind: FullQueryTest = new FullQueryTest(args(titleIndex), args(docIndex), args(wordIndex))
    }
  }
}
