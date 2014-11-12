(defproject dpp.rocks/manifest-destiny "0.1.0"
  :description "A *very* simple jar that loads a Java file that looks in META-INF/MANIFEST.MF for the name of a Clojure namespace to load and run"
  :url "http://dpp.rocks"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :java-source-paths ["src/java"]

  :main dpp.rocks.Destiny

  :dependencies [[org.clojure/clojure "1.6.0"]])
