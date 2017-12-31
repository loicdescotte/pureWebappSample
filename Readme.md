Simple http4s + Doobie + Cats Effect (IO monad) + Circe POC to show how to build a purely functional web application in Scala (only pure functions, no mutable state).

This is just an example to show how to retrieve a stock value in a database, validate this value ( > 0) and return a result as json.

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

I recommend watching [this talk from Daniel Spiewak](https://www.youtube.com/watch?v=g_jP47HFpWA) to learn more about benefits of the IO monad.

Test part is done using Specs2.

How to run the sample : 

 * sbt run
 * open http://localhost:8080/stock

For a more advanced example, see https://github.com/pauljamescleary/scala-pet-store
