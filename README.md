# manifest-destiny

A Clojure library designed to start Clojure code in an UberJar
without having to AOT the Clojure code. How?

## Usage

Set the `Main-Class` to `dpp.rocks.Destiny`
and set the `Implementation-Package` attribute to the Clojure package
that contains your bootstrap code and the `Implementation-Function`
attribute to the function to be called.

Manifest-Destiny does a Clojure `require` on the package and then
`invoke`s the named function, passing the `String[]` that was
handed to the `java -jar` invocation.

## License

Copyright © 2014 David Pollak

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
