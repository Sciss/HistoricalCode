requirements:

Scala 2.8
DataNucleus --> http://www.datanucleus.org/project/download.html
JDO 2 API --> http://db.apache.org/jdo/downloads.html ( jdo2-api-2.2.jar ; or use the one that comes with datanucleus dependancies )

you need to create a folder "libraries" and put the stuff in there. mine looks like this:

$ ls -la libraries/
total 168
drwxr-xr-x  23 rutz  rutz  782  8 Mär 19:56 .
drwxr-xr-x  23 rutz  rutz  782  9 Mär 00:44 ..
lrwxr-xr-x   1 rutz  rutz   87  8 Mär 16:08 asm.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/deps/asm-3.1.jar
lrwxr-xr-x   1 rutz  rutz  103  8 Mär 18:58 commons-collections.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/deps/commons-collections-3.1.jar
lrwxr-xr-x   1 rutz  rutz   98  8 Mär 18:59 commons-dbcp.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/deps/commons-dbcp-1.2.1.jar
lrwxr-xr-x   1 rutz  rutz   96  8 Mär 18:59 commons-pool.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/deps/commons-pool-1.2.jar
lrwxr-xr-x   1 rutz  rutz  110  8 Mär 18:49 datanucleus-cache.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/lib/datanucleus-cache-2.0.0-release.jar
lrwxr-xr-x   1 rutz  rutz  119  8 Mär 18:50 datanucleus-connectionpool.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/lib/datanucleus-connectionpool-2.0.0-release.jar
lrwxr-xr-x   1 rutz  rutz  101  8 Mär 16:10 datanucleus-core.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/lib/datanucleus-core-2.0.1.jar
lrwxr-xr-x   1 rutz  rutz  113  8 Mär 16:07 datanucleus-enhancer.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/lib/datanucleus-enhancer-2.0.0-release.jar
lrwxr-xr-x   1 rutz  rutz  105  8 Mär 18:57 datanucleus-jodatime.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/lib/datanucleus-jodatime-2.0.1.jar
lrwxr-xr-x   1 rutz  rutz  100  8 Mär 18:57 datanucleus-jpa.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/lib/datanucleus-jpa-2.0.1.jar
lrwxr-xr-x   1 rutz  rutz  107  8 Mär 18:50 datanucleus-management.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/lib/datanucleus-management-1.0.2.jar
lrwxr-xr-x   1 rutz  rutz  102  8 Mär 17:55 datanucleus-rdbms.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/lib/datanucleus-rdbms-2.0.1.jar
lrwxr-xr-x   1 rutz  rutz  112  8 Mär 19:05 datanucleus-spatial.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/lib/datanucleus-spatial-2.0.0-release.jar
lrwxr-xr-x   1 rutz  rutz  118  8 Mär 19:06 datanucleus-xmltypeoracle.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/lib/datanucleus-xmltypeoracle-2.0.0-release.jar
lrwxr-xr-x   1 rutz  rutz   91  8 Mär 19:01 ehcache.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/deps/ehcache-1.1.jar
lrwxr-xr-x   1 rutz  rutz  110  8 Mär 19:18 geronimo-jpa_2.0_spec.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/deps/geronimo-jpa_2.0_spec-1.0-PFD2.jar
lrwxr-xr-x   1 rutz  rutz  105  8 Mär 19:02 geronimo-jta_1.1_spec.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/deps/geronimo-jta_1.1_spec-1.1.jar
lrwxr-xr-x   1 rutz  rutz   49  8 Mär 18:05 hsqldb.jar -> /Users/rutz/Documents/devel/hsqldb/lib/hsqldb.jar
lrwxr-xr-x   1 rutz  rutz   95  8 Mär 16:08 jdo2-api.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/deps/jdo2-api-2.3-ec.jar
lrwxr-xr-x   1 rutz  rutz   93  8 Mär 19:19 joda-time.jar -> /Users/rutz/Documents/devel/datanucleus-accessplatform-full-deps-2.0.1/deps/joda-time-1.6.jar
lrwxr-xr-x   1 rutz  rutz   70  8 Mär 17:47 scala-library.jar -> /Users/rutz/Documents/devel/scala-2.8.0-snapshot/lib/scala-library.jar

but you don't need all of them. when in doubt, consult the datanucleus guide.

the ant build.xml still lacks a compile task. i compile from within IDEA. the rest is

$ ant enhance
$ ant createschema  (after the db server has been separately booted using start_hsql.sh)

then to test

$ ./run.sh -create 1234 5678
