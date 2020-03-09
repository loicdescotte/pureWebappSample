Simple ZIO + HTTP4s + Doobie + Circe POC to show how to build a purely functional web application in Scala, using functional effects with ZIO.

This is just an example to show how to retrieve and update a stock value in a database, validate this value ( > 0) and return a result as json.

How to run the sample : 

 * sbt run
 * open http://localhost:8080/stock/1

