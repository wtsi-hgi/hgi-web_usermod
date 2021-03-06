# User module for HGI-web
Implements role-based access control (RBAC). 

## Installation
To install, clone the github repository, change into hgi-web_usermod, and run Scala Built Tool (sbt).

You can also use the sbt bootstrapping script from https://github.com/paulp/sbt-extras by running:

```sh
wget -O ./sbt https://raw.github.com/paulp/sbt-extras/master/sbt
chmod a+x ./sbt
./sbt
```

That should install all prerequisites and leave you at the sbt project shell:
```
[hgi-web_usermod] $
```

From which you can enter the play console:
```
[hgi-web_usermod] $ play
```

After which you can start a dev instance (in this case on port 9000) by running:
```
[hgi-web_usermod] $ run 9000
```

JVM versions lower than 1.7 may have errors on dome Linux machines relating to memory allocation after a fork (`java.io.IOException: error=12`) when running `sbt`. This can be solved by moving to JVM 1.7, or potentially by turning virtual memory overcommit on in the kernel (`echo 1 > /proc/sys/vm/overcommit_memory`).

