# Love Pop

This is a simple web app that simulates orders for cards and visually shows those
cards, and their parts, progressively getting created. From getting the paper from
the storeroom to mailing the order out.

## Running

Go to the orders page of the app and click the `Add Order` button.

That's it.

## Developing
Ensure you have the required dependencies then run the application

#### Prerequisites

You will need [Leiningen][1] 2.0 or above installed.
[1]: https://github.com/technomancy/leiningen

You will need [Java 8][2] as there is currently a dependency issue with Java 9.
[2]: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

#### Then

To start a web server for the application, run:

`lein run`

In an new terminal, run:

`lein figwheel`

## License

Copyright © 2017 Mike Czabaj
