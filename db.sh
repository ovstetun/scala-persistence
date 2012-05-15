#! /bin/sh
java -jar $HOME/.ivy2/cache/org.liquibase/liquibase-core/jars/liquibase-core-2.0.1.jar \
  --driver=org.h2.Driver \
  --classpath=target/scala-2.9.1/classes:$HOME/.ivy2/cache/com.h2database/h2/jars/h2-1.3.153.jar \
  --changeLogFile=no/ovstetun/db/changelog-all.xml \
  --url=jdbc:h2:test \
  --username=sa \
  --password= \
  update
