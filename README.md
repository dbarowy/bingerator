Bingerator
==========

or "why the heck is it so hard to issue web queries in a program?"

`Bingerator` is an easy-to-use library for doing Bing web and image queries in Scala.

Downloading
-----------

The easiest way to get Bingerator is [as a binary release](https://github.com/dbarowy/bingerator/releases/tag/0.2.1).  Please be sure to have an up-to-date version of Scala (v.2.10.4).

If you need a differently-configured build, I suggest that you [checkout a copy of the source](#source) and [see the build instructions](#building).

Usage
-----

`Bingerator` is very simple to use.  It works much like an ordinary collection object in Scala.  First, include Bingerator into your scope:

    import net.ettinsmoor.Bingerator

Next, create a `Bingerator` object with your API key and then call the search function.  The following example

    val results1 = new Bingerator(key).SearchWeb("cowboy").take(150)

returns 150 search results (a `Stream[WebResult]`), where `key` is your Bing API key.  Note that `Bingerator` does a minimal amount of communication with Bing (a single transaction in the smallest case) until you read enough results that it needs to fetch more.  By contrast,

    val results2 = new Bingerator(key).SearchWeb("cowboy").take(150).toList

_immediately_ calls Bing and returns 150 results (because we called `toList` at the end). The previous incantation will not call Bing until you read `results1`. `Bingerator`'s _laziness_ allows you to express a search result simply, without needing to worry about how big the collection should be until you use the results.  `Bingerator` also _caches_ results so that re-reading previous-retrieved results does not again retrieve new results.  This is important because Bing counts each and every transaction against your monthly quota.  `Bingerator` is presently configured to retrieve the maximum number of results per transaction (50) for maximum savings.

#### I Don't Get It. Can You Show Me How to use Bingerator in a `for` loop?

Sure.

    val b = new Bingerator(key, true)
    val result = b.SearchWeb("cowboy").take(5)
    for (r <- result) {
      println("Result is: " + r.title)
    }

Note that if you don't specify the number of search results using `take`, `Bingerator` will return up to 5000 results (the present cap).  This is probably not what you want, since you have to pay for all 100 of those transactions. Only `take` what you need!

Getting the Source <a name="source"></a>
------------------

There is nothing special here.  I.e.,

    git clone git@github.com:dbarowy/bingerator.git

Building the Library <a name="building"></a>
--------------------

`Bingerator` uses Scala's [Simple Build Tool](http://www.scala-sbt.org/).  Please make sure that you [have SBT installed](http://www.scala-sbt.org/release/docs/Getting-Started/Setup.html) prior to following these directions.  Note that in keeping with my arbitary and yet highly-refined sense of aesthetics, `Bingerator` has no other dependencies (well, other than Scala and Java, of course).

To build a JAR that you can import into your Java/Scala classpath, `cd` into the `bingerator` folder and type

    sbt package

On my machine, SBT creates the following JAR:

    bingerator/target/scala-2.10/bingerator_2.10-0.2.0.jar
    
If you need to modify the build, e.g., to change the required Scala version, see the `build.sbt` buildfile.  Please note that if you change the Scala version, I make no guarantees that `Bingerator` will continue to function properly.

Note on Bing Account Keys
-------------------------

Bing requires that users of the search API have Bing developer accounts.  You will need to create one and give it to `Bingerator` when you ask for results.  Microsoft uses this account to bill you for your usage.  Note that as I write this (Jan 2, 2014), the lowest tier (fewer than 5000 transactions/mo) is free, but you do still need an account.

You can [sign up here](http://datamarket.azure.com/dataset/bing/search).

`WebResult` object
--------------------------

The return type of `SearchWeb` is a `WebResult`.  `WebResult` has the following fields:

| field name | type | description |
| --- | --- | --- |
| `description` | `String` | The short caption that Bing provides for a result. |
| `display_url` | `String` | The same as `url`, except without the scheme name (e.g., `http`) and `://` separator. |
| `id` | `String` | The Bing [GUID](http://msdn.microsoft.com/en-us/library/system.guid%28v=vs.110%29.aspx) identifying the result. |
| `title` | `String` | The name of the result. |
| `url` | `String` | The URL of the search result. |

`ImageResult` object
--------------------------

The return type of `SearchImage` is an `ImageResult`.  `ImageResult` has the following fields:

| field name | type | description |
| --- | --- | --- |
| `content_type` | `ContentType` | A ContentType case class. |
| `display_url` | `String` | The same as `media_url`, except without the scheme name (e.g., `http`) and `://` separator. |
| `file_size` | `long` | The size, in bytes, of the file located at `media_url` |
| `height` | `int` | The height of the image, in pixels. |
| `id` | `String` | The Bing [GUID](http://msdn.microsoft.com/en-us/library/system.guid%28v=vs.110%29.aspx) identifying the result. |
| `media_url` | `String` | The direct URL of the image file itself. |
| `source_url` | `String` | The URL of the page that hosts the image file. |
| `title` | `String` | The name of the result. |
| `width` | `int` | The width of the image, in pixels. |

`ImageResult` also has some helper methods to make it easy to work with image data. Note that `getImage` and `saveImageFile` share an image data cache.

| method name | arguments | return type | description |
| --- | --- | --- | --- |
| `getImage` | none | `java.awt.image.BufferedImage` | This method will download the file and return it as a `BufferedImage`. Note that repeated calls to `getImage` return the same, cached image data. |
| `saveImageFile` | an output file base name | `java.io.File` | This method will save the file using the user-supplied base name and return a `java.io.File` object.  The file extension (e.g., ".jpg") is determined dynamically, based on the image's `content_type`. Note that `saveImageFile` will also download the file if needed, caching the image data as does `getImage`. |

FAQ
---

#### Why Don't You Support Other Bing SourceTypes, like News?

I plan to.  I'll add them to this library as I need them.

#### Why Can't I Specify Bing Search Options Like Image Size?

I'm busy.  Patches are always welcome, by the way.

#### Why Don't You Support Google Web Search?

Good question.  I initially intended to build this tool against Google's Custom Search API.  However, I eventually opted for Bing for the following reasons:

1. Google's SDK is very poorly documented.
2. Google Custom Search can search the entire web ([really](https://support.google.com/customsearch/answer/1210656?hl=en)!), but is extremely limited: only 100 queries per day.
3. Opting to _pay_ to upgrade to Google Site Search, which lifts the number of transactions you can perform, [removes your ability to search the entire web](https://support.google.com/customsearch/answer/72326?hl=en).
4. The old [Google Web Search API](https://developers.google.com/web-search/), which actually does do what I want, is deprecated.

I could have worked around #1 by using the web API instead, which does come with acceptable documentation, but #2, #3, and #4 were dealbreakers for me.  Bing's only shortcoming is that the MSDN documentation is kind of a mess, since it refers to previous versions of the Bing API.  I eventually discovered [the following page](http://datamarket.azure.com/dataset/bing/search) which contains the correct documentation, but puzzlingly, this documentation is in Microsoft Word format.  You win some, you lose some, I suppose.

If Google changes #2, #3, and #4, I will happily add support for Google web search.

Troubleshooting
---------------

If you're having difficulty with `Bingerator`, you can run its test suite to try to diagnose the problem.  If you encounter a problem, please open a GitHub issue that includes the output of the test suite.

    sbt test

Note that the test suite requires the presence of a file called `BingAccount.properties` that contains your Bing account key in the following form:

    key = 34hKZ87Aab2JJj8abz10MM0jh2sIU806fFXXkacveT4

_Be careful not to add `BingAccount.properties` to your git projects, otherwise others will know your secret key!_

If you find a bug in `Bingerator`, and you're adept at writing ScalaTest tests, you will maximize your chances of me fixing the problem if your GitHub issue includes a new test.

Notes
-----

`Bingerator` is designed to work with Scala, and it depends on the Scala `Stream` library.  `Stream` provides quite a bit more functionality than the Java `iterator`.  This may mean that you cannot use `Bingerator` with Java.  I honestly don't know.  If you figure, it out, [send me a note](http://barowy.net/contact/index.html), but I am otherwise uninterested in porting `Bingerator` to Java.

Change Log
----------

| version | changes |
| --- | --- |
| 0.2.1 | Added helper methods to allow easy downloading/saving of Bing image searches. |
| 0.2.0 | Added support for Bing image searches. |
| 0.1.1 | README updates. |
| 0.1.0 | Initial release. |
