Simple http4s + Doobie + ZIO + Circe POC to show how to build a purely functional web application in Scala (only pure functions, no mutable state).

This is just an example to show how to retrieve and update a stock value in a database, validate this value ( > 0) and return a result as json.

Pure functions make no side effect and always give the same output for the same input.  
  
Benefits of using pure functions (and isolating effects in an IO monad)  :
 * Pure functions are easier to test
 * Easier to refactor
 * It's easier to understand what they do (more predictable)
 * They are easier to make work in parallel
    * Pure functions have no mutable nor side effects, so they can work in parallel out of the box
    * Threading and asynchronicity will be handle for you when side effects will be executed thanks to the IO monad
 * You can control precisely where/when effects occur
 * For optimization, results of pure functions can be memoized

Test part is done using ZIO environment and Specs2.

How to run the sample : 

 * sbt run
 * open http://localhost:8080/stock/1

