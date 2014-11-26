(defproject dpp.rocks/manifest-destiny "0.2.0"
  :description "A *very* simple jar that loads a Java file that looks in META-INF/MANIFEST.MF for the name of a Clojure namespace to load and run"
  :url "http://dpp.rocks"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :java-source-paths ["src/java"]

  :main dpp.rocks.Destiny

  :manifest {"Premain-Class" "dpp.rocks.Destiny"
             "Can-Redefine-Classes" "true"
             "Can-Retransform-Classes" "true"
             }

  :dependencies [[org.javassist/javassist "3.18.2-GA"]
                 [org.clojure/clojure "1.6.0"]])
