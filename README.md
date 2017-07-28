# Ferma OrientDB Extensions

Provides wrappers useful for using OrientDB with Ferma.

**Licensed under the Apache Software License v2**

For support please use [Gitter](https://gitter.im/Syncleus/Ferma?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
or the [official Ferma mailing list](https://groups.google.com/a/syncleus.com/forum/#!forum/ferma-list).

## Dependency

To include OrientDB extensions to Ferma include the following Maven dependencies into your build.

```xml
<dependency>
    <groupId>com.syncleus.ferma</groupId>
    <artifactId>ferma</artifactId>
    <version>2.3.0</version>
</dependency>
<dependency>
    <groupId>com.syncleus.ferma</groupId>
    <artifactId>ferma-orientdb</artifactId>
    <version>2.3.0</version>
</dependency>
```

## Examples

```java
  OrientGraphFactory graphFactory = new OrientGraphFactory("memory:tinkerpop").setupPool(4, 10);
  OrientDBTxFactory graph = new OrientDBTxFactory(graphFactory, Vertx.vertx());
  
  try (Tx tx = graph.tx()) {
     Person p = tx.getGraph().addFramedVertex(Person.class);
     tx.success();
  }
```

## Obtaining the Source

The official source repository for OrientDB extension is located in the Syncleus Github repository and can be cloned using the
following command.

```bash
git clone https://github.com/Syncleus/Ferma-OrientDB.git
```

